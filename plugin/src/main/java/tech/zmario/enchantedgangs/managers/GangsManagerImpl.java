package tech.zmario.enchantedgangs.managers;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.enums.RankingType;
import tech.zmario.enchantedgangs.api.events.GangDisbandEvent;
import tech.zmario.enchantedgangs.api.managers.GangsManager;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.api.objects.User;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;
import tech.zmario.enchantedgangs.utils.InventoryUtils;

import java.util.*;

@RequiredArgsConstructor
public class GangsManagerImpl implements GangsManager {

    private final EnchantedGangs plugin;

    @Override
    public Collection<Gang> getCachedGangs() {
        return plugin.getStorage().getLoadedGangs().values();
    }

    @Override
    public Collection<UUID> getGangMembers(@NotNull String gangName) {
        Objects.requireNonNull(gangName, "gangName");
        Optional<Gang> gangOptional = getGangByName(gangName);

        if (!gangOptional.isPresent()) {
            return Collections.emptyList();
        }

        return gangOptional.get().getMembers().keySet();
    }

    @Override
    public @Nullable Gang getGangUnsafe(@NotNull String name) {
        Objects.requireNonNull(name, "name cannot be null! (GangsManagerImpl#getGangUnsafe)");
        return plugin.getStorage().getGang(name);
    }

    @Override
    public Optional<Gang> getGangByName(@Nullable String name) {
        if (name == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(getGangUnsafe(name));
    }

    @Override
    public Optional<Gang> getGangByMember(@NotNull UUID memberUUID) {
        Objects.requireNonNull(memberUUID, "memberUUID cannot be null! (GangsManagerImpl#getGangByMember)");
        User user = getUserUnsafe(memberUUID);

        if (user == null) {
            return Optional.empty();
        }

        return getGangByName(user.getGangName());
    }

    @Override
    public Optional<Gang> getGangByPosition(RankingType rankingType, int position) {
        return Optional.ofNullable(plugin.getStorage().getGang(rankingType, position));
    }

    @Override
    public @Nullable User getUserUnsafe(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid cannot be null! (GangsManagerImpl#getUserUnsafe)");
        return plugin.getStorage().getUser(uuid);
    }

    @Override
    public Optional<User> getUser(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid cannot be null! (GangsManagerImpl#getUser)");
        return Optional.ofNullable(getUserUnsafe(uuid));
    }

    @Override
    public boolean gangExists(@NotNull String name) {
        Objects.requireNonNull(name, "name cannot be null! (GangsManagerImpl#gangExists)");

        return getGangByName(name).isPresent();
    }

    @Override
    public boolean isPlayerInGang(@NotNull UUID playerUUID) {
        Objects.requireNonNull(playerUUID, "playerUUID cannot be null! (GangsManagerImpl#isPlayerInGang)");

        return getGangByMember(playerUUID).isPresent();
    }

    @Override
    public boolean isOwner(@NotNull UUID playerUUID) {
        return getCachedGangs().stream()
                .anyMatch(gang -> gang.getOwner().equals(playerUUID));
    }

    @Override
    public void addMember(@NotNull String gangName, @NotNull UUID playerUUID, int rank) {
        Objects.requireNonNull(gangName, "gangName cannot be null! (GangsManagerImpl#addMember)");
        Objects.requireNonNull(playerUUID, "playerUUID cannot be null! (GangsManagerImpl#addMember)");
        if (rank < 0) throw new IllegalArgumentException("rank cannot be less than 0! (GangsManagerImpl#addMember)");

        getGangByName(gangName).ifPresent(gang -> {
            gang.getMembers().put(playerUUID, rank);
            plugin.getSqlManager().addGangMember(playerUUID, gang);

            Player player = plugin.getServer().getPlayer(playerUUID);

            if (player != null) {
                refreshTeammates(gang);
            }
        });
    }

    @Override
    public void removeMember(@NotNull UUID playerUUID) {
        Objects.requireNonNull(playerUUID, "playerUUID cannot be null! (GangsManagerImpl#removeMember)");
        Optional<User> userOptional = getUser(playerUUID);

        if (!userOptional.isPresent()) return;
        User user = userOptional.get();

        getGangByName(user.getGangName()).ifPresent(gang -> {
            plugin.getSqlManager().removeGangMember(playerUUID);

            Player player = plugin.getServer().getPlayer(playerUUID);

            if (player != null) {
                plugin.getTeamHookManager().resetTeam(player);
            }

            user.setGangName(null);
        });
    }

    @Override
    public void updateRank(@NotNull UUID playerUUID, int rank) {
        Objects.requireNonNull(playerUUID, "playerUUID cannot be null! (GangsManagerImpl#updateMember)");
        if (rank < 1) throw new IllegalArgumentException("rank cannot be less than 1! (GangsManagerImpl#updateMember)");

        getGangByMember(playerUUID).ifPresent(gang -> {
            gang.getMembers().put(playerUUID, rank);
            plugin.getSqlManager().setMemberRank(playerUUID, rank);
        });
    }

    @Override
    public boolean hasChatActivated(UUID uuid) {
        Optional<User> userOptional = getUser(uuid);

        return userOptional.map(User::isGangChatEnabled).orElse(false);

    }

    @Override
    public void setChatStatus(UUID uuid, boolean status) {
        getUser(uuid).ifPresent(user -> user.setGangChatEnabled(status));
    }

    @Override
    public void tryOpenChest(Player player) {
        getGangByMember(player.getUniqueId()).ifPresent(gang -> {
            if (gang.getChest() == null) {
                Inventory inventory = Bukkit.createInventory(null,
                        SettingsConfiguration.FEATURES_CHEST_SIZE.getInt(),
                        MessagesConfiguration.GANG_CHEST_TITLE.getString(player));

                gang.setChest(inventory);
            }

            player.openInventory(gang.getChest());
        });
    }


    @Override
    public void setChest(Gang gang, Inventory inventory) {
        gang.setChest(inventory);
        plugin.getSqlManager().setChest(gang.getName(), InventoryUtils.toBase64(inventory.getContents()));
    }

    @Override
    public void setNewGang(UUID uuid, String gangName, int rank) {
        plugin.getSqlManager().setPlayerGang(uuid, gangName, rank)
                .thenAccept(v -> getUser(uuid).ifPresent(user -> user.setGangName(gangName)));
    }

    @Override
    public void removeGang(Player player, String gangName) {
        getGangByName(gangName).ifPresent(gang -> {
            GangDisbandEvent event = new GangDisbandEvent(player, gang);
            plugin.getServer().getPluginManager().callEvent(event);

            String disbandMessage = MessagesConfiguration.DISBAND_SUCCESS.getString(player)
                    .replace("%gang%", gangName)
                    .replace("%player%", player.getName());

            for (UUID member : gang.getMembers().keySet()) {
                Player memberPlayer = plugin.getServer().getPlayer(member);
                if (memberPlayer == null) continue;

                memberPlayer.sendMessage(disbandMessage);
            }

            gang.getMembers().keySet().forEach(this::removeMember);

            plugin.getSqlManager().removeGang(gangName);
            plugin.getStorage().removeGang(gang);
        });
    }

    @Override
    public void refreshTeammates(Gang gang) {
        plugin.getTeamHookManager().refreshTeam(gang);
    }

    @Override
    public void depositMoney(Player player, Gang gang, int amount) {
        gang.setBalance(gang.getBalance() + amount);

        plugin.getSqlManager().setBalance(gang.getName(), gang.getBalance());
        plugin.getEconomy().withdrawPlayer(player, amount);
    }

    @Override
    public void withdrawMoney(Player player, Gang gang, int amount) {
        gang.setBalance(gang.getBalance() - amount);

        plugin.getSqlManager().setBalance(gang.getName(), gang.getBalance());
        plugin.getEconomy().depositPlayer(player, amount);
    }

    @Override
    public void setKills(Gang gang, int kills) {
        gang.setKills(kills);
        plugin.getSqlManager().setKills(gang.getName(), kills);
    }
}
