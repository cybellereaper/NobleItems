package com.github.cybellereaper.config;

import com.github.cybellereaper.item.CustomItem;
import com.github.cybellereaper.model.BlockbenchModel;
import com.github.cybellereaper.model.ItemStats;
import com.github.cybellereaper.mob.CustomMob;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class DefinitionLoader {
    private final Logger logger;

    public DefinitionLoader(Logger logger) {
        this.logger = logger;
    }

    public List<CustomItem> loadItems(File file) throws IOException {
        YamlConfiguration config = load(file);
        ConfigurationSection section = config.getConfigurationSection("items");
        List<CustomItem> items = new ArrayList<>();
        if (section == null) {
            logger.warning("No items section found in " + file.getName());
            return items;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(id);
            if (itemSection == null) {
                continue;
            }
            items.add(parseItem(id, itemSection));
        }
        logger.info(() -> "Loaded " + items.size() + " items from " + file.getName());
        return items;
    }

    public List<CustomMob> loadMobs(File file) throws IOException {
        YamlConfiguration config = load(file);
        ConfigurationSection section = config.getConfigurationSection("mobs");
        List<CustomMob> mobs = new ArrayList<>();
        if (section == null) {
            logger.warning("No mobs section found in " + file.getName());
            return mobs;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection mobSection = section.getConfigurationSection(id);
            if (mobSection == null) {
                continue;
            }
            mobs.add(parseMob(id, mobSection));
        }
        logger.info(() -> "Loaded " + mobs.size() + " mobs from " + file.getName());
        return mobs;
    }

    private CustomItem parseItem(String id, ConfigurationSection section) {
        String name = section.getString("display-name", id);
        String material = section.getString("material", "STONE");
        int customModelData = section.getInt("custom-model-data", 0);
        List<String> lore = section.getStringList("lore");
        BlockbenchModel model = parseBlockbench(section.getConfigurationSection("blockbench"));
        ItemStats stats = parseStats(section.getConfigurationSection("stats"));
        return new CustomItem(id, name, material, customModelData, lore, model, stats);
    }

    private CustomMob parseMob(String id, ConfigurationSection section) {
        String displayName = section.getString("display-name", id);
        String typeName = section.getString("type", EntityType.ZOMBIE.name());
        EntityType entityType = EntityType.fromName(typeName.toUpperCase());
        if (entityType == null) {
            entityType = EntityType.ZOMBIE;
            logger.warning("Unknown entity type for mob " + id + ": " + typeName + ". Defaulting to ZOMBIE.");
        }
        double maxHealth = section.getDouble("max-health", 20.0);
        double attackDamage = section.getDouble("attack-damage", 3.0);
        BlockbenchModel blockbench = parseBlockbench(section.getConfigurationSection("blockbench"));
        Map<EquipmentSlot, String> equipment = parseEquipment(section.getConfigurationSection("equipment"));
        return new CustomMob(id, displayName, entityType, maxHealth, attackDamage, blockbench, equipment);
    }

    private Map<EquipmentSlot, String> parseEquipment(ConfigurationSection section) {
        Map<EquipmentSlot, String> equipment = new HashMap<>();
        if (section == null) {
            return equipment;
        }
        for (String key : section.getKeys(false)) {
            try {
                EquipmentSlot slot = EquipmentSlot.valueOf(key.toUpperCase());
                String itemId = section.getString(key, "");
                if (itemId != null && !itemId.isEmpty()) {
                    equipment.put(slot, itemId);
                }
            } catch (IllegalArgumentException ex) {
                logger.warning("Skipping unknown equipment slot '" + key + "' for mob definitions.");
            }
        }
        return equipment;
    }

    private ItemStats parseStats(ConfigurationSection section) {
        if (section == null) {
            return ItemStats.EMPTY;
        }
        double damage = section.getDouble("damage", 0.0);
        double defense = section.getDouble("defense", 0.0);
        double critChance = section.getDouble("critical-chance", 0.0);
        return new ItemStats(damage, defense, critChance);
    }

    private BlockbenchModel parseBlockbench(ConfigurationSection section) {
        if (section == null) {
            return BlockbenchModel.NONE;
        }
        String modelPath = section.getString("model", "");
        String texturePath = section.getString("texture", "");
        double scale = section.getDouble("scale", 1.0);
        return new BlockbenchModel(modelPath, texturePath, scale);
    }

    private YamlConfiguration load(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Config file does not exist: " + file.getAbsolutePath());
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
