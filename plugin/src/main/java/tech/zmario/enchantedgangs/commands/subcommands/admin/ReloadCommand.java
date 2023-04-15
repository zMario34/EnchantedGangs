package tech.zmario.enchantedgangs.commands.subcommands.admin;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;

@RequiredArgsConstructor
public class ReloadCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadPlugin();
        sender.sendMessage(MessagesConfiguration.RELOAD_SUCCESS.getString(null));
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }
}
