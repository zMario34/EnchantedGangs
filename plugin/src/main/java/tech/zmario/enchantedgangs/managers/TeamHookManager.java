package tech.zmario.enchantedgangs.managers;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.hooks.team.TeamHook;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.hooks.team.BadlionTeamHook;
import tech.zmario.enchantedgangs.hooks.team.LunarTeamHook;

import java.util.List;

@RequiredArgsConstructor
public class TeamHookManager {
    
    private final EnchantedGangs plugin;
    private final List<TeamHook> teamHooks = Lists.newArrayList();
    
    public void enable() {
        plugin.getLogger().info("Enabling team hooks...");

        if (plugin.getServer().getPluginManager().getPlugin("LunarClient-API") != null) {
            registerTeamHook(new LunarTeamHook(plugin));
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("BadlionModAPI") != null) {
            registerTeamHook(new BadlionTeamHook(plugin));
        }
    }
    
    public void refreshTeam(Gang gang) {
        for (TeamHook teamHook : teamHooks) {
            teamHook.refreshTeam(gang);
        }
    }
    
    public void resetTeam(Player player) {
        for (TeamHook teamHook : teamHooks) {
            teamHook.resetTeam(player);
        }
    }

    public boolean registerTeamHook(TeamHook teamHook) {
        plugin.getLogger().info("Registering team hook: " + teamHook.getClass().getSimpleName());
        return teamHooks.add(teamHook);
    }
}
