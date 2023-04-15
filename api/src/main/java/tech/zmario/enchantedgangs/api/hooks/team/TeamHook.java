package tech.zmario.enchantedgangs.api.hooks.team;

import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.api.objects.Gang;

public interface TeamHook {

    void sendTeammates(Player paramPlayer, Gang paramGang);

    void refreshTeam(Gang paramGang);

    void resetTeam(Player paramPlayer);

}