package com.github.cybellereaper.resourcepack;

import com.github.cybellereaper.item.CustomItem;
import com.github.cybellereaper.item.CustomItemRegistry;
import com.github.cybellereaper.model.BlockbenchModel;
import com.github.cybellereaper.model.ItemStats;
import com.github.cybellereaper.mob.CustomMobRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcePackBuilderTest {

    @Test
    void buildsZipWithBlockbenchAssetsAndOverrides(@TempDir Path tempDir) throws IOException {
        Path dataFolder = tempDir.resolve("data");
        Files.createDirectories(dataFolder.resolve("models/item"));
        Files.createDirectories(dataFolder.resolve("textures/item"));

        Path modelFile = dataFolder.resolve("models/item/test.geo.json");
        Path textureFile = dataFolder.resolve("textures/item/test.png");
        Files.writeString(modelFile, "{\"dummy\":true}");
        Files.writeString(textureFile, "texture");

        CustomItem item = new CustomItem(
                "test_item",
                "Test Item",
                "DIAMOND_SWORD",
                1234,
                List.of("Line"),
                new BlockbenchModel("models/item/test.geo.json", "textures/item/test.png", 1.0),
                ItemStats.EMPTY
        );

        CustomItemRegistry itemRegistry = new CustomItemRegistry();
        itemRegistry.replaceAll(List.of(item));

        ResourcePackBuilder builder = new ResourcePackBuilder(dataFolder, Logger.getLogger("test"), itemRegistry, new CustomMobRegistry());
        Path destination = tempDir.resolve("pack.zip");
        builder.build(destination);

        try (ZipFile zip = new ZipFile(destination.toFile())) {
            assertNotNull(zip.getEntry("pack.mcmeta"));
            assertNotNull(zip.getEntry("assets/nobleitems/models/item/test.geo.json"));
            assertNotNull(zip.getEntry("assets/nobleitems/textures/item/test.png"));

            ZipEntry override = zip.getEntry("assets/minecraft/models/item/diamond_sword.json");
            assertNotNull(override);
            String overrideContent = new String(zip.getInputStream(override).readAllBytes());
            assertTrue(overrideContent.contains("custom_model_data"));
            assertTrue(overrideContent.contains("nobleitems:item/test.geo"));
        }
    }
}

