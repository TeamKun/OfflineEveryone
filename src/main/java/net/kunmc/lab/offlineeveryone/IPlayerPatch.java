package net.kunmc.lab.offlineeveryone;

import net.querz.nbt.tag.CompoundTag;
import org.bukkit.entity.Player;

public interface IPlayerPatch<T> {
    void applyOfflinePlayer(CompoundTag nbt, T parameter);

    void applyOnlinePlayer(Player player, T parameter);
}
