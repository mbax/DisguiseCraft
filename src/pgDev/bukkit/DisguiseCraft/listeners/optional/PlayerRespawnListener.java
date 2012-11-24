package pgDev.bukkit.DisguiseCraft.listeners.optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class PlayerRespawnListener implements Listener {
	final DisguiseCraft plugin;
	
	public PlayerRespawnListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (plugin.disguiseDB.containsKey(player.getName())) {
			// Respawn disguise
			plugin.sendUnDisguise(player, null);
			plugin.sendDisguise(player, null);
		}
	}
}
