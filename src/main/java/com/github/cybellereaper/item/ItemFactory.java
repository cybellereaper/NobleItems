package com.github.cybellereaper.item;

import com.github.cybellereaper.model.ItemStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class ItemFactory {
    private final Plugin plugin;

    public ItemFactory(Plugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItem(CustomItem definition, int amount) {
        Material material = Material.matchMaterial(definition.material());
        if (material == null) {
            throw new IllegalArgumentException("Unknown material for item " + definition.id());
        }

        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        meta.displayName(toComponent(definition.displayName()));
        meta.setCustomModelData(definition.customModelData());
        meta.lore(toComponentLore(definition.lore()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        writeStats(meta.getPersistentDataContainer(), definition.stats());

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void writeStats(PersistentDataContainer container, ItemStats stats) {
        container.set(key("damage"), PersistentDataType.DOUBLE, stats.damage());
        container.set(key("defense"), PersistentDataType.DOUBLE, stats.defense());
        container.set(key("crit"), PersistentDataType.DOUBLE, stats.critChance());
    }

    private NamespacedKey key(String value) {
        return new NamespacedKey(plugin, value);
    }

    private Component toComponent(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    private List<Component> toComponentLore(List<String> lore) {
        List<Component> lines = new ArrayList<>(lore.size());
        lore.forEach(line -> lines.add(toComponent(line)));
        return lines;
    }
}
