package pgDev.bukkit.DisguiseCraft.listeners.movement;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DCMovementAsyncListener implements Runnable {
	final DisguiseCraft plugin;
	
	public DCMovementAsyncListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		// Update the location of all disguises
		for (String playerName : plugin.disguiseDB.keySet()) {
			Player player;
			if ((player = plugin.getServer().getPlayer(playerName)) != null) {
				plugin.sendMovement(player, null, player.getVelocity(), player.getLocation());
			}
		}
	}

}
