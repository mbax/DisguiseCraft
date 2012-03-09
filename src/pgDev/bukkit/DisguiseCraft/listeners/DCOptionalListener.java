package pgDev.bukkit.DisguiseCraft.listeners;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DCOptionalListener implements Listener {
	final DisguiseCraft plugin;
	
	public DCOptionalListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onHeldItemChange(PlayerItemHeldEvent event) {
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
			ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
			plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.getEquipmentChangePacket((short) 0, heldItem));
		}
	}
}
