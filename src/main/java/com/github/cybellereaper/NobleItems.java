package com.github.cybellereaper;

import com.github.cybellereaper.command.NobleItemsCommand;
import com.github.cybellereaper.config.DefinitionLoader;
import com.github.cybellereaper.item.CustomItemRegistry;
import com.github.cybellereaper.item.ItemFactory;
import com.github.cybellereaper.mob.CustomMobRegistry;
import com.github.cybellereaper.mob.MobSpawner;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class NobleItems extends JavaPlugin {
    private final CustomItemRegistry itemRegistry = new CustomItemRegistry();
    private final CustomMobRegistry mobRegistry = new CustomMobRegistry();
    private DefinitionLoader definitionLoader;
    private ItemFactory itemFactory;
    private MobSpawner mobSpawner;

    @Override
    public void onEnable() {
        this.definitionLoader = new DefinitionLoader(getLogger());
        this.itemFactory = new ItemFactory(this);
        this.mobSpawner = new MobSpawner(mobRegistry, itemRegistry, itemFactory);

        saveResourceIfMissing("items.yml");
        saveResourceIfMissing("mobs.yml");
        reloadDefinitions();

        registerCommand();
        getLogger().info(() -> "NobleItems is ready with " + itemRegistry.size() + " items and " + mobRegistry.size() + " mobs.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NobleItems shutting down gracefully.");
    }

    public void reloadDefinitions() {
        File itemsFile = new File(getDataFolder(), "items.yml");
        File mobsFile = new File(getDataFolder(), "mobs.yml");
        try {
            itemRegistry.replaceAll(definitionLoader.loadItems(itemsFile));
            mobRegistry.replaceAll(definitionLoader.loadMobs(mobsFile));
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Unable to load definitions", ex);
        }
    }

    private void registerCommand() {
        PluginCommand command = getCommand("nobleitems");
        if (command == null) {
            getLogger().severe("Failed to register /nobleitems command; check plugin.yml");
            return;
        }
        NobleItemsCommand executor = new NobleItemsCommand(this, itemRegistry, mobRegistry, itemFactory, mobSpawner);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void saveResourceIfMissing(String resource) {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create plugin data folder.");
        }
        if (!new File(getDataFolder(), resource).exists()) {
            saveResource(resource, false);
        }
    }
}
