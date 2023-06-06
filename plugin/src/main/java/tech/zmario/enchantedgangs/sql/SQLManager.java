package tech.zmario.enchantedgangs.sql;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.api.objects.Gang;
import tech.zmario.enchantedgangs.api.objects.User;
import tech.zmario.enchantedgangs.enums.MessagesConfiguration;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;
import tech.zmario.enchantedgangs.objects.pojo.GangImpl;
import tech.zmario.enchantedgangs.utils.InventoryUtils;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

public class SQLManager {

    private final EnchantedGangs plugin;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    @Getter
    private HikariDataSource dataSource;

    public SQLManager(EnchantedGangs plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        LibraryManager libraryManager = plugin.getLibraryManager();
        HikariConfig config = new HikariConfig();

        if (SettingsConfiguration.MYSQL_ENABLED.getBoolean()) {

            Library mysql = Library.builder()
                    .groupId("com.mysql")
                    .artifactId("mysql-connector-j")
                    .version("8.0.31")
                    .build();

            libraryManager.loadLibrary(mysql);

            config.setJdbcUrl("jdbc:mysql://" + SettingsConfiguration.MYSQL_HOST.getString() + ":" +
                    SettingsConfiguration.MYSQL_PORT.getString() + "/" + SettingsConfiguration.MYSQL_DATABASE.getString()
                    + "?autoReconnect=true&useSSL=false");

            config.setDriverClassName(SettingsConfiguration.MYSQL_DRIVER.getString() != null ?
                    SettingsConfiguration.MYSQL_DRIVER.getString() : "com.mysql.jdbc.Driver");

            config.setUsername(SettingsConfiguration.MYSQL_USERNAME.getString());
            config.setPassword(SettingsConfiguration.MYSQL_PASSWORD.getString());
        } else {
            Library library = Library.builder()
                    .groupId("org.xerial")
                    .artifactId("sqlite-jdbc")
                    .version("3.34.0")
                    .build();

            libraryManager.loadLibrary(library);

            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load SQLite driver", e);
                return false;
            }

            File file = new File(plugin.getDataFolder(), "database.db");

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error while creating database file", e);
                }
            }

            config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        }

        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(5000);
        config.setLeakDetectionThreshold(5000);

        config.setPoolName("EnchantedGangs Pool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useSSL", false);
        config.setConnectionTestQuery("SELECT 1");

        try {
            dataSource = new HikariDataSource(config);

            update("CREATE TABLE IF NOT EXISTS gangs (`name` VARCHAR(48) PRIMARY KEY," +
                    " `chest` LONGTEXT, `kills` int(8), `balance` REAL)");

            update("CREATE TABLE IF NOT EXISTS gang_members (`uuid` CHAR(36), `gang` VARCHAR(48)," +
                    " `rank` int(1))");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to the database! Disabling plugin...", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    public void disconnect() throws InterruptedException {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        executor.shutdown();

        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();

            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                plugin.getLogger().severe("Pool did not terminate!");
            }
        }
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to get connection from pool.");
            return null;
        }
    }

    public CachedRowSet query(String query, Object... objects) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (int i = 0; i < objects.length; i++)
                preparedStatement.setObject(i + 1, objects[i]);

            CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
            cachedRowSet.populate(preparedStatement.executeQuery());

            return cachedRowSet;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void update(String query, Object... objects) {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (int i = 0; i < objects.length; i++)
                preparedStatement.setObject(i + 1, objects[i]);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public CompletableFuture<CachedRowSet> queryAsync(String query, Object... objects) {
        return CompletableFuture.supplyAsync(() -> query(query, objects), executor);
    }

    public CompletableFuture<Void> updateAsync(String query, Object... objects) {
        return CompletableFuture.runAsync(() -> update(query, objects), executor);
    }

    public void setMemberRank(UUID uuid, int rank) {
        updateAsync("UPDATE `gang_members` SET `rank` = ? WHERE `uuid` = ?",
                rank, uuid.toString());
    }


    public void addGangMember(UUID playerUuid, Gang gang) {
        updateAsync("INSERT INTO `gang_members` (`uuid`, `gang`, `rank`) VALUES (?, ?, ?)",
                playerUuid.toString(), gang.getName(), plugin.getStorage().getLowestRank());
    }

    public void removeGangMember(UUID playerUuid) {
        updateAsync("DELETE FROM `gang_members` WHERE `uuid` = ?", playerUuid.toString());
    }

    public CompletableFuture<String> getGangName(UUID playerUuid) {
        return queryAsync("SELECT `gang` FROM `gang_members` WHERE `uuid` = ?",
                playerUuid.toString()).thenApplyAsync(set -> {
            try (CachedRowSet rowSet = set) {
                if (rowSet.next()) {
                    return rowSet.getString("gang");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get gang name", e);
            }

            return null;
        });
    }

    public void updateUuid(UUID oldUuid, UUID newUuid) {
        updateAsync("UPDATE `gang_members` SET `uuid` = ? WHERE `uuid` = ?",
                newUuid.toString(), oldUuid.toString());
    }

    public CompletableFuture<Void> createGang(String gangName) {
        return updateAsync("INSERT INTO `gangs` (`name`, `chest`, `kills`, `balance`) VALUES (?, ?, ?, ?)",
                gangName, null, 0, 0.0);
    }

    public CompletableFuture<Void> setPlayerGang(UUID uuid, String gangName, int rank) {
        return updateAsync("INSERT INTO `gang_members` (`uuid`, `gang`, `rank`) VALUES (?, ?, ?)",
                uuid.toString(), gangName, rank);
    }

    public void removeGang(String gangName) {
        updateAsync("DELETE FROM `gangs` WHERE `name` = ?", gangName);
    }

    public void setBalance(String name, double balance) {
        updateAsync("UPDATE `gangs` SET `balance` = ? WHERE `name` = ?", balance, name);
    }

    public void setChest(String name, String toBase64) {
        updateAsync("UPDATE `gangs` SET `chest` = ? WHERE `name` = ?", toBase64, name);
    }

    public List<Gang> getGangs() {
        List<Gang> gangs = new ArrayList<>();

        try (CachedRowSet rowSet = query("SELECT * FROM `gangs`")) {
            while (rowSet.next()) {
                String name = rowSet.getString("name");
                String chest = rowSet.getString("chest");
                Map<UUID, Integer> members = getMembers(name);
                Gang gang = new GangImpl(name, members);

                try {
                    gang.setKills(rowSet.getInt("kills"));
                    gang.setBalance(rowSet.getDouble("balance"));

                    if (chest != null) {
                        gang.setChest(InventoryUtils.fromBase64(chest,
                                MessagesConfiguration.GANG_CHEST_TITLE.getString(null)));
                    }

                    gang.setOwner(getOwner(name));
                    gangs.add(gang);
                } catch (SQLException | IOException e) {
                    plugin.getLogger().log(Level.SEVERE,
                            String.format("Failed to load gang %s from database.", name), e);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load gangs from database.", e);
        }

        return gangs;
    }

    public UUID getOwner(String name) {
        try (CachedRowSet rowSet = query("SELECT `uuid` FROM `gang_members` WHERE `gang` = ? AND `rank` = ?",
                name, 1)) {
            if (rowSet.next()) {
                return UUID.fromString(rowSet.getString("uuid"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE,
                    String.format("Failed to get owner of gang %s from database.", name), e);
        }

        return null;
    }

    public Map<UUID, Integer> getMembers(String name) {
        Map<UUID, Integer> members = Maps.newHashMap();

        try (CachedRowSet rowSet = query("SELECT * FROM `gang_members` WHERE `gang` = ?", name)) {
            while (rowSet.next()) {
                members.put(UUID.fromString(rowSet.getString("uuid")), rowSet.getInt("rank"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load gang members from database.", e);
        }

        return members;
    }

    public void setKills(String name, int kills) {
        updateAsync("UPDATE `gangs` SET `kills` = ? WHERE `name` = ?", kills, name);
    }

    public Collection<User> getUsers() {
        List<User> users = new ArrayList<>();

        try (CachedRowSet rowSet = query("SELECT * FROM `gang_members`")) {
            while (rowSet.next()) {
                UUID uuid = UUID.fromString(rowSet.getString("uuid"));
                User user = new User(uuid);

                user.setGangName(rowSet.getString("gang"));

                users.add(user);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load users from database.", e);
        }

        return users;
    }

    public void setGangName(String name, String newName) {
        updateAsync("UPDATE `gangs` SET `name` = ? WHERE `name` = ?", newName, name);
    }

    public CompletableFuture<UUID> getCurrentUuid(UUID uuid) {
        return queryAsync("SELECT `uuid` FROM `gang_members` WHERE `uuid` = ?",
                uuid.toString()).thenApplyAsync(set -> {
            try (CachedRowSet rowSet = set) {
                if (rowSet.next()) {
                    return UUID.fromString(rowSet.getString("uuid"));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get current uuid", e);
            }

            return null;
        });
    }

    public CompletableFuture<User> loadUser(UUID uuid) {
        return getCurrentUuid(uuid).thenApply(dataUuid -> {
            User user = new User(uuid);

            if (dataUuid == null) {
                user.setGangName(null);
            } else {
                if (!dataUuid.equals(uuid)) {
                    updateUuid(dataUuid, uuid);
                }

                getGangName(uuid).thenAccept(user::setGangName);
            }

            plugin.getStorage().getUsers().put(uuid, user);
            return user;
        });
    }
}
