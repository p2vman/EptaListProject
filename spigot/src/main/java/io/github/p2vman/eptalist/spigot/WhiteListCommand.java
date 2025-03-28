package io.github.p2vman.eptalist.spigot;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.p2vman.lang.Lang;
import io.github.p2vman.updater.Updater;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.StringUtil;
import io.github.p2vman.Identifier;
import io.github.p2vman.Utils;

import java.util.*;
import java.util.function.Function;

public class WhiteListCommand extends Command {
    private final String[] w1 = new String[] {
            "off",
            "on",
            "add",
            "remove",
            "list",
            "help",
            "mode",
            "kick_nolisted",
            "reload",
            "info"
    };
    private final Permission permission_enable;
    private final Permission permission_add;
    private final Permission permission_remove;
    private final Permission permission_reload;
    private final Permission permission_mode;
    private final Permission permission_outher;
    private final Permission permission_list;


    public WhiteListCommand(Identifier identifier) {
        super(identifier.getPath());
        Function<Permission, Permission> recal = (p) -> {
            p.recalculatePermissibles();
            return p;
        };

        permission_enable = recal.apply(new Permission("eptalist.enable", PermissionDefault.OP));
        permission_add = recal.apply(new Permission("eptalist.add", PermissionDefault.OP));
        permission_remove = recal.apply(new Permission("eptalist.remove", PermissionDefault.OP));
        permission_reload = recal.apply(new Permission("eptalist.reload", PermissionDefault.OP));
        permission_mode = recal.apply(new Permission("eptalist.mode", PermissionDefault.OP));
        permission_list = recal.apply(new Permission("eptalist.list", PermissionDefault.OP));
        permission_outher = recal.apply(new Permission("eptalist.outher", PermissionDefault.OP));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(args.length > 0)) {
            sender.sendMessage(String.format("/%s (%s)", getName(), String.join("/", w1)));
            return false;
        }
        switch (args[0]) {
            case "off": if (testPermission(sender, permission_enable)) {
                EptaList.config.get().enable = false;
                EptaList.config.save();
                sender.sendMessage(Lang.LANG.format("command.off"));
            }
                break;
            case "on": if (testPermission(sender, permission_enable)) {
                EptaList.config.get().enable = true;
                EptaList.config.save();
                sender.sendMessage(Lang.LANG.format("command.on"));
            }
                break;
            case "add": if (testPermission(sender, permission_add)) {
                List<String> info = new ArrayList<>();
                if (!Utils.len(args, 1)) {
                    sender.sendMessage("Usage: /" + commandLabel + " add <username>");
                } else if (EptaList.list.addUser(args[1], info)) {
                    sender.sendMessage(Lang.LANG.format("command.add.succes", args[1]));
                } else {
                    for (String line : info) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }
            }
                break;
            case "remove": if (testPermission(sender, permission_remove)) {
                List<String> info = new ArrayList<>();
                if (!Utils.len(args, 1)) {
                    sender.sendMessage("Usage: /" + commandLabel + " remove <username>");
                } else if (EptaList.list.removeUser(args[1], info)) {
                    sender.sendMessage(Lang.LANG.format("command.remove.succes", args[1]));
                } else {
                    for (String line : info) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }
            }
                break;
            case "list": if (testPermission(sender, permission_list)) {
                sender.sendMessage("Whitelisted players: " + EptaList.list.toList().toString());
            }
                break;
            case "mode": if (testPermission(sender, permission_mode)) {
                if (!Utils.len(args, 1)) {
                    sender.sendMessage("Usage: /" + commandLabel + " mode <ID>");
                } else {
                    Identifier id = Identifier.tryParse(args[1]);
                    if (id != null && EptaList.identifiers.contains(id)) {
                        EptaList.config.get().curent = id;
                        EptaList.config.save();
                        EptaList.load();
                        sender.sendMessage(Lang.LANG.format("command.mode.succes", id));
                    } else {
                        sender.sendMessage(Lang.LANG.format("command.mode.invalid.id"));
                    }
                }
            }
                break;
            case "kick_nolisted": if (testPermission(sender, permission_outher)) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (!EptaList.list.is(player.getName())) {
                        player.kickPlayer(EptaList.mode.kick_msg);
                    }
                }
                sender.sendMessage("Non-whitelisted players have been kicked.");
            }
                break;
            case "help": {
                sender.sendMessage(new String[]{
                        String.format("1: /%s add <username> - Add user to whitelist", getName()),
                        String.format("2: /%s remove <username> - Remove user from whitelist", getName()),
                        String.format("3: /%s list - Display whitelisted players", getName()),
                        String.format("4: /%s on - Enable whitelist", getName()),
                        String.format("5: /%s off - Disable whitelist", getName()),
                        String.format("6: /%s mode <ID> - Set list data mode", getName()),
                        String.format("7: /%s kick_nolisted - Kick non-whitelisted players", getName()),
                        String.format("8: /%s reload - Reload the whitelist configuration", getName()),
                });
            }
                break;
            case "reload": if (testPermission(sender, permission_reload)) {
                try {
                    EptaList.load();
                    sender.sendMessage(Lang.LANG.format("command.reload.succes"));
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(Lang.LANG.format("command.reload.failed"));
                }
            }
                break;
            case "info":
            {
                JsonObject object = Updater.getInstance().getJson().getAsJsonObject("info");
                sender.sendMessage("links:");
                for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("urls").entrySet()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2"+entry.getKey()+"&f: &4"+entry.getValue().getAsString()));
                }
                sender.sendMessage("");
                break;
            }
            default:
                sender.sendMessage(Lang.LANG.getOrDefult("command.default"));
                break;
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 0) {
            return ImmutableList.of();
        } else if (args.length == 1) {
            String lastWord = args[args.length - 1];
            ArrayList<String> matchedPlayers = new ArrayList();
            Iterator var7 = Arrays.stream(w1).iterator();
            while(var7.hasNext()) {
                String name = (String)var7.next();
                if (StringUtil.startsWithIgnoreCase(name, lastWord)) {
                    matchedPlayers.add(name);
                }
            }

            Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                String lastWord = args[args.length - 1];

                Player senderPlayer = sender instanceof Player ? (Player) sender : null;

                ArrayList<String> matchedPlayers = new ArrayList<String>();
                for (Player player : sender.getServer().getOnlinePlayers()) {
                    String name = player.getName();
                    if ((senderPlayer == null || senderPlayer.canSee(player)) && StringUtil.startsWithIgnoreCase(name, lastWord)) {
                        matchedPlayers.add(name);
                    }
                }

                Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
                return matchedPlayers;
            } else if (args[0].equalsIgnoreCase("mode")) {
                String lastWord = args[args.length - 1];
                ArrayList<String> matchedPlayers = new ArrayList();
                Iterator var7 = EptaList.identifiers.iterator();
                while(var7.hasNext()) {
                    String name = var7.next().toString();
                    if (StringUtil.startsWithIgnoreCase(name, lastWord)) {
                        matchedPlayers.add(name);
                    }
                }

                Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
                return matchedPlayers;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                String lastWord = args[args.length - 1];
                ArrayList<String> matchedPlayers = new ArrayList();
                Iterator var7 = EptaList.identifiers.iterator();
                while (var7.hasNext()) {
                    String name = var7.next().toString();
                    if (StringUtil.startsWithIgnoreCase(name, lastWord)) {
                        matchedPlayers.add(name);
                    }
                }

                Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
                return matchedPlayers;
            }
        }

        return ImmutableList.of();
    }

    public boolean testPermission(CommandSender target, Permission permission) {
        if (target.hasPermission(permission.getName())) {
            return true;
        } else {
            target.sendMessage(Lang.LANG.format("perm.throw"));
            return false;
        }
    }
}
