package net.minelink.ctplus.compat.v1_7_R4;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minelink.ctplus.compat.api.NpcIdentity;
import net.minelink.ctplus.compat.api.NpcNameGenerator;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class NpcPlayer extends EntityPlayer {

    private NpcIdentity identity;

    private NpcPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    }

    public NpcIdentity getNpcIdentity() {
        return identity;
    }

    public static NpcPlayer valueOf(Player player) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        WorldServer worldServer = minecraftServer.getWorldServer(0);
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), NpcNameGenerator.generate());

        for (Map.Entry<String, Property> entry: ((CraftPlayer) player).getProfile().getProperties().entries()) {
            gameProfile.getProperties().put(entry.getKey(), entry.getValue());
        }

        NpcPlayer npcPlayer = new NpcPlayer(minecraftServer, worldServer, gameProfile, playerInteractManager);
        npcPlayer.identity = new NpcIdentity(player);

        new NpcPlayerConnection(npcPlayer);

        return npcPlayer;
    }

}