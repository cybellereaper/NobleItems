package com.github.cybellereaper.resourcepack;

import com.github.cybellereaper.item.CustomItem;
import com.github.cybellereaper.item.CustomItemRegistry;
import com.github.cybellereaper.model.BlockbenchModel;
import com.github.cybellereaper.mob.CustomMob;
import com.github.cybellereaper.mob.CustomMobRegistry;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds a lightweight resource pack for Blockbench models and textures defined in NobleItems configuration files.
 */
public final class ResourcePackBuilder {
    private static final int PACK_FORMAT_1_21 = 34;

    private final Path dataFolder;
    private final Logger logger;
    private final CustomItemRegistry itemRegistry;
    private final CustomMobRegistry mobRegistry;

    public ResourcePackBuilder(Path dataFolder, Logger logger, CustomItemRegistry itemRegistry, CustomMobRegistry mobRegistry) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.itemRegistry = itemRegistry;
        this.mobRegistry = mobRegistry;
    }

    /**
     * Builds a zip file containing a resource pack for every Blockbench asset referenced in the loaded definitions.
     * @param destination where the pack zip should be written
     * @return the destination path for convenience
     * @throws IOException if any file operation fails
     */
    public Path build(Path destination) throws IOException {
        Path working = Files.createTempDirectory("nobleitems-pack");
        try {
            writePackMcmeta(working);
            copyAssets(working);
            writeItemOverrides(working);
            zipPack(working, destination);
        } finally {
            deleteRecursive(working);
        }
        return destination;
    }

    private void writePackMcmeta(Path root) throws IOException {
        String json = "{" +
                "\"pack\":{" +
                "\"pack_format\":" + PACK_FORMAT_1_21 + ',' +
                "\"description\":\"NobleItems Blockbench pack\"" +
                "}}";
        Path meta = root.resolve("pack.mcmeta");
        Files.createDirectories(meta.getParent());
        Files.writeString(meta, json);
    }

    private void copyAssets(Path root) {
        itemRegistry.values().forEach(item -> copyBlockbenchAssets(root, item.blockbench()));
        mobRegistry.values().forEach(mob -> copyBlockbenchAssets(root, mob.blockbench()));
    }

    private void copyBlockbenchAssets(Path root, BlockbenchModel model) {
        if (model == null || model.equals(BlockbenchModel.NONE)) {
            return;
        }
        copyIfExists(model.modelPath(), root.resolve("assets/nobleitems").resolve(model.modelPath()));
        copyIfExists(model.texturePath(), root.resolve("assets/nobleitems").resolve(model.texturePath()));
    }

    private void writeItemOverrides(Path root) throws IOException {
        Map<String, List<OverrideEntry>> overrides = new HashMap<>();
        for (CustomItem item : itemRegistry.values()) {
            if (item.customModelData() <= 0 || item.blockbench() == null || item.blockbench().equals(BlockbenchModel.NONE)) {
                continue;
            }
            String material = item.material().toLowerCase();
            String modelResource = toModelResource(item.blockbench());
            overrides.computeIfAbsent(material, key -> new ArrayList<>())
                    .add(new OverrideEntry(item.customModelData(), modelResource));
        }

        for (Map.Entry<String, List<OverrideEntry>> entry : overrides.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt(OverrideEntry::customModelData));
            Path itemModel = root.resolve("assets/minecraft/models/item/" + entry.getKey().toLowerCase() + ".json");
            Files.createDirectories(itemModel.getParent());
            String json = generateItemModelJson(entry.getKey(), entry.getValue());
            Files.writeString(itemModel, json);
        }
    }

    private String generateItemModelJson(String material, List<OverrideEntry> overrides) {
        String parent = isHandheld(material) ? "minecraft:item/handheld" : "minecraft:item/generated";
        String texture = "minecraft:item/" + material.toLowerCase();
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"parent\":\"").append(parent).append('\"')
                .append(',')
                .append("\"textures\":{\"layer0\":\"").append(texture).append("\"}")
                .append(',')
                .append("\"overrides\":[");
        for (int i = 0; i < overrides.size(); i++) {
            OverrideEntry entry = overrides.get(i);
            builder.append('{')
                    .append("\"predicate\":{\"custom_model_data\":")
                    .append(entry.customModelData())
                    .append('}')
                    .append(',')
                    .append("\"model\":\"")
                    .append(entry.model())
                    .append("\"}");
            if (i < overrides.size() - 1) {
                builder.append(',');
            }
        }
        builder.append(']')
                .append('}');
        return builder.toString();
    }

    private String toModelResource(BlockbenchModel model) {
        String path = Optional.ofNullable(model.modelPath()).orElse("");
        String normalized = path.replace('\\', '/');
        if (normalized.startsWith("models/")) {
            normalized = normalized.substring("models/".length());
        }
        if (normalized.endsWith(".json")) {
            normalized = normalized.substring(0, normalized.length() - 5);
        }
        return "nobleitems:" + normalized;
    }

    private void copyIfExists(String relativePath, Path destination) {
        if (relativePath == null || relativePath.isEmpty()) {
            return;
        }
        Path source = dataFolder.resolve(relativePath);
        if (!Files.exists(source)) {
            logger.warning("Missing Blockbench asset: " + source);
            return;
        }
        try {
            Files.createDirectories(destination.getParent());
            Files.copy(source, destination);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to copy Blockbench asset " + source, ex);
        }
    }

    private boolean isHandheld(String material) {
        String name = material.toUpperCase();
        return name.contains("SWORD") || name.contains("AXE") || name.contains("HOE") || name.contains("PICKAXE") || name.contains("SHOVEL");
    }

    private void zipPack(Path root, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(destination))) {
            Files.walk(root)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> writeZipEntry(zip, root, path));
        }
    }

    private void writeZipEntry(ZipOutputStream zip, Path root, Path file) {
        try (InputStream in = new FileInputStream(file.toFile())) {
            String entryName = root.relativize(file).toString().replace('\\', '/');
            zip.putNextEntry(new ZipEntry(entryName));
            in.transferTo(zip);
            zip.closeEntry();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to add file to resource pack: " + file, ex);
        }
    }

    private void deleteRecursive(Path path) {
        try {
            if (!Files.exists(path)) {
                return;
            }
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                            // best effort cleanup
                        }
                    });
        } catch (IOException ignored) {
            // best effort cleanup
        }
    }

    private record OverrideEntry(int customModelData, String model) { }
}
