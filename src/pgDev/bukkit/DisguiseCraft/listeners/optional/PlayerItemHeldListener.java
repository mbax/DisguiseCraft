package pgDev.bukkit.DisguiseCraft.listeners.optional;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

public class PlayerItemHeldListener implements Listener {
	final DisguiseCraft plugin;
	
	public PlayerItemHeldListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onHeldItemChange(PlayerItemHeldEvent event) {
		if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
			Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
			if (disguise.type.isPlayer() || disguise.type == DisguiseType.Zombie || disguise.type == DisguiseType.PigZombie || disguise.type == DisguiseType.Skeleton) {
				ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
				plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.packetGenerator.getEquipmentChangePacket((short) 0, heldItem));
			}
		}
	}
}
