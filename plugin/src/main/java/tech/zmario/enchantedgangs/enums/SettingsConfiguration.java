package tech.zmario.enchantedgangs.enums;

import tech.zmario.enchantedgangs.EnchantedGangs;

import java.util.List;

public enum SettingsConfiguration {

    // MySQL
    MYSQL_ENABLED("mysql.enabled"),
    MYSQL_DRIVER("mysql.driver"),
    MYSQL_HOST("mysql.host"),
    MYSQL_PORT("mysql.port"),
    MYSQL_USERNAME("mysql.username"),
    MYSQL_PASSWORD("mysql.password"),
    MYSQL_DATABASE("mysql.database"),

    LANGUAGE("language"),

    // Main settings
    CONFIGURATION_UPDATER("configuration-updater"),
    FRIENDLY_FIRE("friendly-fire"),
    INVITE_TIMEOUT("invite-timeout"),
    ACCEPT_COOLDOWN("accept-cooldown"),
    MAX_NAME_LENGTH("max-name-length"),
    MAX_MEMBERS_IN_GANG("max-members-in-gang"),
    LIST_MAX_SIZE("max-gangs-in-list"),
    LIST_RANKING_TYPE("list-ranking-type"),

    CREATE_COST("create-cost"),

    PERMISSION_RENAME("ranks.permissions.rename"),
    PERMISSION_PROMOTE("ranks.permissions.promote"),
    PERMISSION_DEMOTE("ranks.permissions.demote"),
    PERMISSION_KICK("ranks.permissions.kick"),
    PERMISSION_INVITE("ranks.permissions.invite"),
    PERMISSION_CHEST("ranks.permissions.chest"),

    // Features
    FEATURES_CHEST_ENABLED("features.chest.enabled"),
    FEATURES_CHEST_SIZE("features.chest.size"),

    FEATURES_BANK_ENABLED("features.bank.enabled"),
    FEATURES_BANK_WITHDRAW_MINIMUM_AMOUNT("features.bank.withdraw-minimum-amount"),
    FEATURES_DEPOSIT_MINIMUM_AMOUNT("features.bank.deposit-minimum-amount"),
    FEATURES_KILLS_ENABLED("features.kills.enabled"),


    // Commands
    COMMAND_GANG_NAME("commands.gangs.name"),
    COMMAND_GANG_ALIASES("commands.gangs.aliases"),

    COMMAND_GANG_ADMIN_NAME("commands.gang-admin.name"),
    COMMAND_GANG_ADMIN_ALIASES("commands.gang-admin.aliases"),

    COMMAND_ADMIN_RELOAD_NAME("commands.admin.reload.name"),
    COMMAND_ADMIN_DISBAND_NAME("commands.admin.disband.name"),
    COMMAND_ADMIN_STATISTIC_NAME("commands.admin.statistic.name"),

    COMMAND_CREATE_NAME("commands.create.name"),
    COMMAND_INVITE_NAME("commands.invite.name"),
    COMMAND_KICK_NAME("commands.kick.name"),
    COMMAND_LEAVE_NAME("commands.leave.name"),
    COMMAND_DISBAND_NAME("commands.disband.name"),
    COMMAND_RENAME_NAME("commands.rename.name"),
    COMMAND_ACCEPT_NAME("commands.accept.name"),
    COMMAND_PROMOTE_NAME("commands.promote.name"),
    COMMAND_DEMOTE_NAME("commands.demote.name"),
    COMMAND_SHOW_NAME("commands.show.name"),
    COMMAND_LIST_NAME("commands.list.name"),
    COMMAND_CHAT_NAME("commands.chat.name"),
    COMMAND_DEPOSIT_NAME("commands.deposit.name"),
    COMMAND_WITHDRAW_NAME("commands.withdraw.name"),
    COMMAND_CHEST_NAME("commands.chest.name"),

    VERSION("config-version")
    ;

    private final String path;
    private final EnchantedGangs instance = EnchantedGangs.getInstance();

    SettingsConfiguration(String path) {
        this.path = path;
    }

    public String getString() {
        return instance.getConfig().getString(path);
    }

    public int getInt() {
        return instance.getConfig().getInt(path);
    }

    public boolean getBoolean() {
        return instance.getConfig().getBoolean(path);
    }

    public List<String> getStringList() {
        return instance.getConfig().getStringList(path);
    }

    public void set(Object value) {
        instance.getConfig().set(path, value);
    }
}
