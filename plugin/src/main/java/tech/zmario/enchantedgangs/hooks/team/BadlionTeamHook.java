package tech.zmario.enchantedgangs.hooks.team;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.badlion.modapicommon.mods.TeamMarker;
import net.badlion.modapicommon.utility.TeamMemberLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.hooks.team.TeamHook;
import tech.zmario.enchantedgangs.api.objects.Gang;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BadlionTeamHook implements TeamHook {

    private final EnchantedGangs plugin;

    public void sendTeammates(Player player, Gang gang) {
        List<TeamMemberLocation> locationList = Lists.newArrayList();

        if (gang == null) {
            resetTeam(player);
            return;
        }

        for (UUID member : gang.getMembers().keySet()) {
            Player memberPlayer = plugin.getServer().getPlayer(member);
            if (memberPlayer == null) continue;

            Location memberLocation = memberPlayer.getLocation();

            locationList.add(new TeamMemberLocation(member, 3,
                    memberLocation.getX(), memberLocation.getY(), memberLocation.getZ()));
        }

        TeamMarker.sendLocations(player.getUniqueId(), locationList);
    }

    public void refreshTeam(Gang gang) {
        for (UUID member : gang.getMembers().keySet()) {
            Player player = plugin.getServer().getPlayer(member);

            if (player == null) continue;

            sendTeammates(player, gang);
        }
    }
    public void resetTeam(Player player) {
        TeamMarker.sendLocations(player.getUniqueId(), Lists.newArrayList());
    }
}
