package pgDev.bukkit.DisguiseCraft.listeners;

import net.minecraft.server.Packet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

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
    		Packet revivePacket = disguise.getMobSpawnPacket(disguisee.getLocation());
    		Packet revivePlayerPacket = disguise.getPlayerSpawnPacket(disguisee.getLocation(), (short) disguisee.getItemInHand().getTypeId());
			Packet reviveListPacket = disguise.getPlayerInfoPacket(disguisee, true);
    		
			// Remove his disguise from the old world
			if (killListPacket == null) {
				plugin.undisguiseToWorld(event.getFrom(), disguisee, killPacket);
			} else {
				plugin.undisguiseToWorld(event.getFrom(), disguisee, killPacket, killListPacket);
			}
			
			// Show the disguise to the people in the new world
			if (disguise.isPlayer()) {
				plugin.disguiseToWorld(disguisee.getWorld(), disguisee, revivePlayerPacket, reviveListPacket);
			} else {
				plugin.disguiseToWorld(disguisee.getWorld(), disguisee, revivePacket);
			}
			
			/*
			// Permissions check
			if ((disguise.isPlayer() && !plugin.hasPermissions(disguisee, "disguisecraft.player"))
					|| (Arrays.asList(disguise.data.split(",")).contains("baby") && !plugin.hasPermissions(disguisee, "disguisecraft.mob." + disguise.mob.name().toLowerCase() + ".baby"))
					|| (disguise.data == null && !plugin.hasPermissions(disguisee, "disguisecraft.mob." + disguise.mob.name().toLowerCase()))) {
				// Pass the event
				PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(disguisee);
				plugin.getServer().getPluginManager().callEvent(ev);
				if (!ev.isCancelled()) {
					plugin.unDisguisePlayer(disguisee);
					disguisee.sendMessage(ChatColor.RED + "You've been undisguised because you do not have permissions to wear that disguise in this world.");
				} else {
					// Show the disguise to the people in the new world
					if (reviveListPacket == null) {
						(new Timer()).schedule(new WorldDisguiseTask(plugin, disguisee.getWorld(), disguisee, revivePacket), 600);
					} else {
						(new Timer()).schedule(new WorldDisguiseTask(plugin, disguisee.getWorld(), disguisee, revivePacket, reviveListPacket), 600);
					}
				}
			} else {
				// Show the disguise to the people in the new world
				if (reviveListPacket == null) {
					(new Timer()).schedule(new WorldDisguiseTask(plugin, disguisee.getWorld(), disguisee, revivePacket), 600);
				} else {
					(new Timer()).schedule(new WorldDisguiseTask(plugin, disguisee.getWorld(), disguisee, revivePacket, reviveListPacket), 600);
				}
			}*/
		}
		
		// World Change is like a join
		plugin.showWorldDisguises(event.getPlayer());
	}
}
