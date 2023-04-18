package tech.zmario.enchantedgangs;

import lombok.Getter;
import lombok.var;
import net.byteflux.libby.BukkitLibraryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import tech.zmario.enchantedgangs.api.EnchantedGangsAPI;
import tech.zmario.enchantedgangs.api.commands.interfaces.SubCommand;
import tech.zmario.enchantedgangs.api.hooks.team.TeamHook;
import tech.zmario.enchantedgangs.api.managers.GangsManager;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.api.objects.User;
import tech.zmario.enchantedgangs.commands.GangAdminCommand;
import tech.zmario.enchantedgangs.commands.GangCommand;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;
import tech.zmario.enchantedgangs.hooks.PlaceholderAPIHook;
import tech.zmario.enchantedgangs.listeners.PlayerConnectionListener;
import tech.zmario.enchantedgangs.listeners.PlayerListener;
import tech.zmario.enchantedgangs.managers.GangsManagerImpl;
import tech.zmario.enchantedgangs.managers.TeamHookManager;
import tech.zmario.enchantedgangs.sql.SQLManager;
import tech.zmario.enchantedgangs.storage.CacheStorage;
import tech.zmario.enchantedgangs.utils.Utils;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

@Getter
public final class EnchantedGangs extends JavaPlugin implements EnchantedGangsAPI {

    @Getter
    private static EnchantedGangs instance;

    private CacheStorage storage;
    private GangsManager gangsManager;
    private SQLManager sqlManager;

    private TeamHookManager teamHookManager;

    private YamlConfiguration messages;

    private BukkitLibraryManager libraryManager;

    private Economy economy;

    private GangCommand command;

    @Override
    public void onEnable() {
        loadInstances();
        loadMessages();

        updateConfigurations();
        loadListeners();

        loadGangs();
        loadUsers();
        loadRanks();

        Bukkit.getServicesManager().register(EnchantedGangsAPI.class, this, this, ServicePriority.Normal);

        getLogger().info("The plugin has been enabled. Thank you for using it!");
    }

    private void loadUsers() {
        for (User user : sqlManager.getUsers()) {
            storage.addUser(user);
        }
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    }

    private void updateConfigurations() {
        if (!SettingsConfiguration.CONFIGURATION_UPDATER.getBoolean()) return;
        int newSettingsVersion = 2;
        int newMessagesVersion = 3;

        if (SettingsConfiguration.VERSION.getInt() < newSettingsVersion) {
            SettingsConfiguration.VERSION.set(newSettingsVersion);

            Utils.updateFile(this, "config", getConfig(), newSettingsVersion);
        }

        if (MessagesConfiguration.VERSION.getInt() < newMessagesVersion) {
            MessagesConfiguration.VERSION.set(newMessagesVersion);

            Utils.updateFile(this, "messages_" + SettingsConfiguration.LANGUAGE.getString(),
                    getMessages(), newMessagesVersion);
        }
    }

    private void loadMessages() {
        String lang = SettingsConfiguration.LANGUAGE.getString();

        if (lang == null || lang.isEmpty()) {
            lang = "en";
        }

        File file = new File(getDataFolder(), "messages_" + lang + ".yml");

        if (!file.exists()) {
            saveResource("messages_" + lang + ".yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(getDataFolder().toPath()
                .resolve("messages_" + lang + ".yml").toFile());
    }

    private void loadInstances() {
        saveDefaultConfig();

        instance = this;
        libraryManager = new BukkitLibraryManager(this);

        libraryManager.addMavenCentral();

        sqlManager = new SQLManager(this);

        if (!sqlManager.connect()) return; // Disable plugin if connection failed

        storage = new CacheStorage();
        gangsManager = new GangsManagerImpl(this);
        teamHookManager = new TeamHookManager(this);

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            var registration = Bukkit.getServicesManager().getRegistration(Economy.class);

            if (registration == null) {
                getLogger().severe("Could not find an economy provider for Vault!");
            } else {
                economy = registration.getProvider();
                getLogger().info("Hooked into Vault!");
            }
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("Hooked into PlaceholderAPI!");
        }

        command = new GangCommand(this);
        new GangAdminCommand(this);

        teamHookManager.enable();
    }

    public void reloadPlugin() {
        reloadConfig();
        loadMessages();

        storage.getRanks().clear();
        loadRanks();
    }

    private void loadRanks() {
        for (String string : getConfig().getConfigurationSection("ranks.list").getKeys(false)) {
            storage.addRank(Integer.parseInt(string), getConfig().getString("ranks.list." + string));
        }
    }

    private void loadGangs() {
        for (Gang gang : sqlManager.getGangs()) {
            storage.getLoadedGangs().put(gang.getName(), gang);
        }
    }

    @Override
    public void onDisable() {
        if (sqlManager != null) {
            try {
                sqlManager.disconnect();
            } catch (InterruptedException e) {
                getLogger().log(Level.SEVERE, "An error occurred while disconnecting from the database", e);
            }
        }

        Bukkit.getServicesManager().unregister(EnchantedGangsAPI.class, this);
        Utils.unregisterCommands();
    }

    @Override
    public void setGangsManager(@NotNull GangsManager gangsManager) {
        Objects.requireNonNull(gangsManager, "gangsManager cannot be null");
        this.gangsManager = gangsManager;
    }

    @Override
    public void registerSubCommand(@NotNull String label, SubCommand subCommand) {
        command.addSubCommand(label, subCommand);
    }

    @Override
    public boolean registerTeamHook(TeamHook teamHook) {
        return teamHookManager.registerTeamHook(teamHook);
    }
}
