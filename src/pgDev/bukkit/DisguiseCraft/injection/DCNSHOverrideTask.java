package pgDev.bukkit.DisguiseCraft.injection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.NetServerHandler;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DCNSHOverrideTask implements Runnable {

	final Player player;
	
	public DCNSHOverrideTask(Player player) {
		this.player = player;
	}
	
	public void run() {
		if (player.isOnline()) {
			EntityPlayer entity = ((CraftPlayer)player).getHandle();
			if (!(entity.netServerHandler instanceof DCHandler)) {
				NetServerHandler oldHandler = entity.netServerHandler;
				NetServerHandler newHandler;
				if (Bukkit.getServer().getPluginManager().getPlugin("Spout") != null) { // Spout
					newHandler = SpoutHandleProducer.getHandle(entity.server, entity.netServerHandler.networkManager, entity);
					copyFields(entity.netServerHandler, newHandler, "disconnected");
					newHandler.a(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
					entity.netServerHandler.networkManager.a(newHandler);
					entity.server.ac().a(newHandler);
					
					oldHandler.disconnected = true;
				} else if (Bukkit.getServer().getPluginManager().getPlugin("Orebfuscator") != null) { // Orebfuscator
					newHandler = OrebfuscatorHandleProducer.getHandle(entity.server, entity.netServerHandler);
					entity.netServerHandler = newHandler;
				} else { // DisguiseCraft
					newHandler = new DCNetServerHandler(entity.server, entity.netServerHandler.networkManager, entity);
					copyFields(entity.netServerHandler, newHandler, "disconnected");
					newHandler.a(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
					entity.netServerHandler.networkManager.a(newHandler);
					entity.server.ac().a(newHandler);
					
					oldHandler.disconnected = true;
				}
			}
		}
	}
	
	static public void copyFields(NetServerHandler from, NetServerHandler to, String... excludes) {
		for (Field f : from.getClass().getDeclaredFields()) {
			if (excludes != null && Arrays.asList(excludes).contains(f.getName())) continue;
			try {
				f.setAccessible(true);
				f.set(to, f.get(from));
			} catch (IllegalArgumentException e) {
				DisguiseCraft.logger.log(Level.WARNING, "A DCNSH instance could not set the field: " + f.getName(), e);
			} catch (IllegalAccessException e) {
				DisguiseCraft.logger.log(Level.WARNING, "A DCNSH instance could not access the field:" + f.getName(), e);
			}
		}
	}
}
