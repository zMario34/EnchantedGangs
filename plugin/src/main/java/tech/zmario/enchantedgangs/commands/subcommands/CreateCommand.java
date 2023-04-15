package tech.zmario.enchantedgangs.commands.subcommands;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.events.GangCreateEvent;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;
import tech.zmario.enchantedgangs.objects.pojo.GangImpl;
import tech.zmario.enchantedgangs.utils.Utils;

@RequiredArgsConstructor
public class CreateCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String gangName = args[1];

        if (plugin.getGangsManager().isPlayerInGang(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.CREATE_ALREADY_IN_GANG.getString(player));
            return;
        }

        if (plugin.getGangsManager().gangExists(gangName)) {
            player.sendMessage(MessagesConfiguration.CREATE_ALREADY_EXISTS.getString(player));
            return;
        }

        if (gangName.length() >= SettingsConfiguration.MAX_NAME_LENGTH.getInt() || gangName.length() >= 48) {
            player.sendMessage(MessagesConfiguration.CREATE_NAME_TOO_LONG.getString(player));
            return;
        }

        if (Utils.containsIllegals(gangName)) {
            player.sendMessage(MessagesConfiguration.CREATE_CONTAINS_INVALID_CHARACTERS.getString(player));
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Vault") &&
                !plugin.getEconomy().has(player, SettingsConfiguration.CREATE_COST.getInt())) {
            player.sendMessage(MessagesConfiguration.CREATE_NOT_ENOUGH_MONEY.getString(player));
            return;
        }
        Gang gang = new GangImpl(gangName, Maps.newHashMap());

        gang.getMembers().put(player.getUniqueId(), 1);

        gang.setChest(null);
        gang.setOwner(player.getUniqueId());
        gang.setKills(0);
        gang.setBalance(0);

        plugin.getSqlManager().createGang(gangName).whenComplete((aVoid, throwable) -> {
            plugin.getStorage().addGang(gang);
            plugin.getGangsManager().setNewGang(player.getUniqueId(), gangName, 1);

            GangCreateEvent event = new GangCreateEvent(player, gang);
            Bukkit.getPluginManager().callEvent(event);

            if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
                plugin.getEconomy().withdrawPlayer(player, SettingsConfiguration.CREATE_COST.getInt());

            player.sendMessage(MessagesConfiguration.CREATE_SUCCESS.getString(player).replace("%gang%", gangName));
        });
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }
}
