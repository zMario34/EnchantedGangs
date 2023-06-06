package tech.zmario.enchantedgangs.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.objects.User;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerConnectionListener implements Listener {

    private final EnchantedGangs plugin;

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (plugin.getStorage().getUser(uuid) != null) return;

        plugin.getSqlManager().loadUser(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getGangsManager().getGangByMember(event.getPlayer().getUniqueId())
                .ifPresent(gang -> plugin.getGangsManager().refreshTeammates(gang));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getStorage().getUsers().remove(event.getPlayer().getUniqueId());

        plugin.getGangsManager().getGangByMember(event.getPlayer().getUniqueId())
                .ifPresent(gang -> plugin.getGangsManager().refreshTeammates(gang));
    }
}
