package tech.zmario.enchantedgangs.hooks;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.enums.RankingType;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class PlaceholderAPIHook extends PlaceholderExpansion implements Relational {

    private final EnchantedGangs plugin;

    private boolean errorSent = false;

    @Override
    public @NotNull String getIdentifier() {
        return "EnchantedGangs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "zMario";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String identifierLowerCase = identifier.toLowerCase();
        Optional<Gang> gangOptional = plugin.getGangsManager().getGangByMember(player.getUniqueId());

        switch (identifierLowerCase) {
            case "gang_name":
                return gangOptional.map(gang -> MessagesConfiguration.PLACEHOLDER_API_GANG_NAME_DEFAULT.getString(player)
                                .replace("%gang%", ChatColor.stripColor(gang.getName())))
                        .orElseGet(() -> MessagesConfiguration.PLACEHOLDER_API_GANG_NAME_NONE.getString(player));
            case "in_gang":
                return gangOptional.isPresent() ? MessagesConfiguration.PLACEHOLDER_API_IN_GANG_TRUE.getString(player)
                        : MessagesConfiguration.PLACEHOLDER_API_IN_GANG_FALSE.getString(player);
            case "gang_balance":
                return gangOptional.map(gang -> MessagesConfiguration.PLACEHOLDER_API_GANG_BALANCE_DEFAULT.getString(player)
                        .replace("%balance%", gang.getBalance() + ""))
                        .orElseGet(() -> MessagesConfiguration.PLACEHOLDER_API_GANG_BALANCE_NONE.getString(player));
            case "gang_kills":
                return gangOptional.map(gang -> MessagesConfiguration.PLACEHOLDER_API_GANG_KILLS_DEFAULT.getString(player)
                                .replace("%kills%", gang.getKills() + ""))
                        .orElseGet(() -> MessagesConfiguration.PLACEHOLDER_API_GANG_KILLS_NONE.getString(player));
        }

        if (identifierLowerCase.startsWith("leaderboard_")) {
            String[] split = identifierLowerCase.split("_");

            String typeString = split[1];
            RankingType type = RankingType.valueOf(split[2].toUpperCase());
            int position = Integer.parseInt(split[3]);

            Optional<Gang> leaderboardOptional = plugin.getGangsManager().getGangByPosition(type, position);

            if (!leaderboardOptional.isPresent()) {
                return MessagesConfiguration.PLACEHOLDER_API_LEADERBOARD_NOT_PRESENT.getString(player);
            }
            Gang gang = leaderboardOptional.get();

            switch (typeString) {
                case "amount":
                    if (type == RankingType.BALANCE) {
                        return gang.getBalance() + "";
                    } else if (type == RankingType.KILLS) {
                        return gang.getKills() + "";
                    }
                case "name":
                    return gang.getName();
            }
        }

        return "";
    }

    @Override
    public String onPlaceholderRequest(Player one, Player two, String identifier) {
        if (one == null || two == null) {
            return "";
        }
        try {
            return CompletableFuture.supplyAsync(() -> {
                if (identifier.equals("gang_name")) {
                    Optional<Gang> firstGang = plugin.getGangsManager().getGangByMember(one.getUniqueId());
                    Optional<Gang> secondGang = plugin.getGangsManager().getGangByMember(two.getUniqueId());

                    if (firstGang.isPresent() && firstGang.equals(secondGang)) {
                        return MessagesConfiguration.PLACEHOLDER_API_RELATIONAL_GANG_NAME_SAME.getString(one)
                                .replace("%gang%", ChatColor.stripColor(firstGang.get().getName()));
                    }

                    return secondGang.map(gang -> MessagesConfiguration.PLACEHOLDER_API_RELATIONAL_GANG_NAME_DIFFERENT.getString(two)
                                    .replace("%gang%", ChatColor.stripColor(gang.getName())))
                            .orElse(MessagesConfiguration.PLACEHOLDER_API_RELATIONAL_GANG_NAME_DIFFERENT_NONE.getString(two));
                }

                return "";
            }).exceptionally(throwable -> {
                if (!errorSent) {
                    errorSent = true;
                    plugin.getLogger().severe("Error while getting placeholder value!");
                    throwable.printStackTrace();
                }
                return "";
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            if (!errorSent) {
                errorSent = true;
                e.printStackTrace();
                plugin.getLogger().severe("Error while getting placeholder value!");
            }

            return "ERROR";
        }
    }
}
