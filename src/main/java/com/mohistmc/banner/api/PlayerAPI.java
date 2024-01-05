package com.mohistmc.banner.api;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerAPI {

    public static Map<SocketAddress, Integer> mods = new ConcurrentHashMap<>();
    public static Map<SocketAddress, String> modlist = new ConcurrentHashMap<>();

    /**
     * Get Player ping
     *
     * @param player org.bukkit.entity.player
     */
    public static String getPing(Player player) {
        return String.valueOf(getNMSPlayer(player).connection.latency());
    }

    public static ServerPlayer getNMSPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static Player getCBPlayer(ServerPlayer player) {
        return player.getBukkitEntity().getPlayer();
    }

    // Don't count the default number of mods
    public static int getModSize(Player player) {
        SocketAddress socketAddress = getRemoteAddress(player);
        return mods.get(socketAddress) == null ? 0 : mods.get(socketAddress) - 2;
    }

    public static String getModlist(Player player) {
        SocketAddress socketAddress = getRemoteAddress(player);
        return modlist.get(socketAddress) == null ? "null" : modlist.get(socketAddress);
    }

    public static Boolean hasMod(Player player, String modid) {
        return getModlist(player).contains(modid);
    }

    public static boolean isOp(ServerPlayer ep) {
        return BukkitExtraConstants.getServer().getPlayerList().isOp(ep.getGameProfile());
    }

    public static SocketAddress getRemoteAddress(Player player) {
        return getNMSPlayer(player).connection.connection.getRemoteAddress();
    }
}

