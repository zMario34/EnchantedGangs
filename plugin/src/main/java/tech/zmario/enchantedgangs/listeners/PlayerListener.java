package tech.zmario.enchantedgangs.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.events.GangChatEvent;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final EnchantedGangs plugin;

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!SettingsConfiguration.FEATURES_KILLS_ENABLED.getBoolean() || event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();

        if (killer.getUniqueId().equals(event.getEntity().getUniqueId())) return;

        plugin.getGangsManager().getGangByMember(killer.getUniqueId())
                .ifPresent(gang -> plugin.getGangsManager().setKills(gang, gang.getKills() + 1));
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (SettingsConfiguration.FRIENDLY_FIRE.getBoolean() ||
                !(event.getDamager() instanceof Player) && !(event.getDamager() instanceof Projectile) ||
                !(event.getEntity() instanceof Player)) return;
        Player damaged = (Player) event.getEntity();
        Player damager = event.getDamager() instanceof Player ? (Player) event.getDamager() :
                (Player) ((Projectile) event.getDamager()).getShooter();

        if (damager == null || damager.getUniqueId().equals(damaged.getUniqueId()) ||
                damaged.hasMetadata("npc") || damager.hasMetadata("npc")) return;

        Optional<Gang> damagerGang = plugin.getGangsManager().getGangByMember(damager.getUniqueId());
        Optional<Gang> damagedGang = plugin.getGangsManager().getGangByMember(damaged.getUniqueId());

        if (damagerGang.isPresent() && damagedGang.isPresent() && damagerGang.get().equals(damagedGang.get()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        InventoryView view = event.getView();

        if (view.getType() != InventoryType.CHEST ||
                !view.getTitle().equals(MessagesConfiguration.GANG_CHEST_TITLE.getString(player)))
            return;

        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) return;
        Gang gang = gangOptional.get();

        plugin.getGangsManager().setChest(gang, inventory);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            if (plugin.getGangsManager().hasChatActivated(player.getUniqueId()))
                plugin.getGangsManager().setChatStatus(player.getUniqueId(), false);
            return;
        }

        if (!plugin.getGangsManager().hasChatActivated(player.getUniqueId())) return;
        Gang gang = gangOptional.get();

        GangChatEvent chatEvent = new GangChatEvent(player, gang);
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(chatEvent));

        event.getRecipients().clear();
        event.getRecipients().addAll(gang.getMembers().keySet()
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        event.setFormat(MessagesConfiguration.CHAT_FORMAT.getString(player)
                .replace("%gang%", gang.getName())
                .replace("%player%", player.getName())
                .replace("%message%", event.getMessage())
                .replace("%", "%%"));
    }
}
