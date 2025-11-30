package com.github.cybellereaper.mob;

import com.github.cybellereaper.model.BlockbenchModel;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

public record CustomMob(String id,
                        String displayName,
                        EntityType entityType,
                        double maxHealth,
                        double attackDamage,
                        BlockbenchModel blockbench,
                        Map<EquipmentSlot, String> equipment) {
}
