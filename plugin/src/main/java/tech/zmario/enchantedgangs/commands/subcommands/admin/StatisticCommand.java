package tech.zmario.enchantedgangs.commands.subcommands.admin;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;

import java.util.Optional;

@RequiredArgsConstructor
public class StatisticCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        String gangName = args[1];
        String type = args[2].toLowerCase();
        String statistic = args[3].toLowerCase();
        String value = args[4];

        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByName(gangName);

        if (!gangOptional.isPresent()) {
            sender.sendMessage(MessagesConfiguration.ADMIN_GANG_NOT_FOUND.getString(null)
                    .replace("%gang%", gangName));
            return;
        }

        if (!type.equalsIgnoreCase("set") && !type.equalsIgnoreCase("add") &&
                !type.equalsIgnoreCase("remove")) {
            sender.sendMessage(MessagesConfiguration.ADMIN_STATISTIC_ACTION_NOT_FOUND.getString(null)
                    .replace("%type%", type));
            return;
        }

        if (!statistic.equalsIgnoreCase("kills") && !statistic.equalsIgnoreCase("balance")) {
            sender.sendMessage(MessagesConfiguration.ADMIN_STATISTIC_TYPE_NOT_FOUND.getString(null)
                    .replace("%statistic%", statistic));
            return;
        }

        int valueInt;

        try {
            valueInt = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            sender.sendMessage(MessagesConfiguration.ADMIN_VALUE_NOT_NUMBER.getString(null)
                    .replace("%statistic%", statistic));
            return;
        }
        Gang gang = gangOptional.get();

        switch (statistic) {
            case "kills":
                switch (type) {
                    case "set":
                        gang.setKills(valueInt);
                        break;
                    case "add":
                        gang.setKills(gang.getKills() + valueInt);
                        break;
                    case "remove":
                        gang.setKills(Math.max(gang.getKills() - valueInt, 0));
                        break;
                }
                break;
            case "balance":
                switch (type) {
                    case "set":
                        gang.setBalance(valueInt);
                        break;
                    case "add":
                        gang.setBalance(gang.getBalance() + valueInt);
                        break;
                    case "remove":
                        if (gang.getBalance() - valueInt < 0) {
                            gang.setBalance(0);
                        } else {
                            gang.setBalance(Math.max(gang.getBalance() - valueInt, 0));
                        }
                        break;
                }
                break;
        }

        if (statistic.equals("balance")) {
            plugin.getSqlManager().setBalance(gang.getName(), gang.getBalance());
        } else {
            plugin.getSqlManager().setKills(gang.getName(), gang.getKills());
        }

        sender.sendMessage(MessagesConfiguration.ADMIN_SUCCESS.getString(null)
                .replace("%gang%", gang.getName()));
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public int getMinArgs() {
        return 4;
    }
}
