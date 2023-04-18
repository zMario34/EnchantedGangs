package tech.zmario.enchantedgangs.api;

import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.hooks.team.TeamHook;
import tech.zmario.enchantedgangs.api.managers.GangsManager;

public interface EnchantedGangsAPI {

    GangsManager getGangsManager();

    void setGangsManager(GangsManager gangsManager);

    void registerSubCommand(String label, SubCommand subCommand);

    boolean registerTeamHook(TeamHook teamHook);

}