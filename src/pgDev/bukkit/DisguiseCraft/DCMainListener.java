package pgDev.bukkit.DisguiseCraft;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;

public class DCMainListener implements Listener {
	final DisguiseCraft plugin;
	
	public DCMainListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			plugin.sendMovement(event.getPlayer(), null, event.getFrom(), event.getTo());
		}
	}
}
