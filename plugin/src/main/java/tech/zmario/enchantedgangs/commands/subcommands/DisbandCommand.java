package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;

import java.util.Optional;

@RequiredArgsConstructor
public class DisbandCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.DISBAND_NOT_IN_GANG.getString(player));
            return;
        }
        Gang gang = gangOptional.get();

        if (gang.getMembers().get(player.getUniqueId()) != 1) {
            player.sendMessage(MessagesConfiguration.DISBAND_NOT_OWNER.getString(player));
            return;
        }
        String gangName = gang.getName();

        plugin.getGangsManager().removeGang(player, gangName);
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
