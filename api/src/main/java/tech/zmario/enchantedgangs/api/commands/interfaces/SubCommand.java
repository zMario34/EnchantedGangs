package tech.zmario.enchantedgangs.api.commands.interfaces;

import org.bukkit.command.CommandSender;
public interface SubCommand {
    void execute(CommandSender commandSender, String[] args);

    boolean isPlayerOnly();

    int getMinArgs();

}