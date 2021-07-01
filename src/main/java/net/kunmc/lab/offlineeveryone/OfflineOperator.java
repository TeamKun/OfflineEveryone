package net.kunmc.lab.offlineeveryone;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class OfflineOperator {
    private final World world;

    public OfflineOperator(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public <T> void applyForAllPlayers(IPlayerPatch<T> operator, T parameter) {
        File playerDir = new File(world.getWorldFolder(), "playerdata");
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            File playerData = new File(playerDir, player.getUniqueId() + ".dat");
            if (!playerData.exists())
                continue;

            NamedTag tag;
            try {
                tag = NBTUtil.read(playerData, true);
            } catch (IOException e) {
                OfflineEveryone.LOGGER.warning(String.format("Failed to read NBT: {Name:%s,UUID:%s}", player.getName(), player.getUniqueId()));
                OfflineEveryone.LOGGER.log(Level.FINE, String.format("Failed to read NBT: {Name:%s,UUID:%s}", player.getName(), player.getUniqueId()), e);
                continue;
            }

            if (!(tag.getTag() instanceof CompoundTag))
                continue;
            CompoundTag nbt = (CompoundTag) tag.getTag();
            operator.applyOfflinePlayer(nbt, parameter);

            try {
                NBTUtil.write(tag, playerData, true);
            } catch (IOException e) {
                OfflineEveryone.LOGGER.warning(String.format("Failed to read NBT: {Name:%s,UUID:%s}", player.getName(), player.getUniqueId()));
                OfflineEveryone.LOGGER.log(Level.FINE, String.format("Failed to read NBT: {Name:%s,UUID:%s}", player.getName(), player.getUniqueId()), e);
                continue;
            }

            ;
        }

        for (Player player : Bukkit.getOnlinePlayers())
            operator.applyOnlinePlayer(player, parameter);
    }

    public void clearSpawnPoint() {
        applyForAllPlayers(new IPlayerPatch<Void>() {
            @Override
            public void applyOfflinePlayer(CompoundTag nbt, Void parameter) {
                nbt.remove("SpawnX");
                nbt.remove("SpawnY");
                nbt.remove("SpawnZ");
                nbt.remove("SpawnWorld");
            }

            @Override
            public void applyOnlinePlayer(Player player, Void parameter) {
                player.setBedSpawnLocation(null, true);
            }
        }, null);
    }

    public void setGameMode(GameMode gameMode) {
        applyForAllPlayers(new IPlayerPatch<GameMode>() {
            private int getGameModeId(GameMode gameMode) {
                switch (gameMode) {
                    default:
                    case SURVIVAL:
                        return 0;
                    case CREATIVE:
                        return 1;
                    case ADVENTURE:
                        return 2;
                    case SPECTATOR:
                        return 3;
                }
            }

            @Override
            public void applyOfflinePlayer(CompoundTag nbt, GameMode gameMode) {
                nbt.putInt("playerGameType", getGameModeId(gameMode));
            }

            @Override
            public void applyOnlinePlayer(Player player, GameMode gameMode) {
                player.setGameMode(gameMode);
            }
        }, gameMode);
    }

    public void setLocation(Location location) {
        applyForAllPlayers(new IPlayerPatch<Location>() {
            @Override
            public void applyOfflinePlayer(CompoundTag nbt, Location location) {
                ListTag<DoubleTag> tag = new ListTag<>(DoubleTag.class);
                tag.add(new DoubleTag(location.getX()));
                tag.add(new DoubleTag(location.getY()));
                tag.add(new DoubleTag(location.getZ()));
                nbt.put("Pos", tag);
            }

            @Override
            public void applyOnlinePlayer(Player player, Location location) {
                player.teleport(location);
            }
        }, location);
    }

    public void clearInventory() {
        applyForAllPlayers(new IPlayerPatch<Void>() {
            @Override
            public void applyOfflinePlayer(CompoundTag nbt, Void parameter) {
                nbt.remove("Inventory");
            }

            @Override
            public void applyOnlinePlayer(Player player, Void parameter) {
                player.getInventory().clear();
            }
        }, null);
    }
}
