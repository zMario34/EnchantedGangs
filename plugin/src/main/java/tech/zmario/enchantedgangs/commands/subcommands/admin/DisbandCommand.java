package tech.zmario.enchantedgangs.commands.subcommands.admin;

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
        String gangName = args[1];

        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByName(gangName);

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.ADMIN_GANG_NOT_FOUND.getString(player)
                    .replace("%gang%", gangName));
            return;
        }
        Gang gang = gangOptional.get();

        plugin.getGangsManager().removeGang(player, gang.getName());
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }
}
