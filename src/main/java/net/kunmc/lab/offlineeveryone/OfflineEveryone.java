package net.kunmc.lab.offlineeveryone;

import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OfflineEveryone extends JavaPlugin {
    public static Logger LOGGER;
    private OfflineOperator offlineOperator;

    @Override
    public void onEnable() {
        // Plugin startup logic
        LOGGER = getLogger();

        Optional<World> optionalWorld = Bukkit.getWorlds().stream()
                .filter(e -> World.Environment.NORMAL.equals(e.getEnvironment()))
                .findFirst();

        if (!optionalWorld.isPresent()) {
            LOGGER.severe("Failed to get world");
            return;
        }

        offlineOperator = new OfflineOperator(optionalWorld.get());
    }

    private static double parseDouble(String number, double base) {
        if (number == null)
            return 0;
        double result = 0;
        if (number.startsWith("~")) {
            result += base;
            number = number.substring(1);
        }
        try {
            result += Double.parseDouble(number);
        } catch (NumberFormatException ignored) {
        }
        return result;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "引数が足りません。");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "使い方: /everyone <clear-spawnpoint|clear-inventory|gamemode|tp>");
            return true;
        }

        switch (args[0]) {
            case "clear-spawnpoint": {
                offlineOperator.clearSpawnPoint();
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.GREEN + "全員のスポーンポイントを消去しました。");
                return true;
            }

            case "gamemode": {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "引数が足りません。");
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "使い方: /everyone gamemode <creative|survival|adventure|spectator>");
                    return true;
                }
                try {
                    offlineOperator.setGameMode(GameMode.valueOf(args[1].toUpperCase()));
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.GREEN + "全員のゲームモードを変更しました。");
                    return true;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "引数が不正です。");
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "使い方: /everyone gamemode <creative|survival|adventure|spectator>");
                    return true;
                }
            }

            case "tp": {
                Location loc = new Location(offlineOperator.getWorld(), 0, 0, 0);
                if (sender instanceof Player)
                    loc = ((Player) sender).getLocation();
                else if (sender instanceof BlockCommandSender)
                    loc = ((BlockCommandSender) sender).getBlock().getLocation();

                if (args.length == 4) {
                    double x = parseDouble(args[1], loc.getX());
                    double y = parseDouble(args[2], loc.getY());
                    double z = parseDouble(args[3], loc.getZ());
                    offlineOperator.setLocation(new Location(loc.getWorld(), x, y, z));
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.GREEN + "全員をTPしました。");
                } else {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (args.length == 2 && player != null) {
                        offlineOperator.setLocation(player.getLocation());
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.GREEN + "全員を" + player.getName() + "にTPしました。");
                    } else {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "プレイヤー、または座標が不正です。");
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "使い方: /everyone tp <x y z|~dx ~dy ~dz|player>");
                    }
                }
                return true;
            }

            case "clear-inventory": {
                offlineOperator.clearInventory();
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.GREEN + "全員のインベントリを消去しました。");
                return true;
            }

            default: {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "サブコマンドが見つかりません。");
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "使い方: /everyone <clear-spawnpoint|clear-inventory|gamemode|tp>");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        switch (args.length) {
            case 1:
                return Stream.of("clear-spawnpoint", "clear-inventory", "gamemode", "tp")
                        .filter(e -> e.startsWith(args[0]))
                        .collect(Collectors.toList());
            case 2:
                switch (args[0]) {
                    case "gamemode":
                        return Stream.of("creative", "survival", "adventure", "spectator")
                                .filter(e -> e.startsWith(args[1]))
                                .collect(Collectors.toList());
                    case "tp":
                        return Stream.concat(
                                Stream.of("~ ~ ~", "0 0 0"),
                                Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        )
                                .filter(e -> e.startsWith(args[1]))
                                .collect(Collectors.toList());
                }
                break;
        }

        return Collections.emptyList();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
