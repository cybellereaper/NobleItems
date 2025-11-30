package com.github.cybellereaper.config;

import com.github.cybellereaper.item.CustomItem;
import com.github.cybellereaper.mob.CustomMob;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefinitionLoaderTest {
    private final DefinitionLoader loader = new DefinitionLoader(Logger.getLogger("test"));

    @Test
    void loadsItemsAndMobsFromYaml() throws Exception {
        File tempDir = Files.createTempDirectory("nobleitems-test").toFile();
        File itemsFile = new File(tempDir, "items.yml");
        File mobsFile = new File(tempDir, "mobs.yml");

        write(itemsFile, """
items:
  blade:
    display-name: "Blade"
    material: DIAMOND_SWORD
    custom-model-data: 10
    lore:
      - "Line"
    stats:
      damage: 5
""");

        write(mobsFile, """
mobs:
  warrior:
    display-name: "Warrior"
    type: ZOMBIE
    max-health: 30
    attack-damage: 7
    equipment:
      hand: blade
""");

        List<CustomItem> items = loader.loadItems(itemsFile);
        List<CustomMob> mobs = loader.loadMobs(mobsFile);

        assertEquals(1, items.size());
        assertEquals("blade", items.getFirst().id());
        assertEquals(10, items.getFirst().customModelData());
        assertEquals(1, items.getFirst().lore().size());

        assertEquals(1, mobs.size());
        assertEquals("warrior", mobs.getFirst().id());
        assertTrue(mobs.getFirst().equipment().containsKey(org.bukkit.inventory.EquipmentSlot.HAND));
        assertEquals("blade", mobs.getFirst().equipment().get(org.bukkit.inventory.EquipmentSlot.HAND));
    }

    private void write(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
