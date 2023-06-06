package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.events.GangKickEvent;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Optional;

@RequiredArgsConstructor
public class KickCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.KICK_NOT_IN_GANG.getString(player));
            return;
        }
        Gang gang = gangOptional.get();

        if (SettingsConfiguration.PERMISSION_KICK.getInt() < gang.getMembers().get(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.KICK_NOT_ENOUGH_PERMISSIONS.getString(player));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target;

            if ((target = Bukkit.getPlayer(args[1])) == null) {
                target = Bukkit.getOfflinePlayer(args[1]);

                if (!target.hasPlayedBefore()) {
                    player.sendMessage(MessagesConfiguration.KICK_TARGET_NOT_FOUND.getString(player)
                            .replace("%target%", args[1]));
                    return;
                }
            }

            if (target == player) {
                player.sendMessage(MessagesConfiguration.KICK_DENIED_SELF.getString(player));
                return;
            }
            Optional<Gang> targetGangOptional = plugin.getGangsManager().getGangByMember(target.getUniqueId());

            if (!targetGangOptional.isPresent()) {
                player.sendMessage(MessagesConfiguration.KICK_TARGET_NOT_IN_GANG.getString(player)
                        .replace("%target%", target.getName()));
                return;
            }
            Gang targetGang = targetGangOptional.get();

            if (!targetGang.equals(gang)) {
                player.sendMessage(MessagesConfiguration.KICK_TARGET_NOT_IN_SENDER_GANG.getString(player)
                        .replace("%target%", target.getName()));
                return;
            }
            Player targetPlayer = target.getPlayer();

            GangKickEvent event = new GangKickEvent(player, target, gang);
            Bukkit.getPluginManager().callEvent(event);

            plugin.getGangsManager().removeMember(target.getUniqueId());
            gang.getMembers().remove(target.getUniqueId());

            if (targetPlayer != null) {
                targetPlayer.sendMessage(MessagesConfiguration.KICK_SUCCESS_TARGET.getString(target)
                        .replace("%player%", player.getName())
                        .replace("%gang%", gang.getName()));
            }

            player.sendMessage(MessagesConfiguration.KICK_SUCCESS_SENDER.getString(player)
                    .replace("%target%", target.getName())
                    .replace("%gang%", gang.getName()));

            plugin.getGangsManager().refreshTeammates(gang);
        });
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
