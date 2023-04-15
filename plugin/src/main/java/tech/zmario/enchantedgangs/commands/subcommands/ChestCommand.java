package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Optional;

@RequiredArgsConstructor
public class ChestCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.CHEST_NOT_IN_GANG.getString(player));
            return;
        }
        Gang gang = gangOptional.get();

        if (SettingsConfiguration.PERMISSION_CHEST.getInt() < gang.getMembers().get(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.CHEST_NOT_ENOUGH_PERMISSIONS.getString(player));
            return;
        }

        player.sendMessage(MessagesConfiguration.CHEST_OPENING.getString(player));
        plugin.getGangsManager().tryOpenChest(player);
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }
}
