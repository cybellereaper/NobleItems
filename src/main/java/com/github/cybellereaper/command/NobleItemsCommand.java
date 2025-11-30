package com.github.cybellereaper.command;

import com.github.cybellereaper.NobleItems;
import com.github.cybellereaper.item.CustomItem;
import com.github.cybellereaper.item.CustomItemRegistry;
import com.github.cybellereaper.item.ItemFactory;
import com.github.cybellereaper.mob.CustomMobRegistry;
import com.github.cybellereaper.mob.MobSpawner;
import com.github.cybellereaper.resourcepack.ResourcePackBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.Path;

public final class NobleItemsCommand implements TabExecutor {
    private final NobleItems plugin;
    private final CustomItemRegistry itemRegistry;
    private final CustomMobRegistry mobRegistry;
    private final ItemFactory itemFactory;
    private final MobSpawner mobSpawner;
    private final ResourcePackBuilder resourcePackBuilder;

    public NobleItemsCommand(NobleItems plugin, CustomItemRegistry itemRegistry, CustomMobRegistry mobRegistry,
                             ItemFactory itemFactory, MobSpawner mobSpawner, ResourcePackBuilder resourcePackBuilder) {
        this.plugin = plugin;
        this.itemRegistry = itemRegistry;
        this.mobRegistry = mobRegistry;
        this.itemFactory = itemFactory;
        this.mobSpawner = mobSpawner;
        this.resourcePackBuilder = resourcePackBuilder;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " reload|give|spawn|buildpack [filename]"));
            return true;
        }
        return switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "give" -> handleGive(sender, args);
            case "spawn" -> handleSpawn(sender, args);
            case "buildpack" -> handleBuildPack(sender, args);
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand."));
                yield true;
            }
        };
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("nobleitems.admin")) {
            sender.sendMessage(Component.text("You lack permission to do that."));
            return true;
        }
        plugin.reloadDefinitions();
        sender.sendMessage(Component.text("Definitions reloaded."));
        return true;
    }

    private boolean handleBuildPack(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nobleitems.admin")) {
            sender.sendMessage(Component.text("You lack permission to do that."));
            return true;
        }
        String fileName = args.length >= 2 ? args[1] : "nobleitems-resourcepack.zip";
        Path output = plugin.getDataFolder().toPath().resolve(fileName);
        try {
            resourcePackBuilder.build(output);
            sender.sendMessage(Component.text("Built resource pack at " + output.toAbsolutePath()));
        } catch (Exception ex) {
            sender.sendMessage(Component.text("Failed to build pack: " + ex.getMessage()));
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to build resource pack", ex);
        }
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /nobleitems give <player> <itemId> [amount]"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found."));
            return true;
        }
        Optional<CustomItem> itemOpt = itemRegistry.get(args[2]);
        if (itemOpt.isEmpty()) {
            sender.sendMessage(Component.text("Unknown item id."));
            return true;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(Component.text("Amount must be a number."));
                return true;
            }
        }
        ItemStack stack = itemFactory.createItem(itemOpt.get(), amount);
        target.getInventory().addItem(stack);
        sender.sendMessage(Component.text("Gave " + amount + "x " + itemOpt.get().displayName() + " to " + target.getName()));
        return true;
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can spawn mobs."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /nobleitems spawn <mobId>"));
            return true;
        }
        if (mobSpawner.spawn(player, args[1]).isPresent()) {
            sender.sendMessage(Component.text("Spawned mob."));
        } else {
            sender.sendMessage(Component.text("Unknown mob id."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(List.of("reload", "give", "spawn", "buildpack"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            itemRegistry.values().forEach(item -> completions.add(item.id()));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            mobRegistry.values().forEach(mob -> completions.add(mob.id()));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("buildpack")) {
            completions.add("nobleitems-resourcepack.zip");
        }
        return completions;
    }
}
