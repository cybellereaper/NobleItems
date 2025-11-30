package com.github.cybellereaper.item;

import com.github.cybellereaper.model.BlockbenchModel;
import com.github.cybellereaper.model.ItemStats;

import java.util.List;

public record CustomItem(String id,
                         String displayName,
                         String material,
                         int customModelData,
                         List<String> lore,
                         BlockbenchModel blockbench,
                         ItemStats stats) {
    public CustomItem {
        lore = List.copyOf(lore);
    }
}
