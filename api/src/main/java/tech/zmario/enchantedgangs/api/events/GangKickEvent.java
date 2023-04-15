package tech.zmario.enchantedgangs.api.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import tech.zmario.enchantedgangs.api.objects.Gang;

@Getter
@RequiredArgsConstructor
public class GangKickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final OfflinePlayer kicked;
    private final Gang gang;

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}