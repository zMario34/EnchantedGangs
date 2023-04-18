package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.events.GangPromoteEvent;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PromoteCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.PROMOTE_NOT_IN_GANG.getString(player));
            return;
        }
        Gang gang = gangOptional.get();

        if (SettingsConfiguration.PERMISSION_PROMOTE.getInt() < gang.getMembers().get(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.PROMOTE_NOT_ENOUGH_PERMISSIONS.getString(player));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            if (!target.hasPlayedBefore()) {
                player.sendMessage(MessagesConfiguration.PROMOTE_TARGET_NOT_FOUND.getString(player)
                        .replace("%target%", args[1]));
                return;
            }

            if (target == player) {
                player.sendMessage(MessagesConfiguration.PROMOTE_DENIED_SELF.getString(player));
                return;
            }
            Optional<Gang> targetGang = plugin.getGangsManager().getGangByMember(target.getUniqueId());

            if (!targetGang.isPresent()) {
                player.sendMessage(MessagesConfiguration.PROMOTE_TARGET_NOT_IN_GANG.getString(player)
                        .replace("%target%", args[1]));
                return;
            }

            if (!Objects.equals(targetGang, gangOptional)) {
                player.sendMessage(MessagesConfiguration.PROMOTE_TARGET_NOT_IN_SENDER_GANG.getString(player)
                        .replace("%target%", args[1]));
                return;
            }
            int rank = gang.getMembers().get(target.getUniqueId());

            if (target.getUniqueId().equals(gang.getOwner()) || rank == 1) {
                player.sendMessage(MessagesConfiguration.PROMOTE_TARGET_IS_OWNER.getString(player)
                        .replace("%target%", args[1]));
                return;
            }

            String gangName = gang.getName();

            int newRank = rank - 1;

            if (!plugin.getStorage().rankExists(newRank)) {
                player.sendMessage(MessagesConfiguration.PROMOTE_TARGET_IS_RANKED.getString(player));
                return;
            }

            if (newRank == 1) {
                gang.setOwner(target.getUniqueId());
                plugin.getGangsManager().updateRank(player.getUniqueId(), newRank + 1);
            }

            plugin.getGangsManager().updateRank(target.getUniqueId(), newRank);

            GangPromoteEvent event = new GangPromoteEvent(player, target, gang);
            Bukkit.getPluginManager().callEvent(event);

            player.sendMessage(MessagesConfiguration.PROMOTE_SUCCESS_SENDER.getString(target)
                    .replace("%gang%", gangName)
                    .replace("%target%", target.getName())
                    .replace("%rank%", plugin.getStorage().getRankName(newRank)));

            for (UUID memberUuid : plugin.getGangsManager().getGangMembers(gangName)) {
                if (player.getUniqueId().equals(memberUuid)) continue;
                Player member = Bukkit.getPlayer(memberUuid);

                if (member == null) continue;

                member.sendMessage(MessagesConfiguration.PROMOTE_SUCCESS_MEMBERS.getString(target)
                        .replace("%gang%", gangName)
                        .replace("%target%", target.getName())
                        .replace("%rank%", plugin.getStorage().getRankName(newRank)));
            }
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
