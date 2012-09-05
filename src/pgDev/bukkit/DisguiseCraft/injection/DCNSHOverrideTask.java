package pgDev.bukkit.DisguiseCraft.injection;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.NetServerHandler;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class DCNSHOverrideTask implements Runnable {

	final Player player;
	
	public DCNSHOverrideTask(Player player) {
		this.player = player;
	}
	
	public void run() {
		if (player.isOnline()) {
			EntityPlayer entity = ((CraftPlayer)player).getHandle();
			if (!(entity.netServerHandler instanceof DCHandler)) {
				NetServerHandler newHandler;
				if (Bukkit.getServer().getPluginManager().getPlugin("Orebfuscator") != null) { // Orebfuscator
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
