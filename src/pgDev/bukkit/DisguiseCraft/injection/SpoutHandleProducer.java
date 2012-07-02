package pgDev.bukkit.DisguiseCraft.injection;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;

public class SpoutHandleProducer {
	
	public static NetServerHandler getHandle(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
		return new DCSpoutNetServerHandler(minecraftserver, networkmanager, entityplayer);
	}
}