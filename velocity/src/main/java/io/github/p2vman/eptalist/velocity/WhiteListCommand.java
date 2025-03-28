package io.github.p2vman.eptalist.velocity;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import io.github.p2vman.Identifier;
import io.github.p2vman.lang.Lang;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WhiteListCommand implements SimpleCommand {
    private final String[] w1 = new String[]{
            "off",
            "on",
            "add",
            "remove",
            "list",
            "help",
            "mode",
            "reload"
    };

    private Logger logger;

    public WhiteListCommand(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sender.sendMessage(Component.text(String.format("/eptalist (%s)", String.join("/", w1))));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "off":
                Velocity.config.get().enable = false;
                Velocity.config.save();
                sender.sendMessage(Component.text(Lang.LANG.format("command.off")));
                break;

            case "on":
                Velocity.config.get().enable = true;
                Velocity.config.save();
                sender.sendMessage(Component.text(Lang.LANG.format("command.on")));
                break;

            case "add":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /eptalist add <username>"));
                    break;
                }
                String usernameToAdd = args[1];
                List<String> infoAdd = new ArrayList<>();
                if (Velocity.list.addUser(usernameToAdd, infoAdd)) {
                    sender.sendMessage(Component.text(Lang.LANG.format("command.add.succes", usernameToAdd)));
                } else {
                    infoAdd.forEach(line -> sender.sendMessage(Component.text(line)));
                }
                break;

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /eptalist remove <username>"));
                    break;
                }
                String usernameToRemove = args[1];
                List<String> infoRemove = new ArrayList<>();
                if (Velocity.list.removeUser(usernameToRemove, infoRemove)) {
                    sender.sendMessage(Component.text(Lang.LANG.format("command.remove.succes", usernameToRemove)));
                } else {
                    infoRemove.forEach(line -> sender.sendMessage(Component.text(line)));
                }
                break;

            case "list":
                sender.sendMessage(Component.text("Whitelisted players: " + Velocity.list.toList()));
                break;

            case "mode":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /eptalist mode <ID>"));
                    break;
                }
                Identifier id = Identifier.tryParse(args[1]);
                if (id != null && Velocity.identifiers.contains(id)) {
                    Velocity.config.get().curent = id;
                    Velocity.config.save();
                    Velocity.load();
                    sender.sendMessage(Component.text(Lang.LANG.format("command.mode.succes", id)));
                } else {
                    sender.sendMessage(Component.text(Lang.LANG.format("command.mode.invalid.id")));
                }
                break;
            case "help":
                sender.sendMessage(Component.text(String.join("\n",
                        "1: /eptalist add <username> - Add user to whitelist",
                        "2: /eptalist remove <username> - Remove user from whitelist",
                        "3: /eptalist list - Display whitelisted players",
                        "4: /eptalist on - Enable whitelist",
                        "5: /eptalist off - Disable whitelist",
                        "6: /eptalist mode <ID> - Set list data mode",
                        "7: /eptalist kick_nolisted - Kick non-whitelisted players",
                        "8: /eptalist reload - Reload the whitelist configuration"
                )));
                break;

            case "reload":
                try {
                    Velocity.load();
                    sender.sendMessage(Component.text(Lang.LANG.format("command.reload.succes")));
                } catch (Exception e) {
                    logger.error("Failed to reload the configuration.", e);
                    sender.sendMessage(Component.text(Lang.LANG.format("command.reload.failed")));
                }
                break;

            default:
                sender.sendMessage(Component.text(Lang.LANG.getOrDefult("command.default")));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return ImmutableList.of();
        } else if (args.length == 1) {
            return Arrays.stream(w1)
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }
        return ImmutableList.of();
    }
}
