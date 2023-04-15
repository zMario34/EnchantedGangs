package tech.zmario.enchantedgangs.commands.subcommands.economy;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.util.Optional;

@RequiredArgsConstructor
public class DepositCommand implements SubCommand {

    private final EnchantedGangs plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        if (!gangOptional.isPresent()) {
            player.sendMessage(MessagesConfiguration.DEPOSIT_NOT_IN_GANG.getString(player));
            return;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessagesConfiguration.DEPOSIT_NOT_A_NUMBER.getString(player));
            return;
        }

        if (amount < 1) {
            player.sendMessage(MessagesConfiguration.DEPOSIT_NOT_A_NUMBER.getString(player));
            return;
        }

        if (plugin.getEconomy().getBalance(player) < amount) {
            player.sendMessage(MessagesConfiguration.DEPOSIT_NOT_ENOUGH_MONEY.getString(player));
            return;
        }

        if (amount < SettingsConfiguration.FEATURES_DEPOSIT_MINIMUM_AMOUNT.getInt()) {
            player.sendMessage(MessagesConfiguration.DEPOSIT_LESS_THAN_MINIMUM.getString(player)
                    .replace("%amount%", String.valueOf(SettingsConfiguration.FEATURES_DEPOSIT_MINIMUM_AMOUNT.getInt())));
            return;
        }
        Gang gang = gangOptional.get();

        plugin.getGangsManager().depositMoney(player, gang, amount);

        player.sendMessage(MessagesConfiguration.DEPOSIT_SUCCESS.getString(player).replace("%amount%", String.valueOf(amount)));
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
