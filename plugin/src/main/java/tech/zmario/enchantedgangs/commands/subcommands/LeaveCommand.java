package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.events.GangLeaveEvent;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class LeaveCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.LEAVE_NOT_IN_GANG.getString(player));
            return;
        }
        Gang gang = gangOptional.get();

        if (gang.getMembers().get(player.getUniqueId()) == 1) {
            player.sendMessage(MessagesConfiguration.LEAVE_IS_OWNER.getString(player));
            return;
        }
        String gangName = gang.getName();

        plugin.getGangsManager().removeMember(player.getUniqueId());
        gang.getMembers().remove(player.getUniqueId());

        for (UUID memberUuid : plugin.getGangsManager().getGangMembers(gangName)) {
            Player member = Bukkit.getPlayer(memberUuid);

            if (member == null) continue;
            member.sendMessage(MessagesConfiguration.LEAVE_SUCCESS_MEMBERS.getString(player)
                    .replace("%player%", player.getName())
                    .replace("%gang%", gangName));
        }

        GangLeaveEvent event = new GangLeaveEvent(player, gangOptional.get());
        Bukkit.getPluginManager().callEvent(event);

        player.sendMessage(MessagesConfiguration.LEAVE_SUCCESS.getString(player)
                .replace("%gang%", gangName));

        plugin.getGangsManager().refreshTeammates(gang);
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
