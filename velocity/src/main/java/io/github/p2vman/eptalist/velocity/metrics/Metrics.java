package io.github.p2vman.eptalist.velocity.metrics;

/*
 * This Metrics class was auto-generated and can be copied into your project if you are
 * not using a build tool like Gradle or Maven for dependency management.
 *
 * IMPORTANT: You are not allowed to modify this class, except changing the package.
 *
 * Disallowed modifications include but are not limited to:
 *  - Remove the option for users to opt-out
 *  - Change the frequency for data submission
 *  - Obfuscate the code (every obfuscator should allow you to make an exception for specific files)
 *  - Reformat the code (if you use a linter, add an exception)
 *
 * Violations will result in a ban of your plugin and account from bStats.
 */

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.p2vman.eptalist.metrics.CustomChart;
import io.github.p2vman.eptalist.metrics.JsonObjectBuilder;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Metrics {

    /** A factory to create new Metrics classes. */
    public static class Factory {

        private final ProxyServer server;

        private final Logger logger;

        private final Path dataDirectory;

        // The constructor is not meant to be called by the user.
        // The instance is created using Dependency Injection
        @Inject
        private Factory(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
            this.server = server;
            this.logger = logger;
            this.dataDirectory = dataDirectory;
        }

        /**
         * Creates a new Metrics class.
         *
         * @param plugin The plugin instance.
         * @param serviceId The id of the service. It can be found at <a
         *     href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
         *     <p>Not to be confused with Velocity's {@link PluginDescription#getId()} method!
         * @return A Metrics instance that can be used to register custom charts.
         *     <p>The return value can be ignored, when you do not want to register custom charts.
         */
        public Metrics make(Object plugin, int serviceId) {
            return new Metrics(plugin, server, logger, dataDirectory, serviceId);
        }
    }

    private final PluginContainer pluginContainer;

    private final ProxyServer server;

    private MetricsBase metricsBase;

    private Metrics(
            Object plugin, ProxyServer server, Logger logger, Path dataDirectory, int serviceId) {
        pluginContainer =
                server
                        .getPluginManager()
                        .fromInstance(plugin)
                        .orElseThrow(
                                () -> new IllegalArgumentException("The provided instance is not a plugin"));
        this.server = server;
        File configFile = dataDirectory.getParent().resolve("bStats").resolve("config.txt").toFile();
        MetricsConfig config;
        try {
            config = new MetricsConfig(configFile, true);
        } catch (IOException e) {
            logger.error("Failed to create bStats config", e);
            return;
        }
        metricsBase =
                new MetricsBase(
                        "velocity",
                        config.getServerUUID(),
                        serviceId,
                        config.isEnabled(),
                        this::appendPlatformData,
                        this::appendServiceData,
                        task -> server.getScheduler().buildTask(plugin, task).schedule(),
                        () -> true,
                        logger::warn,
                        logger::info,
                        config.isLogErrorsEnabled(),
                        config.isLogSentDataEnabled(),
                        config.isLogResponseStatusTextEnabled(),
                        false);
        if (!config.didExistBefore()) {
            // Send an info message when the bStats config file gets created for the first time
            logger.info(
                    "Velocity and some of its plugins collect metrics and send them to bStats (https://bStats.org).");
            logger.info(
                    "bStats collects some basic information for plugin authors, like how many people use");
            logger.info(
                    "their plugin and their total player count. It's recommend to keep bStats enabled, but");
            logger.info(
                    "if you're not comfortable with this, you can opt-out by editing the config.txt file in");
            logger.info("the '/plugins/bStats/' folder and setting enabled to false.");
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
        if (metricsBase != null) {
            metricsBase.addCustomChart(chart);
        }
    }

    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", server.getPlayerCount());
        builder.appendField("managedServers", server.getAllServers().size());
        builder.appendField("onlineMode", server.getConfiguration().isOnlineMode() ? 1 : 0);
        builder.appendField("velocityVersionVersion", server.getVersion().getVersion());
        builder.appendField("velocityVersionName", server.getVersion().getName());
        builder.appendField("velocityVersionVendor", server.getVersion().getVendor());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField(
                "pluginVersion", pluginContainer.getDescription().getVersion().orElse("unknown"));
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

    /**
     * A simple config for bStats.
     *
     * <p>This class is not used by every platform.
     */
    public static class MetricsConfig {

        private final File file;

        private final boolean defaultEnabled;

        private String serverUUID;

        private boolean enabled;

        private boolean logErrors;

        private boolean logSentData;

        private boolean logResponseStatusText;

        private boolean didExistBefore = true;

        public MetricsConfig(File file, boolean defaultEnabled) throws IOException {
            this.file = file;
            this.defaultEnabled = defaultEnabled;
            setupConfig();
        }

        public String getServerUUID() {
            return serverUUID;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isLogErrorsEnabled() {
            return logErrors;
        }

        public boolean isLogSentDataEnabled() {
            return logSentData;
        }

        public boolean isLogResponseStatusTextEnabled() {
            return logResponseStatusText;
        }

        /**
         * Checks whether the config file did exist before or not.
         *
         * @return If the config did exist before.
         */
        public boolean didExistBefore() {
            return didExistBefore;
        }

        /** Creates the config file if it does not exist and read its content. */
        private void setupConfig() throws IOException {
            if (!file.exists()) {
                // Looks like it's the first time we create it (or someone deleted it).
                didExistBefore = false;
                writeConfig();
            }
            readConfig();
            if (serverUUID == null) {
                // Found a malformed config file with no UUID. Let's recreate it.
                writeConfig();
                readConfig();
            }
        }

        /** Creates a config file with teh default content. */
        private void writeConfig() throws IOException {
            List<String> configContent = new ArrayList<>();
            configContent.add(
                    "# bStats (https://bStats.org) collects some basic information for plugin authors, like");
            configContent.add(
                    "# how many people use their plugin and their total player count. It's recommended to keep");
            configContent.add(
                    "# bStats enabled, but if you're not comfortable with this, you can turn this setting off.");
            configContent.add(
                    "# There is no performance penalty associated with having metrics enabled, and data sent to");
            configContent.add("# bStats is fully anonymous.");
            configContent.add("enabled=" + defaultEnabled);
            configContent.add("server-uuid=" + UUID.randomUUID().toString());
            configContent.add("log-errors=false");
            configContent.add("log-sent-data=false");
            configContent.add("log-response-status-text=false");
            writeFile(file, configContent);
        }

        /** Reads the content of the config file. */
        private void readConfig() throws IOException {
            List<String> lines = readFile(file);
            if (lines == null) {
                throw new AssertionError("Content of newly created file is null");
            }
            enabled = getConfigValue("enabled", lines).map("true"::equals).orElse(true);
            serverUUID = getConfigValue("server-uuid", lines).orElse(null);
            logErrors = getConfigValue("log-errors", lines).map("true"::equals).orElse(false);
            logSentData = getConfigValue("log-sent-data", lines).map("true"::equals).orElse(false);
            logResponseStatusText =
                    getConfigValue("log-response-status-text", lines).map("true"::equals).orElse(false);
        }

        /**
         * Gets a config setting from the given list of lines of the file.
         *
         * @param key The key for the setting.
         * @param lines The lines of the file.
         * @return The value of the setting.
         */
        private Optional<String> getConfigValue(String key, List<String> lines) {
            return lines.stream()
                    .filter(line -> line.startsWith(key + "="))
                    .map(line -> line.replaceFirst(Pattern.quote(key + "="), ""))
                    .findFirst();
        }

        /**
         * Reads the text content of the given file.
         *
         * @param file The file to read.
         * @return The lines of the given file.
         */
        private List<String> readFile(File file) throws IOException {
            if (!file.exists()) {
                return null;
            }
            try (FileReader fileReader = new FileReader(file);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                return bufferedReader.lines().collect(Collectors.toList());
            }
        }

        /**
         * Writes the given lines to the given file.
         *
         * @param file The file to write to.
         * @param lines The lines to write.
         */
        private void writeFile(File file, List<String> lines) throws IOException {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (FileWriter fileWriter = new FileWriter(file);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                for (String line : lines) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            }
        }
    }
}