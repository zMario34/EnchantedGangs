package tech.zmario.enchantedgangs.api.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.zmario.enchantedgangs.api.enums.RankingType;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.api.objects.User;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface GangsManager {
    Collection<Gang> getCachedGangs();

    Collection<UUID> getGangMembers(@NotNull String gangName);

    @Nullable
    Gang getGangUnsafe(@NotNull String gangName);

    Optional<Gang> getGangByName(@Nullable String gangName);

    Optional<Gang> getGangByMember(@NotNull UUID uuid);

    Optional<Gang> getGangByPosition(RankingType type, int position);

    @Nullable
    User getUserUnsafe(@NotNull UUID uuid);

    Optional<User> getUser(@NotNull UUID uuid);

    boolean gangExists(@NotNull String name);

    boolean isPlayerInGang(@NotNull UUID uuid);

    boolean isOwner(@NotNull UUID uuid);

    void addMember(@NotNull String gangName, @NotNull UUID uuid, int rank);

    void removeMember(@NotNull UUID uuid);

    void updateRank(@NotNull UUID uuid, int rank);

    boolean hasChatActivated(UUID uuid);

    void setChatStatus(UUID uuid, boolean status);

    void tryOpenChest(Player player);

    void setChest(Gang gang, Inventory inventory);

    void setNewGang(UUID uuid, String name, int rank);

    void removeGang(Player player, String name);

    void refreshTeammates(Gang gang);

    void depositMoney(Player player, Gang gang, int amount);

    void withdrawMoney(Player player, Gang gang, int amount);

    void setKills(Gang gang, int amount);

}