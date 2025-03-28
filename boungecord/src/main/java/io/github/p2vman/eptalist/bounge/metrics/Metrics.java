package io.github.p2vman.eptalist.bounge.metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import io.github.p2vman.eptalist.metrics.CustomChart;
import io.github.p2vman.eptalist.metrics.JsonObjectBuilder;

public class Metrics {

    private final Plugin plugin;

    private final MetricsBase metricsBase;

    private boolean enabled;

    private String serverUUID;

    private boolean logErrors = false;

    private boolean logSentData;

    private boolean logResponseStatusText;

    /**
     * Creates a new Metrics instance.
     *
     * @param plugin Your plugin instance.
     * @param serviceId The id of the service. It can be found at <a
     *     href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public Metrics(Plugin plugin, int serviceId) {
        this.plugin = plugin;
        try {
            loadConfig();
        } catch (IOException e) {
            // Failed to load configuration
            plugin.getLogger().log(Level.WARNING, "Failed to load bStats config!", e);
            metricsBase = null;
            return;
        }
        metricsBase =
                new MetricsBase(
                        "bungeecord",
                        serverUUID,
                        serviceId,
                        enabled,
                        this::appendPlatformData,
                        this::appendServiceData,
                        null,
                        () -> true,
                        (message, error) -> this.plugin.getLogger().log(Level.WARNING, message, error),
                        (message) -> this.plugin.getLogger().log(Level.INFO, message),
                        logErrors,
                        logSentData,
                        logResponseStatusText,
                        false);
    }

    /** Loads the bStats configuration. */
    private void loadConfig() throws IOException {
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        bStatsFolder.mkdirs();
        File configFile = new File(bStatsFolder, "config.yml");
        if (!configFile.exists()) {
            writeFile(
                    configFile,
                    "# bStats (https://bStats.org) collects some basic information for plugin authors, like how",
                    "# many people use their plugin and their total player count. It's recommended to keep bStats",
                    "# enabled, but if you're not comfortable with this, you can turn this setting off. There is no",
                    "# performance penalty associated with having metrics enabled, and data sent to bStats is fully",
                    "# anonymous.",
                    "enabled: true",
                    "serverUuid: \"" + UUID.randomUUID() + "\"",
                    "logFailedRequests: false",
                    "logSentData: false",
                    "logResponseStatusText: false");
        }
        Configuration configuration =
                ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        // Load configuration
        enabled = configuration.getBoolean("enabled", true);
        serverUUID = configuration.getString("serverUuid");
        logErrors = configuration.getBoolean("logFailedRequests", false);
        logSentData = configuration.getBoolean("logSentData", false);
        logResponseStatusText = configuration.getBoolean("logResponseStatusText", false);
    }

    private void writeFile(File file, String... lines) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        }
    }

    /** Shuts down the underlying scheduler service. */
    public void shutdown() {
        metricsBase.shutdown();
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(CustomChart chart) {
        metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", plugin.getProxy().getOnlineCount());
        builder.appendField("managedServers", plugin.getProxy().getServers().size());
        builder.appendField("onlineMode", plugin.getProxy().getConfig().isOnlineMode() ? 1 : 0);
        builder.appendField("bungeecordVersion", plugin.getProxy().getVersion());
        builder.appendField("bungeecordName", plugin.getProxy().getName());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", plugin.getDescription().getVersion());
    }

    public static class MetricsBase {

        /** The version of the Metrics class. */
        public static final String METRICS_VERSION = "3.1.0";

        private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";

        private final ScheduledExecutorService scheduler;

        private final String platform;

        private final String serverUuid;

        private final int serviceId;

        private final Consumer<JsonObjectBuilder> appendPlatformDataConsumer;

        private final Consumer<JsonObjectBuilder> appendServiceDataConsumer;

        private final Consumer<Runnable> submitTaskConsumer;

        private final Supplier<Boolean> checkServiceEnabledSupplier;

        private final BiConsumer<String, Throwable> errorLogger;

        private final Consumer<String> infoLogger;

        private final boolean logErrors;

        private final boolean logSentData;

        private final boolean logResponseStatusText;

        private final Set<CustomChart> customCharts = new HashSet<>();

        private final boolean enabled;

        /**
         * Creates a new MetricsBase class instance.
         *
         * @param platform The platform of the service.
         * @param serviceId The id of the service.
         * @param serverUuid The server uuid.
         * @param enabled Whether or not data sending is enabled.
         * @param appendPlatformDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
         *     appends all platform-specific data.
         * @param appendServiceDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
         *     appends all service-specific data.
         * @param submitTaskConsumer A consumer that takes a runnable with the submit task. This can be
         *     used to delegate the data collection to a another thread to prevent errors caused by
         *     concurrency. Can be {@code null}.
         * @param checkServiceEnabledSupplier A supplier to check if the service is still enabled.
         * @param errorLogger A consumer that accepts log message and an error.
         * @param infoLogger A consumer that accepts info log messages.
         * @param logErrors Whether or not errors should be logged.
         * @param logSentData Whether or not the sent data should be logged.
         * @param logResponseStatusText Whether or not the response status text should be logged.
         * @param skipRelocateCheck Whether or not the relocate check should be skipped.
         */
        public MetricsBase(
                String platform,
                String serverUuid,
                int serviceId,
                boolean enabled,
                Consumer<JsonObjectBuilder> appendPlatformDataConsumer,
                Consumer<JsonObjectBuilder> appendServiceDataConsumer,
                Consumer<Runnable> submitTaskConsumer,
                Supplier<Boolean> checkServiceEnabledSupplier,
                BiConsumer<String, Throwable> errorLogger,
                Consumer<String> infoLogger,
                boolean logErrors,
                boolean logSentData,
                boolean logResponseStatusText,
                boolean skipRelocateCheck) {
            ScheduledThreadPoolExecutor scheduler =
                    new ScheduledThreadPoolExecutor(
                            1,
                            task -> {
                                Thread thread = new Thread(task, "bStats-Metrics");
                                thread.setDaemon(true);
                                return thread;
                            });
            // We want delayed tasks (non-periodic) that will execute in the future to be
            // cancelled when the scheduler is shutdown.
            // Otherwise, we risk preventing the server from shutting down even when
            // MetricsBase#shutdown() is called
            scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            this.scheduler = scheduler;
            this.platform = platform;
            this.serverUuid = serverUuid;
            this.serviceId = serviceId;
            this.enabled = enabled;
            this.appendPlatformDataConsumer = appendPlatformDataConsumer;
            this.appendServiceDataConsumer = appendServiceDataConsumer;
            this.submitTaskConsumer = submitTaskConsumer;
            this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
            this.errorLogger = errorLogger;
            this.infoLogger = infoLogger;
            this.logErrors = logErrors;
            this.logSentData = logSentData;
            this.logResponseStatusText = logResponseStatusText;
            if (!skipRelocateCheck) {
                checkRelocation();
            }
            if (enabled) {
                // WARNING: Removing the option to opt-out will get your plugin banned from
                // bStats
                startSubmitting();
            }
        }

        public void addCustomChart(CustomChart chart) {
            this.customCharts.add(chart);
        }

        public void shutdown() {
            scheduler.shutdown();
        }

        private void startSubmitting() {
            final Runnable submitTask =
                    () -> {
                        if (!enabled || !checkServiceEnabledSupplier.get()) {
                            // Submitting data or service is disabled
                            scheduler.shutdown();
                            return;
                        }
                        if (submitTaskConsumer != null) {
                            submitTaskConsumer.accept(this::submitData);
                        } else {
                            this.submitData();
                        }
                    };
            // Many servers tend to restart at a fixed time at xx:00 which causes an uneven
            // distribution of requests on the
            // bStats backend. To circumvent this problem, we introduce some randomness into
            // the initial and second delay.
            // WARNING: You must not modify and part of this Metrics class, including the
            // submit delay or frequency!
            // WARNING: Modifying this code will get your plugin banned on bStats. Just
            // don't do it!
            long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
            long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
            scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(
                    submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
        }

        private void submitData() {
            final JsonObjectBuilder baseJsonBuilder = new JsonObjectBuilder();
            appendPlatformDataConsumer.accept(baseJsonBuilder);
            final JsonObjectBuilder serviceJsonBuilder = new JsonObjectBuilder();
            appendServiceDataConsumer.accept(serviceJsonBuilder);
            JsonObjectBuilder.JsonObject[] chartData =
                    customCharts.stream()
                            .map(customChart -> customChart.getRequestJsonObject(errorLogger, logErrors))
                            .filter(Objects::nonNull)
                            .toArray(JsonObjectBuilder.JsonObject[]::new);
            serviceJsonBuilder.appendField("id", serviceId);
            serviceJsonBuilder.appendField("customCharts", chartData);
            baseJsonBuilder.appendField("service", serviceJsonBuilder.build());
            baseJsonBuilder.appendField("serverUUID", serverUuid);
            baseJsonBuilder.appendField("metricsVersion", METRICS_VERSION);
            JsonObjectBuilder.JsonObject data = baseJsonBuilder.build();
            scheduler.execute(
                    () -> {
                        try {
                            // Send the data
                            sendData(data);
                        } catch (Exception e) {
                            // Something went wrong! :(
                            if (logErrors) {
                                errorLogger.accept("Could not submit bStats metrics data", e);
                            }
                        }
                    });
        }

        private void sendData(JsonObjectBuilder.JsonObject data) throws Exception {
            if (logSentData) {
                infoLogger.accept("Sent bStats metrics data: " + data.toString());
            }
            String url = String.format(REPORT_URL, platform);
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            // Compress the data to save bandwidth
            byte[] compressedData = compress(data.toString());
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Connection", "close");
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Metrics-Service/1");
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(compressedData);
            }
            StringBuilder builder = new StringBuilder();
            try (BufferedReader bufferedReader =
                         new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
            }
            if (logResponseStatusText) {
                infoLogger.accept("Sent data to bStats and received response: " + builder);
            }
        }

        /** Checks that the class was properly relocated. */
        private void checkRelocation() {
            // You can use the property to disable the check in your test environment
            if (System.getProperty("bstats.relocatecheck") == null
                    || !System.getProperty("bstats.relocatecheck").equals("false")) {
                // Maven's Relocate is clever and changes strings, too. So we have to use this
                // little "trick" ... :D
                final String defaultPackage =
                        new String(new byte[] {'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's'});
                final String examplePackage =
                        new String(new byte[] {'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
                // We want to make sure no one just copy & pastes the example and uses the wrong
                // package names
                if (MetricsBase.class.getPackage().getName().startsWith(defaultPackage)
                        || MetricsBase.class.getPackage().getName().startsWith(examplePackage)) {
                    throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
                }
            }
        }

        /**
         * Gzips the given string.
         *
         * @param str The string to gzip.
         * @return The gzipped string.
         */
        private static byte[] compress(final String str) throws IOException {
            if (str == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return outputStream.toByteArray();
        }
    }
}