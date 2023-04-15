package tech.zmario.enchantedgangs.enums;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

public enum MessagesConfiguration {

    NO_CONSOLE("no-console"),
    NO_PERMISSION("no-permission"),
    RELOAD_SUCCESS("reload-success"),
    SUBCOMMAND_USAGE("commands.subcommand-usage"),
    ADMIN_SUBCOMMAND_USAGE("commands.admin-subcommand-usage"),
    GANG_CHEST_TITLE("chest-title"),
    CHAT_FORMAT("gang-chat-format"),

    PLACEHOLDER_API_GANG_NAME_DEFAULT("placeholder-api.gang_name.default"),
    PLACEHOLDER_API_GANG_NAME_NONE("placeholder-api.gang_name.none"),
    PLACEHOLDER_API_IN_GANG_TRUE("placeholder-api.in_gang.true"),
    PLACEHOLDER_API_IN_GANG_FALSE("placeholder-api.in_gang.false"),
    PLACEHOLDER_API_GANG_BALANCE_DEFAULT("placeholder-api.gang_balance.default"),
    PLACEHOLDER_API_GANG_BALANCE_NONE("placeholder-api.gang_balance.none"),

    PLACEHOLDER_API_GANG_KILLS_DEFAULT("placeholder-api.gang_kills.default"),
    PLACEHOLDER_API_GANG_KILLS_NONE("placeholder-api.gang_kills.none"),

    PLACEHOLDER_API_LEADERBOARD_NOT_PRESENT("placeholder-api.leaderboard.not-present"),

    PLACEHOLDER_API_RELATIONAL_GANG_NAME_SAME("placeholder-api.relational.gang_name.same"),
    PLACEHOLDER_API_RELATIONAL_GANG_NAME_DIFFERENT_NONE("placeholder-api.relational.gang_name.different-none"),
    PLACEHOLDER_API_RELATIONAL_GANG_NAME_DIFFERENT("placeholder-api.relational.gang_name.different"),

    // Commands
    CREATE_ALREADY_IN_GANG("commands.create.already-in-gang"),
    CREATE_NAME_TOO_LONG("commands.create.name-too-long"),
    CREATE_ALREADY_EXISTS("commands.create.already-exists"),
    CREATE_CONTAINS_INVALID_CHARACTERS("commands.create.contains-invalid-characters"),
    CREATE_NOT_ENOUGH_MONEY("commands.create.not-enough-money"),
    CREATE_SUCCESS("commands.create.success"),

    INVITE_NOT_IN_GANG("commands.invite.not-in-gang"),
    INVITE_NOT_OWNER("commands.invite.not-owner"),
    INVITE_MAX_MEMBERS_REACHED("commands.invite.max-members-reached"),
    INVITE_TARGET_OFFLINE("commands.invite.target-offline"),
    INVITE_DENIED_SELF("commands.invite.denied-self"),
    INVITE_PENDING_REQUEST("commands.invite.pending-request"),
    INVITE_TARGET_IN_GANG("commands.invite.target-in-gang"),
    INVITE_TARGET_IN_SENDER_GANG("commands.invite.target-in-sender-gang"),
    INVITE_SUCCESS_SENDER("commands.invite.success-sender"),
    INVITE_SUCCESS_TARGET("commands.invite.success-target"),
    INVITE_EXPIRED_SENDER("commands.invite.expired-sender"),
    INVITE_EXPIRED_TARGET("commands.invite.expired-target"),

    KICK_NOT_IN_GANG("commands.kick.not-in-gang"),
    KICK_NOT_ENOUGH_PERMISSIONS("commands.kick.not-enough-permissions"),
    KICK_TARGET_NOT_FOUND("commands.kick.target-not-found"),
    KICK_DENIED_SELF("commands.kick.denied-self"),
    KICK_TARGET_NOT_IN_GANG("commands.kick.target-not-in-gang"),
    KICK_TARGET_NOT_IN_SENDER_GANG("commands.kick.target-not-in-sender-gang"),
    KICK_SUCCESS_SENDER("commands.kick.success-sender"),
    KICK_SUCCESS_TARGET("commands.kick.success-target"),

    LEAVE_NOT_IN_GANG("commands.leave.not-in-gang"),
    LEAVE_IS_OWNER("commands.leave.is-owner"),
    LEAVE_SUCCESS("commands.leave.success"),
    LEAVE_SUCCESS_MEMBERS("commands.leave.success-members"),

    DISBAND_NOT_IN_GANG("commands.disband.not-in-gang"),
    DISBAND_NOT_OWNER("commands.disband.not-owner"),
    DISBAND_SUCCESS("commands.disband.success"),

    ACCEPT_ON_COOLDOWN("commands.accept.on-cooldown"),
    ACCEPT_ALREADY_IN_GANG("commands.accept.already-in-gang"),
    ACCEPT_NO_REQUESTS("commands.accept.no-requests"),
    ACCEPT_TARGET_OFFLINE("commands.accept.target-offline"),
    ACCEPT_NOT_INVITED("commands.accept.not-invited"),
    ACCEPT_TARGET_NOT_IN_GANG("commands.accept.target-not-in-gang"),
    ACCEPT_MAX_MEMBERS_REACHED("commands.accept.max-members-reached"),
    ACCEPT_SUCCESS_SENDER("commands.accept.success-sender"),
    ACCEPT_SUCCESS_MEMBERS("commands.accept.success-members"),

    PROMOTE_NOT_IN_GANG("commands.promote.not-in-gang"),
    PROMOTE_NOT_ENOUGH_PERMISSIONS("commands.promote.not-enough-permissions"),
    PROMOTE_TARGET_NOT_FOUND("commands.promote.target-not-found"),
    PROMOTE_DENIED_SELF("commands.promote.denied-self"),
    PROMOTE_TARGET_NOT_IN_GANG("commands.promote.target-not-in-gang"),
    PROMOTE_TARGET_NOT_IN_SENDER_GANG("commands.promote.target-not-in-sender-gang"),
    PROMOTE_TARGET_IS_RANKED("commands.promote.target-is-ranked"),
    PROMOTE_TARGET_IS_OWNER("commands.promote.target-is-owner"),
    PROMOTE_SUCCESS_SENDER("commands.promote.success-sender"),
    PROMOTE_SUCCESS_MEMBERS("commands.promote.success-members"),

    DEMOTE_NOT_IN_GANG("commands.demote.not-in-gang"), // CHANGED / ADDED
    DEMOTE_NOT_ENOUGH_PERMISSIONS("commands.demote.not-enough-permissions"),
    DEMOTE_TARGET_NOT_FOUND("commands.demote.target-not-found"),
    DEMOTE_DENIED_SELF("commands.demote.denied-self"),
    DEMOTE_TARGET_NOT_IN_GANG("commands.demote.target-not-in-gang"),
    DEMOTE_TARGET_NOT_IN_SENDER_GANG("commands.demote.target-not-in-sender-gang"),
    DEMOTE_TARGET_IS_OWNER("commands.demote.target-is-owner"),
    DEMOTE_TARGET_IS_LOWEST_RANK("commands.demote.target-is-lowest-rank"),
    DEMOTE_SUCCESS_SENDER("commands.demote.success-sender"),
    DEMOTE_SUCCESS_MEMBERS("commands.demote.success-members"),

    SHOW_NOT_IN_GANG("commands.show.not-in-gang"),
    SHOW_TARGET_NOT_IN_GANG("commands.show.target-not-in-gang"),
    SHOW_GANG_NOT_FOUND("commands.show.gang-not-found"),

    SHOW_PLACEHOLDER_STATUS_ONLINE("commands.show.placeholders.status.online"),
    SHOW_PLACEHOLDER_STATUS_OFFLINE("commands.show.placeholders.status.offline"),

    SHOW_PLACEHOLDER_MEMBERS_DEFAULT("commands.show.placeholders.members.default"),
    SHOW_PLACEHOLDER_MEMBERS_LAST("commands.show.placeholders.members.last"),
    SHOW_PLACEHOLDER_MEMBERS_EMPTY("commands.show.placeholders.members.empty"),

    SHOW_PLACEHOLDER_ALLIES_DEFAULT("commands.show.placeholders.allies.default"),
    SHOW_PLACEHOLDER_ALLIES_LAST("commands.show.placeholders.allies.last"),
    SHOW_PLACEHOLDER_ALLIES_EMPTY("commands.show.placeholders.allies.empty"),

    SHOW_MESSAGE("commands.show.message"),

    LIST_EMPTY("commands.list.empty"),
    LIST_PAGE_NOT_EXIST("commands.list.page-not-exist"),
    LIST_HEADER("commands.list.header"),
    LIST_LINE("commands.list.line"),
    LIST_NOT_A_NUMBER("commands.list.not-a-number"),

    CHAT_NOT_IN_GANG("commands.chat.not-in-gang"),
    CHAT_ENABLED("commands.chat.enabled"),
    CHAT_DISABLED("commands.chat.disabled"),

    DEPOSIT_NOT_IN_GANG("commands.deposit.not-in-gang"),
    DEPOSIT_NOT_A_NUMBER("commands.deposit.not-a-number"),
    DEPOSIT_NOT_ENOUGH_MONEY("commands.deposit.not-enough-money"),
    DEPOSIT_LESS_THAN_MINIMUM("commands.deposit.less-than-minimum"),
    DEPOSIT_SUCCESS("commands.deposit.success"),

    WITHDRAW_NOT_IN_GANG("commands.withdraw.not-in-gang"),
    WITHDRAW_NOT_A_NUMBER("commands.withdraw.not-a-number"),
    WITHDRAW_NOT_ENOUGH_MONEY("commands.withdraw.not-enough-money"),
    WITHDRAW_LESS_THAN_MINIMUM("commands.withdraw.less-than-minimum"),
    WITHDRAW_SUCCESS("commands.withdraw.success"),

    CHEST_NOT_IN_GANG("commands.chest.not-in-gang"),
    CHEST_NOT_ENOUGH_PERMISSIONS("commands.chest.not-enough-permissions"),
    CHEST_OPENING("commands.chest.opening"),

    // Admin
    ADMIN_GANG_NOT_FOUND("commands.admin.gang-not-found"),
    ADMIN_SUCCESS("commands.admin.success"),
    ADMIN_STATISTIC_ACTION_NOT_FOUND("commands.admin.statistic.action-not-found"),
    ADMIN_STATISTIC_TYPE_NOT_FOUND("commands.admin.statistic.type-not-found"),
    ADMIN_VALUE_NOT_NUMBER("commands.admin.value-not-number"),

    HELP("commands.help"),
    ADMIN_HELP("commands.admin-help"),


    VERSION("messages-version")
    ;

    private final String path;
    private final EnchantedGangs instance = EnchantedGangs.getInstance();

    MessagesConfiguration(String path) {
        this.path = path;
    }

    public String getString(OfflinePlayer player) {
        String message = Utils.colorize(instance.getMessages().getString(path));
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public List<String> getStringList() {
        return instance.getMessages().getStringList(path).stream().map(Utils::colorize).collect(Collectors.toList());
    }

    public int getInt() {
        return instance.getMessages().getInt(path);
    }

    public void set(Object object) {
        instance.getMessages().set(path, object);
    }
}
