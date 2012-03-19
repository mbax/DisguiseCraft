package pgDev.bukkit.DisguiseCraft.listeners;

import org.bukkit.event.*;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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
			if (disguise.isPlayer()) {
				ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
				plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.getEquipmentChangePacket((short) 0, heldItem));
			}
		}
	}
	
	@EventHandler
	public void onAnimation(PlayerAnimationEvent event) {
		if (!event.isCancelled()) {
			// Just a quick check just in case
			if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
				return;
			}
			
			if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
				Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
				if (disguise.isPlayer()) {
					plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.getAnimationPacket(event.getAnimationType()));
				}
			}
		}
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (!event.isCancelled()) {
			if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
				Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
				if (disguise.isPlayer()) {
					disguise.setCrouch(event.isSneaking());
					plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.getMetadataPacket());
				}
			}
		}
	}
}
