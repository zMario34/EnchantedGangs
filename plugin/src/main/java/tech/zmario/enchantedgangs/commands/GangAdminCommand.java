package tech.zmario.enchantedgangs.commands;

import com.google.common.collect.Maps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.commands.subcommands.admin.DisbandCommand;
import tech.zmario.enchantedgangs.commands.subcommands.admin.ReloadCommand;
import tech.zmario.enchantedgangs.commands.subcommands.admin.StatisticCommand;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;
import tech.zmario.enchantedgangs.utils.Utils;

import java.util.Map;
import java.util.Optional;

public class GangAdminCommand implements CommandExecutor {

    private final EnchantedGangs plugin;
    private final Map<String, SubCommand> subCommands = Maps.newHashMap();;

    public GangAdminCommand(EnchantedGangs plugin) {
        this.plugin = plugin;

        String label = SettingsConfiguration.COMMAND_GANG_ADMIN_NAME.getString();

        if (label == null || label.isEmpty()) label = "gangadmin";

        PluginCommand pluginCommand = plugin.getCommand(label);
        String[] aliases = SettingsConfiguration.COMMAND_GANG_ADMIN_ALIASES.getStringList().toArray(new String[0]);

        Utils.registerCommand(this, pluginCommand, aliases, label, plugin);

        addSubCommand(SettingsConfiguration.COMMAND_ADMIN_RELOAD_NAME.getString(), new ReloadCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_ADMIN_DISBAND_NAME.getString(), new DisbandCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_ADMIN_STATISTIC_NAME.getString(), new StatisticCommand(plugin));
    }


    public void addSubCommand(String label, SubCommand subCommand) {
        subCommands.put(label, subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        String permission = plugin.getConfig().getString("commands.gang-admin.permission");

        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfiguration.NO_PERMISSION.getString(sender instanceof Player ? (Player) sender : null));
            return true;
        }

        if (args.length > 0) {
            Optional<SubCommand> subCommandOptional = Optional.ofNullable(subCommands.get(args[0].toLowerCase()));

            if (subCommandOptional.isPresent()) {
                SubCommand subCommand = subCommandOptional.get();
                String subcommandPermission = plugin.getConfig().getString("commands.admin." + args[0].toLowerCase() + ".permission");

                if (subcommandPermission != null && !subcommandPermission.isEmpty() &&
                        !sender.hasPermission(subcommandPermission)) {
                    sender.sendMessage(MessagesConfiguration.NO_PERMISSION
                            .getString(sender instanceof Player ? (Player) sender : null));
                    return true;
                }

                if (subCommand.getMinArgs() > args.length - 1) {
                    sender.sendMessage(MessagesConfiguration.ADMIN_SUBCOMMAND_USAGE
                            .getString(sender instanceof Player ? (Player) sender : null)
                            .replace("%subcommand%", args[0].toLowerCase()));
                    return true;
                }

                subCommand.execute(sender, args);
                return true;
            }
        }

        for (String string : MessagesConfiguration.ADMIN_HELP.getStringList())
            sender.sendMessage(string.replace("%version%", plugin.getDescription().getVersion()));

        return true;
    }
}