package tech.zmario.enchantedgangs.api.objects;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface Gang {

    String getName();

    @NotNull
    UUID getOwner();

    void setOwner(UUID uuid);

    Map<UUID, Integer> getMembers();

    Inventory getChest();

    void setName(String name);

    void setChest(Inventory inventory);

    int getKills();

    void setKills(int amount);

    double getBalance();

    void setBalance(double amount);
}