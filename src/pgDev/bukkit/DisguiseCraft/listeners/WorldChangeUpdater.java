package pgDev.bukkit.DisguiseCraft.listeners;

import java.util.LinkedList;

import net.minecraft.server.Packet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.PlayerUndisguiseEvent;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;

public class WorldChangeUpdater implements Runnable {
	final DisguiseCraft plugin;
	final PlayerChangedWorldEvent event;

	public WorldChangeUpdater(DisguiseCraft plugin, PlayerChangedWorldEvent event) {
		this.plugin = plugin;
		this.event = event;
	}
	
	@Override
	public void run() {
		// World Change is like a join
		plugin.showWorldDisguises(event.getPlayer());
		
		// Handle disguise wearer going through a portal
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			Player disguisee = event.getPlayer();
			Disguise disguise = plugin.disguiseDB.get(disguisee.getName());
			
			// Packets
			LinkedList<Packet> killPackets = new LinkedList<Packet>();
			LinkedList<Packet> revivePackets = new LinkedList<Packet>();
			killPackets.add(disguise.packetGenerator.getEntityDestroyPacket());
			revivePackets.add(disguise.packetGenerator.getSpawnPacket(disguisee));
			if (disguise.type.isPlayer()) {
				killPackets.add(disguise.packetGenerator.getPlayerInfoPacket(disguisee, false));
				revivePackets.add(disguise.packetGenerator.getPlayerInfoPacket(disguisee, true));
			}
    		
			// Remove his disguise from the old world
			plugin.undisguiseToWorld(event.getFrom(), disguisee, killPackets);
			
			if (disguise.hasPermission(disguisee)) {
				// Show the disguise to the people in the new world
				plugin.disguiseToWorld(disguisee.getWorld(), disguisee, revivePackets);
			} else {
				// Pass the event
				PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(disguisee);
				plugin.getServer().getPluginManager().callEvent(ev);
				if (ev.isCancelled()) {
					plugin.disguiseToWorld(disguisee.getWorld(), disguisee, revivePackets);
				} else {
					plugin.unDisguisePlayer(disguisee);
					disguisee.sendMessage(ChatColor.RED + "You've been undisguised because you do not have permissions to wear that disguise in this world.");
				}
			}
		}
	}

}
