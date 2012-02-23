package pgDev.bukkit.DisguiseCraft;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

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
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Show disguises to newly joined players
		for (String disguisedName : plugin.disguiseDB.keySet()) {
			Player disguised = plugin.getServer().getPlayer(disguisedName);
			if (disguised != null) {
				if (disguised.getWorld() == event.getPlayer().getWorld()) {
					plugin.sendDisguise(disguised, event.getPlayer());
				}
			}
		}
		
		// If he was a disguise-quitter, tell him
		if (plugin.disguiseQuitters.contains(event.getPlayer().getName())) {
			event.getPlayer().sendMessage(ChatColor.RED + "You were undisguised because you left the server.");
			plugin.disguiseQuitters.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// Undisguise them because they left
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			plugin.unDisguisePlayer(event.getPlayer());
			plugin.disguiseQuitters.add(event.getPlayer().getName());
		}
	}
}
