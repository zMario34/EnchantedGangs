package tech.zmario.enchantedgangs.api.objects;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface Gang {
    String getName();

    @NotNull
    UUID getOwner();

    void setOwner(UUID paramUUID);

    Map<UUID, Integer> getMembers();

    Inventory getChest();

    void setChest(Inventory paramInventory);

    int getKills();

    void setKills(int paramInt);

    double getBalance();

    void setBalance(double paramDouble);
}