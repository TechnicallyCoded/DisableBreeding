package com.tcoded.disablebreeding;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DisableBreeding extends JavaPlugin implements Listener, CommandExecutor {

    private final List<EntityType> disabledEntities = new ArrayList<>();
    private boolean disableAll = false;

    @Override
    public void onEnable() {
        loadConfig();

        // Register the onBreed event listener
        getServer().getPluginManager().registerEvents(this, this);

        PluginCommand disablebreedingPlCmd = getCommand("disablebreeding");
        if (disablebreedingPlCmd != null) disablebreedingPlCmd.setExecutor(this);
    }

    private void loadConfig() {
        disabledEntities.clear();
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        // Load the list of disabled entities from the configuration file
        disableAll = config.getBoolean("disable-all", false);
        List<String> disabledEntitiesEntries = config.getStringList("disabled-entities");
        for (String entry : disabledEntitiesEntries) {
            try {
                disabledEntities.add(EntityType.valueOf(entry));
            } catch (IllegalArgumentException ex) {
                getLogger().info("Found invalid entity type in config: " + entry);
            }
        }
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /disablebreeding reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            this.reloadConfig();
            loadConfig();
            sender.sendMessage(ChatColor.GREEN + "Reloaded config!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /disablebreeding reload");
        return true;
    }

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        if (disableAll) {
            // Cancel the event to prevent the entities from breeding
            cancelBreed(event);

            // Feedback
            LivingEntity breeder = event.getBreeder();
            if (breeder instanceof Player) {
                Player player = (Player) breeder;
                player.sendMessage(ChatColor.RED + "All breeding was disabled!");
            }
            return;
        }

        // Check if the entity type is in the list of disabled entities
        if (disabledEntities.contains(entityType)) {
            // Cancel the event to prevent the entities from breeding
            cancelBreed(event);

            // Feedback
            LivingEntity breeder = event.getBreeder();
            if (breeder instanceof Player) {
                Player player = (Player) breeder;
                player.sendMessage(ChatColor.RED + "Breeding was disabled for: " + entityType.name().toLowerCase() + "!");
            }
        }
    }

    private static void cancelBreed(EntityBreedEvent event) {
        event.setCancelled(true);
        LivingEntity father = event.getFather();
        LivingEntity mother = event.getMother();
        if (father instanceof Animals) {
            Animals animal = (Animals) father;
            animal.setLoveModeTicks(0);
        }
        if (mother instanceof Animals) {
            Animals animal = (Animals) mother;
            animal.setLoveModeTicks(0);
        }
    }
}
