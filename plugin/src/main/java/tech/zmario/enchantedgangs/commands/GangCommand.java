package tech.zmario.enchantedgangs.commands;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.commands.subcommands.*;
import tech.zmario.enchantedgangs.commands.subcommands.economy.DepositCommand;
import tech.zmario.enchantedgangs.commands.subcommands.economy.WithdrawCommand;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;
import tech.zmario.enchantedgangs.utils.Utils;

import java.util.Map;
import java.util.Optional;

public class GangCommand implements CommandExecutor {

    private final EnchantedGangs plugin;
    private final Map<String, SubCommand> subCommands = Maps.newHashMap();;

    public GangCommand(EnchantedGangs plugin) {
        this.plugin = plugin;

        String label = SettingsConfiguration.COMMAND_GANG_NAME.getString();

        if (label == null || label.isEmpty()) label = "gang";

        PluginCommand pluginCommand = plugin.getCommand(label);
        String[] aliases = SettingsConfiguration.COMMAND_GANG_ALIASES.getStringList().toArray(new String[0]);

        Utils.registerCommand(this, pluginCommand, aliases, label, plugin);

        addSubCommand(SettingsConfiguration.COMMAND_CREATE_NAME.getString(), new CreateCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_INVITE_NAME.getString(), new InviteCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_KICK_NAME.getString(), new KickCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_LEAVE_NAME.getString(), new LeaveCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_DISBAND_NAME.getString(), new DisbandCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_ACCEPT_NAME.getString(), new AcceptCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_PROMOTE_NAME.getString(), new PromoteCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_DEMOTE_NAME.getString(), new DemoteCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_SHOW_NAME.getString(), new ShowCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_LIST_NAME.getString(), new ListCommand(plugin));
        addSubCommand(SettingsConfiguration.COMMAND_CHAT_NAME.getString(), new ChatCommand(plugin));

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            if (SettingsConfiguration.FEATURES_BANK_ENABLED.getBoolean()) {
                addSubCommand(SettingsConfiguration.COMMAND_DEPOSIT_NAME.getString(), new DepositCommand(plugin));
                addSubCommand(SettingsConfiguration.COMMAND_WITHDRAW_NAME.getString(), new WithdrawCommand(plugin));
            }
        }

        if (SettingsConfiguration.FEATURES_CHEST_ENABLED.getBoolean())
            addSubCommand(SettingsConfiguration.COMMAND_CHEST_NAME.getString(), new ChestCommand(plugin));
    }


    public void addSubCommand(String label, SubCommand subCommand) {
        subCommands.put(label, subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        String permission = plugin.getConfig().getString("commands.gangs.permission");

        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendMessage(MessagesConfiguration.NO_PERMISSION.getString(sender instanceof Player ?
                    (Player) sender : null));
            return true;
        }

        if (args.length > 0) {
            Optional<SubCommand> subCommandOptional = Optional.ofNullable(subCommands.get(args[0].toLowerCase()));

            if (subCommandOptional.isPresent()) {
                SubCommand subCommand = subCommandOptional.get();

                if (subCommand.isPlayerOnly() && sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(MessagesConfiguration.NO_CONSOLE
                            .getString(sender instanceof Player ? (Player) sender : null));
                    return true;
                }

                String subcommandPermission = plugin.getConfig().getString("commands." + args[0].toLowerCase() + ".permission");

                if (subcommandPermission != null && !subcommandPermission.isEmpty() &&
                        !sender.hasPermission(subcommandPermission)) {
                    sender.sendMessage(MessagesConfiguration.NO_PERMISSION
                            .getString(sender instanceof Player ? (Player) sender : null));
                    return true;
                }

                if (subCommand.getMinArgs() > args.length - 1) {
                    sender.sendMessage(MessagesConfiguration.SUBCOMMAND_USAGE
                            .getString(sender instanceof Player ? (Player) sender : null)
                            .replace("%subcommand%", args[0].toLowerCase()));
                    return true;
                }

                subCommand.execute(sender, args);
                return true;
            }
        }

        for (String string : MessagesConfiguration.HELP.getStringList())
            sender.sendMessage(string.replace("%version%", plugin.getDescription().getVersion()));

        return true;
    }
}