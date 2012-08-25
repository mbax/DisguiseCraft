package pgDev.bukkit.DisguiseCraft.listeners;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.NetServerHandler;

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.Disguise.MobType;
import pgDev.bukkit.DisguiseCraft.injection.DCHandler;
import pgDev.bukkit.DisguiseCraft.injection.DCNetServerHandler;
import pgDev.bukkit.DisguiseCraft.injection.OrebfuscatorHandleProducer;
import pgDev.bukkit.DisguiseCraft.injection.PlayerInvalidInteractEvent;

public class DCCustomListener implements Listener {
	final DisguiseCraft plugin;
	
	public DCCustomListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDisguiseHit(PlayerInvalidInteractEvent event) {
		if (event.getAction()) {
			Player attacked = plugin.getPlayerFromDisguiseID(event.getTarget());
			if (attacked != null) {
				// Do the attack
				((CraftPlayer) event.getPlayer()).getHandle().attack(((CraftPlayer) attacked).getHandle());
			}
		} else {
			if (event.getPlayer().getItemInHand().getType() == Material.SHEARS) {
				Player clicked = plugin.getPlayerFromDisguiseID(event.getTarget());
				if (clicked != null) {
					Disguise disguise = plugin.disguiseDB.get(clicked.getName());
					if (disguise.mob != null && disguise.mob == MobType.MushroomCow) {
						((CraftPlayer) event.getPlayer()).getHandle().netServerHandler.sendPacket(disguise.getMobSpawnPacket(clicked.getLocation()));
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Injection
		Player player = event.getPlayer();
		if (DisguiseCraft.pluginSettings.disguisePVP && player instanceof CraftPlayer) {
			EntityPlayer entity = ((CraftPlayer)player).getHandle();
			if (!(entity.netServerHandler instanceof DCHandler)) {
				NetServerHandler newHandler;
				if (plugin.getServer().getPluginManager().getPlugin("Orebfuscator") != null) { // Orebfuscator
					newHandler = OrebfuscatorHandleProducer.getHandle(entity.server, entity.netServerHandler);
				} else { // DisguiseCraft
					entity.netServerHandler.disconnected = true;
					newHandler = new DCNetServerHandler(entity.server, entity.netServerHandler.networkManager, entity);
					((DCNetServerHandler) newHandler).copyFields(entity.netServerHandler, "disconnected");
					entity.netServerHandler.networkManager.a(newHandler);
					entity.server.ac().a(newHandler);
				}
			}
		}
	}
}
