package net.minelink.ctplus;

import net.minelink.ctplus.event.NpcDespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NpcManager {

    private final CombatTagPlus plugin;

    private final Map<UUID, Npc> spawnedNpcs = new HashMap<>();

    NpcManager(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    public Npc spawn(Player player) {
        // Do nothing if player already has a NPC
        Npc npc = getSpawnedNpc(player.getUniqueId());
        if (npc != null) return null;

        // Spawn fake player entity
        npc = new Npc(plugin.getNpcPlayerHelper(), plugin.getNpcPlayerHelper().spawn(player));
        spawnedNpcs.put(player.getUniqueId(), npc);

        Player entity = npc.getEntity();

        entity.setCanPickupItems(false);
        entity.setNoDamageTicks(0);

        // Copy player data to fake player
        entity.setHealthScale(player.getHealthScale());
        entity.setMaxHealth(player.getMaxHealth());
        entity.setHealth(player.getHealth());
        entity.setTotalExperience(player.getTotalExperience());
        entity.setFoodLevel(player.getFoodLevel());
        entity.setExhaustion(player.getExhaustion());
        entity.setSaturation(player.getSaturation());
        entity.setFireTicks(player.getFireTicks());
        entity.getInventory().setContents(player.getInventory().getContents());
        entity.getInventory().setArmorContents(player.getInventory().getArmorContents());
        entity.addPotionEffects(player.getActivePotionEffects());

        // Should fix some visual glitches, such as health bars displaying zero
        entity.teleport(player, PlayerTeleportEvent.TeleportCause.PLUGIN);

        // Send equipment packets to nearby players
        plugin.getNpcPlayerHelper().updateEquipment(entity);

        // Play a nice little effect indicating the NPC was spawned
        if (plugin.getSettings().playEffect()) {
            Location l = entity.getLocation();
            l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, 0, 64);
            l.getWorld().playSound(l, Sound.EXPLODE, 0.9F, 0);
        }

        return npc;
    }

    public void despawn(Npc npc) {
        // Do nothing if NPC isn't spawned or if it's a different NPC
        Npc other = getSpawnedNpc(npc.getIdentity().getId());
        if (other == null || other != npc) return;

        // Call NPC despawn event
        NpcDespawnEvent event = new NpcDespawnEvent(npc);
        Bukkit.getPluginManager().callEvent(event);

        // Remove the NPC entity from the world
        plugin.getNpcPlayerHelper().despawn(npc.getEntity());
        spawnedNpcs.remove(npc.getIdentity().getId());
    }

    public Npc getSpawnedNpc(UUID playerId) {
        return spawnedNpcs.get(playerId);
    }

    public boolean npcExists(UUID playerId) {
        return spawnedNpcs.containsKey(playerId);
    }

}
