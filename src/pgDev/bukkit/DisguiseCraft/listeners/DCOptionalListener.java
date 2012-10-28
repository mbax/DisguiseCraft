package pgDev.bukkit.DisguiseCraft.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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
					plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.getAnimationPacket(1));
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
					plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.getEntityMetadataPacket());
				}
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!event.isCancelled()) {
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				if (plugin.disguiseDB.containsKey(player.getName())) {
					// Send the damage animation
					plugin.sendPacketToWorld(player.getWorld(), plugin.disguiseDB.get(player.getName()).getAnimationPacket(2));
				}
			}
		}
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (plugin.disguiseDB.containsKey(player.getName())) {
				// Send death packet
				plugin.sendPacketToWorld(player.getWorld(), plugin.disguiseDB.get(player.getName()).getStatusPacket(3));
			}
		}
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
