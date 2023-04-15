package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.events.GangInviteEvent;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Optional;

@RequiredArgsConstructor
public class InviteCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Player target = plugin.getServer().getPlayer(args[1]);
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.INVITE_NOT_IN_GANG.getString(player));
            return;
        }
        Gang gang = gangOptional.get();

        if (SettingsConfiguration.PERMISSION_INVITE.getInt() < gang.getMembers().get(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.INVITE_NOT_OWNER.getString(player));
            return;
        }

        if (gangOptional.get().getMembers().size() >= SettingsConfiguration.MAX_MEMBERS_IN_GANG.getInt()) {
            player.sendMessage(MessagesConfiguration.INVITE_MAX_MEMBERS_REACHED.getString(player));
            return;
        }

        if (target == null) {
            player.sendMessage(MessagesConfiguration.INVITE_TARGET_OFFLINE.getString(player)
                    .replace("%target%", args[1]));
            return;
        }

        if (target == player) {
            player.sendMessage(MessagesConfiguration.INVITE_DENIED_SELF.getString(player));
            return;
        }

        if (plugin.getStorage().getAcceptRequests().containsKey(player.getUniqueId())) {
            player.sendMessage(MessagesConfiguration.INVITE_PENDING_REQUEST.getString(player));
            return;
        }

        Optional<Gang> targetGang = plugin.getGangsManager().getGangByMember(target.getUniqueId());

        if (targetGang.isPresent()) {
            if (!targetGang.equals(gangOptional))
                player.sendMessage(MessagesConfiguration.INVITE_TARGET_IN_GANG.getString(player)
                        .replace("%target%", target.getName()));
            else
                player.sendMessage(MessagesConfiguration.INVITE_TARGET_IN_SENDER_GANG.getString(player)
                        .replace("%target%", target.getName()));

            return;
        }

        GangInviteEvent event = new GangInviteEvent(player, target, gangOptional.get());
        Bukkit.getPluginManager().callEvent(event);

        TextComponent component = new TextComponent(MessagesConfiguration.INVITE_SUCCESS_TARGET.getString(player)
                .replace("%player%", player.getName())
                .replace("%time%", SettingsConfiguration.INVITE_TIMEOUT.getString()));

        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" +
                SettingsConfiguration.COMMAND_GANG_NAME.getString() + " " +
                SettingsConfiguration.COMMAND_ACCEPT_NAME.getString() + " " +
                player.getName()));

        plugin.getStorage().getAcceptRequests().put(player.getUniqueId(), target.getUniqueId());

        player.sendMessage(MessagesConfiguration.INVITE_SUCCESS_SENDER.getString(player)
                .replace("%target%", target.getName())
                .replace("%time%", SettingsConfiguration.INVITE_TIMEOUT.getString()));
        target.spigot().sendMessage(component);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!plugin.getStorage().getAcceptRequests().containsKey(player.getUniqueId()) &&
                    !plugin.getStorage().getAcceptRequests().containsValue(target.getUniqueId()))
                return;

            plugin.getStorage().getAcceptRequests().remove(player.getUniqueId(), target.getUniqueId());
            
            player.sendMessage(MessagesConfiguration.INVITE_EXPIRED_SENDER.getString(player).replace("%target%", target.getName()));
            target.sendMessage(MessagesConfiguration.INVITE_EXPIRED_TARGET.getString(target).replace("%player%", player.getName()));
        }, 20L * (long) SettingsConfiguration.INVITE_TIMEOUT.getInt());
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
