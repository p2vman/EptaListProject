package io.github.p2vman.eptalist.bounge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.p2vman.Identifier;
import io.github.p2vman.lang.Lang;
import io.github.p2vman.updater.Updater;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WhiteListCommand extends Command implements TabExecutor {
    private final String[] w1 = {"off", "on", "add", "remove", "list", "help", "mode", "reload","info"};

    public WhiteListCommand() {
        super(Boungecord.config.get().command.getPath());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent("/eptalist (" + String.join("/", w1) + ")"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "off":
                Boungecord.config.get().enable = false;
                Boungecord.config.save();
                sender.sendMessage(new TextComponent(Lang.LANG.format("command.off")));
                break;

            case "on":
                Boungecord.config.get().enable = true;
                Boungecord.config.save();
                sender.sendMessage(new TextComponent(Lang.LANG.format("command.on")));
                break;

            case "add":
                if (args.length < 2) {
                    sender.sendMessage(new TextComponent("Usage: /eptalist add <username>"));
                    break;
                }
                String usernameToAdd = args[1];
                if (Boungecord.list.addUser(usernameToAdd, (t) -> sender.sendMessage(new TextComponent((String) t)))) {
                    sender.sendMessage(new TextComponent(Lang.LANG.format("command.add.succes", usernameToAdd)));
                }
                break;

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(new TextComponent("Usage: /eptalist remove <username>"));
                    break;
                }
                String usernameToRemove = args[1];
                if (Boungecord.list.removeUser(usernameToRemove, (t) -> sender.sendMessage(new TextComponent((String) t)))) {
                    sender.sendMessage(new TextComponent(Lang.LANG.format("command.remove.succes", usernameToRemove)));
                }
                break;

            case "list":
                sender.sendMessage(new TextComponent("Whitelisted players: " + Boungecord.list.toList()));
                break;

            case "mode":
                if (args.length < 2) {
                    sender.sendMessage(new TextComponent("Usage: /eptalist mode <ID>"));
                    break;
                }
                Identifier id = Identifier.tryParse(args[1]);
                if (Boungecord.identifiers.contains(id)) {
                    Boungecord.config.get().curent = id;
                    Boungecord.config.save();
                    Boungecord.load();
                    sender.sendMessage(new TextComponent(Lang.LANG.format("command.mode.succes", id)));
                } else {
                    sender.sendMessage(new TextComponent(Lang.LANG.format("command.mode.invalid.id")));
                }
                break;

            case "help":
                sender.sendMessage(new TextComponent(String.join("\n",
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
                    Boungecord.load();
                    sender.sendMessage(new TextComponent(Lang.LANG.format("command.reload.succes")));
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(new TextComponent(Lang.LANG.format("command.reload.failed")));
                }
                break;
            case "info":
            {
                JsonObject object = Updater.getInstance().getJson().getAsJsonObject("info");
                sender.sendMessage(new TextComponent("links:"));
                for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("urls").entrySet()) {
                    sender.sendMessage(new TextComponent(entry.getKey()+": "+entry.getValue().getAsString()));
                }
                sender.sendMessage(new TextComponent());
                break;
            }

            default:
                sender.sendMessage(new TextComponent(Lang.LANG.getOrDefult("command.default")));
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.stream(w1)
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
