package tech.zmario.enchantedgangs.objects.pojo;

import lombok.Data;
import org.bukkit.inventory.Inventory;
import tech.zmario.enchantedgangs.api.objects.Gang;

import java.util.Map;
import java.util.UUID;

@Data
public class GangImpl implements Gang {

    private String name;
    private final Map<UUID, Integer> members;

    private UUID owner;
    private Inventory chest;

    private int kills = 0;
    private double balance;

    public GangImpl(String name, Map<UUID, Integer> members) {
        this.name = name;
        this.members = members;
    }
}
