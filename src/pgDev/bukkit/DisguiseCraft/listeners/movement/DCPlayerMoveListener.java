package pgDev.bukkit.DisguiseCraft.listeners.movement;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DCPlayerMoveListener implements Listener {
	final DisguiseCraft plugin;
	
	public DCPlayerMoveListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		// Track player movements in order to synchronize their disguise
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			plugin.sendMovement(event.getPlayer(), null, event.getPlayer().getVelocity(), event.getTo());
		}
	}
}
