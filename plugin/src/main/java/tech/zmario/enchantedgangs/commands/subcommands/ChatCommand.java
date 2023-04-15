package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;

@RequiredArgsConstructor
public class ChatCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (!plugin.getGangsManager().isPlayerInGang(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.CHAT_NOT_IN_GANG.getString(player));
            return;
        }

        boolean hasChat = plugin.getGangsManager().hasChatActivated(player.getUniqueId());

        if (hasChat) {
            player.sendMessage(MessagesConfiguration.CHAT_DISABLED.getString(player));
        } else {
            player.sendMessage(MessagesConfiguration.CHAT_ENABLED.getString(player));
        }

        plugin.getGangsManager().setChatStatus(player.getUniqueId(), !hasChat);
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
