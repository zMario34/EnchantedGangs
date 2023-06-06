package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.api.objects.User;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class RenameCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.RENAME_NOT_IN_GANG.getString(player));
            return;
        }
        Gang gang = gangOptional.get();

        if (SettingsConfiguration.PERMISSION_RENAME.getInt() < gang.getMembers().get(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.RENAME_NOT_ENOUGH_PERMISSIONS.getString(player));
            return;
        }
        String name = args[1];

        if (name.length() > SettingsConfiguration.MAX_NAME_LENGTH.getInt()) {
            player.sendMessage(MessagesConfiguration.RENAME_NAME_TOO_LONG.getString(player));
            return;
        }

        if (plugin.getStorage().getLoadedGangs().containsKey(name)) {
            player.sendMessage(MessagesConfiguration.RENAME_NAME_ALREADY_EXISTS.getString(player));
            return;
        }
        String oldName = gang.getName();

        gang.setName(name);
        plugin.getSqlManager().setGangName(oldName, name);

        for (Map.Entry<UUID, Integer> entry : gang.getMembers().entrySet()) {
            UUID targetUuid = entry.getKey();
            int rank = entry.getValue();

            plugin.getSqlManager().setPlayerGang(targetUuid, gang.getName(), rank);
            plugin.getGangsManager().getUser(targetUuid).ifPresent(user -> user.setGangName(gang.getName()));
        }

        plugin.getStorage().getLoadedGangs().remove(oldName);
        plugin.getStorage().getLoadedGangs().put(name, gang);

        player.sendMessage(MessagesConfiguration.RENAME_SUCCESS.getString(player)
                .replace("%old-name%", oldName)
                .replace("%new-name%", name));
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
