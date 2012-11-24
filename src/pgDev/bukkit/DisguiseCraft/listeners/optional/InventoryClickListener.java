package pgDev.bukkit.DisguiseCraft.listeners.optional;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.listeners.ArmorUpdater;

public class InventoryClickListener implements Listener {
	final DisguiseCraft plugin;
	
	public InventoryClickListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryChange(InventoryClickEvent event) {
		if (!event.isCancelled()) {
			HumanEntity entity = event.getWhoClicked();
			if (entity instanceof Player) {
				Player player = (Player) entity;
				if (plugin.disguiseDB.containsKey(player.getName())) {
					plugin.getServer().getScheduler().runTask(plugin, new ArmorUpdater(plugin, player, plugin.disguiseDB.get(player.getName())));
				}
			}
		}
	}
}
