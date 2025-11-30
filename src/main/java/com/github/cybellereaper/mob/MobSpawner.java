package com.github.cybellereaper.mob;

import com.github.cybellereaper.item.CustomItem;
import com.github.cybellereaper.item.CustomItemRegistry;
import com.github.cybellereaper.item.ItemFactory;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

public final class MobSpawner {
    private final CustomMobRegistry mobRegistry;
    private final CustomItemRegistry itemRegistry;
    private final ItemFactory itemFactory;

    public MobSpawner(CustomMobRegistry mobRegistry, CustomItemRegistry itemRegistry, ItemFactory itemFactory) {
        this.mobRegistry = mobRegistry;
        this.itemRegistry = itemRegistry;
        this.itemFactory = itemFactory;
    }

    public Optional<LivingEntity> spawn(Player player, String id) {
        Optional<CustomMob> mobOpt = mobRegistry.get(id);
        if (mobOpt.isEmpty()) {
            return Optional.empty();
        }
        CustomMob mob = mobOpt.get();
        World world = player.getWorld();
        Location spawnLocation = player.getLocation().add(player.getLocation().getDirection()).add(0, 0, 1);
        LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLocation, mob.entityType());

        entity.customName(Component.text(mob.displayName()));
        entity.setCustomNameVisible(true);
        applyAttributes(entity, mob);
        equip(entity, mob.equipment());
        return Optional.of(entity);
    }

    private void applyAttributes(LivingEntity entity, CustomMob mob) {
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(mob.maxHealth());
            entity.setHealth(mob.maxHealth());
        }
        AttributeInstance attack = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(mob.attackDamage());
        }
    }

    private void equip(LivingEntity entity, Map<EquipmentSlot, String> equipment) {
        if (equipment.isEmpty()) {
            return;
        }
        EntityEquipment gear = entity.getEquipment();
        if (gear == null) {
            return;
        }
        equipment.forEach((slot, itemId) -> itemRegistry.get(itemId).ifPresent(definition -> {
            ItemStack item = itemFactory.createItem(definition, 1);
            switch (slot) {
                case HAND -> gear.setItemInMainHand(item);
                case OFF_HAND -> gear.setItemInOffHand(item);
                case HEAD -> gear.setHelmet(item);
                case CHEST -> gear.setChestplate(item);
                case LEGS -> gear.setLeggings(item);
                case FEET -> gear.setBoots(item);
                default -> { }
            }
        }));
    }
}
