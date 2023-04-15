package tech.zmario.enchantedgangs.commands.subcommands;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.enums.RankingType;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class ListCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        final List<Gang> gangs = Lists.newArrayList(plugin.getStorage().getLoadedGangs().values());

        switch (RankingType.valueOf(SettingsConfiguration.LIST_RANKING_TYPE.getString().toUpperCase())) {
            case BALANCE:
                gangs.sort(Comparator.comparing(Gang::getBalance).reversed());
                break;
            case KILLS:
                gangs.sort(Comparator.comparing(Gang::getKills).reversed());
                break;
        }

        int page = 0;

        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]) - 1;

                if (page < 0) {
                    player.sendMessage(MessagesConfiguration.LIST_NOT_A_NUMBER.getString(player));
                    return;
                }

            } catch (NumberFormatException e) {
                player.sendMessage(MessagesConfiguration.LIST_NOT_A_NUMBER.getString(player));
                return;
            }
        }

        if (gangs.isEmpty()) {
            player.sendMessage(MessagesConfiguration.LIST_EMPTY.getString(player));
            return;
        }

        int start = page * SettingsConfiguration.LIST_MAX_SIZE.getInt();
        int end = start + SettingsConfiguration.LIST_MAX_SIZE.getInt();

        if (end > gangs.size()) {
            end = gangs.size();
        }

        int maxPage = gangs.size() / SettingsConfiguration.LIST_MAX_SIZE.getInt() + 1;

        if (page >= maxPage) {
            player.sendMessage(MessagesConfiguration.LIST_PAGE_NOT_EXIST.getString(player)
                    .replace("%page%", String.valueOf(page + 1)).replace("%max-page%", String.valueOf(maxPage)));
            return;
        }

        player.sendMessage(MessagesConfiguration.LIST_HEADER.getString(player)
                .replace("%page%", String.valueOf(page + 1)).replace("%max-page%", String.valueOf(maxPage)));

        for (int i = start; i < end; i++) {
            player.sendMessage(MessagesConfiguration.LIST_LINE.getString(player)
                    .replace("%index%", String.valueOf(i + 1))
                    .replace("%gang%", gangs.get(i).getName())
                    .replace("%balance%", String.valueOf(gangs.get(i).getBalance()))
                    .replace("%kills%", String.valueOf(gangs.get(i).getKills())));
        }
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 0;
    }
}
