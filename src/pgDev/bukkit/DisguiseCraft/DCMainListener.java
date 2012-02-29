package pgDev.bukkit.DisguiseCraft;

import java.util.Timer;

import net.minecraft.server.Packet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import pgDev.bukkit.DisguiseCraft.delayedtasks.WorldDisguiseTask;

public class DCMainListener implements Listener {
	final DisguiseCraft plugin;
	
	public DCMainListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		// Track player movements in order to synchronize their disguise
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			plugin.sendMovement(event.getPlayer(), null, event.getPlayer().getVelocity(), event.getTo());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Show disguises to newly joined players
		plugin.showWorldDisguises(event.getPlayer());
		
		// If he was a disguise-quitter, tell him
		if (plugin.disguiseQuitters.contains(event.getPlayer().getName())) {
			event.getPlayer().sendMessage(ChatColor.RED + "You were undisguised because you left the server.");
			plugin.disguiseQuitters.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		
		// Undisguise them because they left
		if (plugin.disguiseDB.containsKey(player.getName())) {
			plugin.unDisguisePlayer(player);
			plugin.disguiseQuitters.add(player.getName());
		}
		
		// Undisguise others
		plugin.halfUndisguiseAllToPlayer(player);
	}
	
	@EventHandler
	public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
		// Handle disguise wearer going through a portal
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			Player disguisee = event.getPlayer();
			Disguise disguise = plugin.disguiseDB.get(disguisee.getName());
			
			// Packets
			Packet killPacket = disguise.getEntityDestroyPacket();
    		Packet killListPacket = disguise.getPlayerInfoPacket(disguisee, false);
    		Packet revivePacket = disguise.getPlayerSpawnPacket(disguisee.getLocation(), (short) disguisee.getItemInHand().getTypeId());
			Packet reviveListPacket = disguise.getPlayerInfoPacket(disguisee, true);
    		
			// Remove his disguise from the old world
			if (killListPacket == null) {
				plugin.undisguiseToWorld(event.getFrom(), disguisee, killPacket);
			} else {
				plugin.undisguiseToWorld(event.getFrom(), disguisee, killPacket, killListPacket);
			}
			
			// Show the disguise to the people in the new world
			if (reviveListPacket == null) {
				(new Timer()).schedule(new WorldDisguiseTask(plugin, disguisee.getWorld(), disguisee, revivePacket), 600);
			} else {
				(new Timer()).schedule(new WorldDisguiseTask(plugin, disguisee.getWorld(), disguisee, revivePacket, reviveListPacket), 600);
			}
		}
		
		// World Change is like a join
		plugin.showWorldDisguises(event.getPlayer());
	}
}
