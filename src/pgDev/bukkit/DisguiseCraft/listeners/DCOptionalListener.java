package pgDev.bukkit.DisguiseCraft.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import pgDev.bukkit.DisguiseCraft.disguise.*;
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
			if (disguise.type.isPlayer() || disguise.type == DisguiseType.Zombie || disguise.type == DisguiseType.PigZombie || disguise.type == DisguiseType.Skeleton) {
				ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
				plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.packetGenerator.getEquipmentChangePacket((short) 0, heldItem));
			}
		}
	}
	
	@EventHandler
	public void onAnimation(PlayerAnimationEvent event) {
		if (!event.isCancelled()) {
			if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
				if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
					Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
					if (disguise.type.isPlayer() || disguise.type == DisguiseType.IronGolem) {
						plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.packetGenerator.getAnimationPacket(1));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (!event.isCancelled()) {
			if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
				Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
				if (disguise.type.isPlayer()) {
					disguise.setCrouch(event.isSneaking());
					plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.packetGenerator.getEntityMetadataPacket());
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
					plugin.sendPacketToWorld(player.getWorld(), plugin.disguiseDB.get(player.getName()).packetGenerator.getAnimationPacket(2));
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
				plugin.sendPacketToWorld(player.getWorld(), plugin.disguiseDB.get(player.getName()).packetGenerator.getStatusPacket(3));
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
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPickup(PlayerPickupItemEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			if (plugin.disguiseDB.containsKey(player.getName())) {
				if (!plugin.disguiseDB.get(player.getName()).type.isObject()) {
					plugin.sendPacketToWorld(player.getWorld(), plugin.disguiseDB.get(player.getName()).packetGenerator.getPickupPacket(event.getItem().getEntityId()));
				}
			}
		}
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
