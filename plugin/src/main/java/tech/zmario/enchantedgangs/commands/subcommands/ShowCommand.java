package tech.zmario.enchantedgangs.commands.subcommands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.utils.Utils;

import java.util.*;

@RequiredArgsConstructor
public class ShowCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Optional<Gang> gang = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (args.length == 1) {
            if (!gang.isPresent()) {
                player.sendMessage(MessagesConfiguration.SHOW_NOT_IN_GANG.getString(player));
                return;
            }

            sendInfo(player, gang.get());
            return;
        }
        Object target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            target = plugin.getGangsManager().getGangByName(args[1]);
        }

        if (target instanceof Optional && !((Optional<?>) target).isPresent()) {
            player.sendMessage(MessagesConfiguration.SHOW_GANG_NOT_FOUND.getString(player)
                    .replace("%gang%", args[1]));
            return;
        }

        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            Optional<Gang> targetGang = plugin.getGangsManager().getGangByMember(targetPlayer.getUniqueId());

            if (!targetGang.isPresent()) {
                player.sendMessage(MessagesConfiguration.SHOW_TARGET_NOT_IN_GANG.getString(player)
                        .replace("%gang%", targetPlayer.getName()));
                return;
            }

            sendInfo(player, targetGang.get());
        } else if (target != null) {
            sendInfo(player, ((Optional<Gang>) target).get());
        }
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    private void sendInfo(Player player, @NotNull Gang gang) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String string : MessagesConfiguration.SHOW_MESSAGE.getStringList()) {
                List<UUID> gangMembers = new ArrayList<>(gang.getMembers().keySet());

                int onlineMembers = (int) gangMembers.stream()
                        .map(Bukkit::getPlayer)
                        .map(Objects::nonNull)
                        .count();

                string = string.replace("%gang%", gang.getName())
                        .replace("%owner%", Bukkit.getOfflinePlayer(gang.getOwner()).getName())
                        .replace("%money%", Utils.formatMoney(gang.getBalance()))
                        .replace("%online%", onlineMembers + "")
                        .replace("%max-members%", gang.getMembers().size() + "")
                        .replace("%kills%", gang.getKills() + "");

                if (string.contains("%members_")) {
                    int rank = Integer.parseInt(string.split("_")[1].replace("%", ""));

                    StringBuilder members = new StringBuilder();

                    for (UUID memberUuid : gangMembers) {
                        if (gang.getMembers().get(memberUuid) != rank) continue;

                        OfflinePlayer member = Bukkit.getOfflinePlayer(memberUuid);

                        String status = member.isOnline() ?
                                MessagesConfiguration.SHOW_PLACEHOLDER_STATUS_ONLINE.getString(player) :
                                MessagesConfiguration.SHOW_PLACEHOLDER_STATUS_OFFLINE.getString(player);
                        String name = member.getName() == null ? "???" : member.getName();

                        if (gangMembers.get(gangMembers.size() - 1).equals(memberUuid)) {
                            members.append(MessagesConfiguration.SHOW_PLACEHOLDER_MEMBERS_LAST.getString(player)
                                    .replace("%status%", status)
                                    .replace("%member%", name));
                            continue;
                        }

                        members.append(MessagesConfiguration.SHOW_PLACEHOLDER_MEMBERS_DEFAULT.getString(player)
                                .replace("%status%", status)
                                .replace("%member%", name));
                    }

                    if (members.length() == 0) {
                        members.append(MessagesConfiguration.SHOW_PLACEHOLDER_MEMBERS_EMPTY.getString(player));
                    }

                    string = string.replace("%members_" + rank + "%", members.toString());
                }

                player.sendMessage(Utils.colorize(string));
            }
        });
    }
}
