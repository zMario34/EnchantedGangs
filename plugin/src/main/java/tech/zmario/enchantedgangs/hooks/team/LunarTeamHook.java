package tech.zmario.enchantedgangs.hooks.team;

import com.google.common.collect.Maps;
import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTeammates;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.hooks.team.TeamHook;
import tech.zmario.enchantedgangs.api.objects.Gang;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class LunarTeamHook implements TeamHook {

    private final EnchantedGangs plugin;
    private final LunarClientAPI lunar = LunarClientAPI.getInstance();

    public void sendTeammates(Player player, Gang gang) {
        Map<UUID, Map<String, Double>> members = Maps.newHashMap();
        Map<String, Double> locationMap = Maps.newHashMap();

        resetTeam(player);

        if (gang == null) return;

        for (UUID member : gang.getMembers().keySet()) {
            Player memberPlayer = plugin.getServer().getPlayer(member);

            if (memberPlayer == null || member.equals(player.getUniqueId())) continue;
            Location memberLocation = memberPlayer.getLocation();

            locationMap.clear();
            locationMap.put("x", memberLocation.getX());
            locationMap.put("y", memberLocation.getY());
            locationMap.put("z", memberLocation.getZ());

            members.put(member, locationMap);
        }

        lunar.sendTeammates(player, new LCPacketTeammates(player.getUniqueId(), 5L, members));
    }

    public void refreshTeam(Gang gang) {
        for (UUID member : gang.getMembers().keySet()) {
            Player player = plugin.getServer().getPlayer(member);

            if (player == null) continue;

            sendTeammates(player, gang);
        }
    }
    public void resetTeam(Player player) {
        lunar.sendTeammates(player, new LCPacketTeammates(player.getUniqueId(), 5L, Maps.newHashMap()));
    }
}
