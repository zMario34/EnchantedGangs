package tech.zmario.enchantedgangs.commands.subcommands;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.events.GangJoinEvent;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AcceptCommand implements SubCommand {

    private final EnchantedGangs plugin;

    private final Map<UUID, Long> cooldowns = Maps.newHashMap();

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        long cooldown = cooldowns.getOrDefault(player.getUniqueId(), 0L);

        if (cooldown > System.currentTimeMillis()) {
            player.sendMessage(MessagesConfiguration.ACCEPT_ON_COOLDOWN.getString(player));
            return;
        }
        int acceptCooldown = SettingsConfiguration.ACCEPT_COOLDOWN.getInt();

        if (acceptCooldown > 0) {
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (1000L * acceptCooldown));
        }

        if (plugin.getGangsManager().isPlayerInGang(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.ACCEPT_ALREADY_IN_GANG.getString(player));
            return;
        }

        if (!plugin.getStorage().hasReceivedAcceptRequest(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.ACCEPT_NO_REQUESTS.getString(player));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            player.sendMessage(MessagesConfiguration.ACCEPT_TARGET_OFFLINE.getString(player)
                    .replace("%target%", args[1]));
            return;
        }

        if (!plugin.getStorage().getAcceptRequests().get(target.getUniqueId()).equals(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.ACCEPT_NOT_INVITED.getString(player)
                    .replace("%target%", target.getName()));
            return;
        }
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(target.getUniqueId());

        plugin.getStorage().removeAcceptRequest(target.getUniqueId(), player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.ACCEPT_TARGET_NOT_IN_GANG.getString(player)
                    .replace("%target%", target.getName()));
            return;
        }

        Gang gang = gangOptional.get();

        if (gang.getMembers().size() >= SettingsConfiguration.MAX_MEMBERS_IN_GANG.getInt()) {
            player.sendMessage(MessagesConfiguration.ACCEPT_MAX_MEMBERS_REACHED.getString(player)
                    .replace("%target%", target.getName())
                    .replace("%gang%", gang.getName()));
            return;
        }

        String message = MessagesConfiguration.ACCEPT_SUCCESS_MEMBERS.getString(player)
                .replace("%player%", player.getName())
                .replace("%target%", target.getName())
                .replace("%gang%", gang.getName());

        GangJoinEvent event = new GangJoinEvent(player, target, gang);
        Bukkit.getPluginManager().callEvent(event);

        plugin.getGangsManager().getUser(player.getUniqueId()).ifPresent(user -> user.setGangName(gang.getName()));
        plugin.getGangsManager().addMember(gang.getName(), player.getUniqueId(), plugin.getStorage().getLowestRank());

        player.sendMessage(MessagesConfiguration.ACCEPT_SUCCESS_SENDER.getString(player)
                .replace("%target%", target.getName())
                .replace("%gang%", gang.getName()));

        for (UUID memberUuid : plugin.getGangsManager().getGangMembers(gang.getName())) {
            if (memberUuid.equals(player.getUniqueId())) continue;
            Player member = Bukkit.getPlayer(memberUuid);

            if (member == null) continue;

            member.sendMessage(message);
        }
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
