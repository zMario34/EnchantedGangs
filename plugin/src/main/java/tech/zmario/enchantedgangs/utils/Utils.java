package tech.zmario.enchantedgangs.utils;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import tech.zmario.enchantedgangs.EnchantedGangs;
import tech.zmario.enchantedgangs.enums.SettingsConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Utils {

    @Getter
    private final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    @Getter
    private final int intVersion = Integer.parseInt(version.split("_")[1]);

    private final Pattern hexPattern = Pattern.compile("&#(\\w{5}[0-9a-f])");
    private final Pattern illegalPattern = Pattern.compile("[#<>$+%!`&*'|{?\"=}/:@~()^]");

    public String colorize(@NotNull String message) {
        String colored = ChatColor.translateAlternateColorCodes('&', message);

        if (intVersion >= 16) {
            Matcher matcher = hexPattern.matcher(message);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                matcher.appendReplacement(buffer,
                        net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
            }

            return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
        }

        return colored;
    }

    public void updateFile(EnchantedGangs plugin, String configName, FileConfiguration external, int newVersion) {
        File file = new File(plugin.getDataFolder(), configName + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(configName + ".yml")));

        boolean updated = false;

        for (String key : configuration.getKeys(true)) {
            if (!external.contains(key)) {
                external.set(key, configuration.get(key));
                updated = true;
            }
        }

        if (updated) {
            try {
                external.save(file);
                plugin.getLogger().info("Updated " + configName + " to version " + newVersion);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save " + configName + ".yml");
            }
        }
    }

    public boolean containsIllegals(String string) {
        Matcher matcher = illegalPattern.matcher(string);
        return matcher.find();
    }

    public String formatMoney(double amount) {
        if (amount < 1000L) {
            return String.format("%.2f", (amount));
        } else if (amount < 1000000L) {
            return String.format("%.2f", amount / 1000D) + "K";
        } else if (amount < 1000000000L) {
            return String.format("%.2f", amount / 1000000D) + "M";
        } else if (amount < 1000000000000L) {
            return String.format("%.2f", amount / 1000000000D) + "B";
        } else if (amount < 1000000000000000L) {
            return String.format("%.2f", amount / 1000000000000D) + "T";
        } else if (amount < 1000000000000000000L) {
            return String.format("%.2f", amount / 1000000000000000D) + "Q";
        } else {
            return String.format("%.2f", amount);
        }
    }

    public void registerCommand(CommandExecutor executor, PluginCommand pluginCommand, String[] aliases, String label, Plugin plugin) {
        if (pluginCommand == null) {
            try {
                Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");

                commandMapField.setAccessible(true);

                CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
                Constructor<PluginCommand> commandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);

                commandConstructor.setAccessible(true);

                pluginCommand = commandConstructor.newInstance(label, plugin);

                if (aliases.length > 0) {
                    pluginCommand.setAliases(Arrays.asList(aliases));
                }

                if (!commandMap.register(plugin.getName(), pluginCommand)) {
                    plugin.getLogger().severe("Failed to register the gang command.");
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to register the gang command.");
                return;
            }
        }

        pluginCommand.setExecutor(executor);
    }

    public void unregisterCommands() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            PluginCommand gangCommand = (PluginCommand) commandMap.getCommand(SettingsConfiguration.COMMAND_GANG_NAME.getString());

            if (gangCommand != null) {
                gangCommand.unregister(commandMap);
            }

            PluginCommand gangAdminCommand = (PluginCommand) commandMap.getCommand(SettingsConfiguration.COMMAND_GANG_ADMIN_NAME.getString());

            if (gangAdminCommand != null) {
                gangAdminCommand.unregister(commandMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
