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

    Collection<UUID> getGangMembers(@NotNull String paramString);

    @Nullable
    Gang getGangUnsafe(@NotNull String paramString);

    Optional<Gang> getGangByName(@Nullable String paramString);

    Optional<Gang> getGangByMember(@NotNull UUID paramUUID);

    Optional<Gang> getGangByPosition(RankingType paramRankingType, int paramInt);

    @Nullable
    User getUserUnsafe(@NotNull UUID paramUUID);

    Optional<User> getUser(@NotNull UUID paramUUID);

    boolean gangExists(@NotNull String paramString);

    boolean isPlayerInGang(@NotNull UUID paramUUID);

    boolean isOwner(@NotNull UUID paramUUID);

    void addMember(@NotNull String paramString, @NotNull UUID paramUUID, int paramInt);

    void removeMember(@NotNull UUID paramUUID);

    void updateRank(@NotNull UUID paramUUID, int paramInt);

    boolean hasChatActivated(UUID paramUUID);

    void setChatStatus(UUID paramUUID, boolean paramBoolean);

    void tryOpenChest(Player paramPlayer);

    void setChest(Gang paramGang, Inventory paramInventory);

    void setNewGang(UUID paramUUID, String paramString, int paramInt);

    void removeGang(Player paramPlayer, String paramString);

    void refreshTeammates(Gang paramGang);

    void depositMoney(Player paramPlayer, Gang paramGang, int paramInt);

    void withdrawMoney(Player paramPlayer, Gang paramGang, int paramInt);

    void setKills(Gang paramGang, int paramInt);
}