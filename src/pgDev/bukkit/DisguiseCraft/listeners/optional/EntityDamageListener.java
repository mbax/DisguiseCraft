package pgDev.bukkit.DisguiseCraft.listeners.optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class EntityDamageListener implements Listener {
	final DisguiseCraft plugin;
	
	public EntityDamageListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!event.isCancelled()) {
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				if (plugin.disguiseDB.containsKey(player.getName())) {
					// Send the damage animation
					plugin.sendPacketToWorld(player.getWorld(), plugin.disguiseDB.get(player.getName()).packetGenerator.getAnimationPacket(2));
				}
			}
		}
	}
}
