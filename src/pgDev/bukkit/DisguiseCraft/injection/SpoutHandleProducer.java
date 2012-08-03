package pgDev.bukkit.DisguiseCraft.injection;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;

public class SpoutHandleProducer {
	
	public static NetServerHandler getHandle(MinecraftServer minecraftserver, INetworkManager networkmanager, EntityPlayer entityplayer) {
		return new DCSpoutNetServerHandler(minecraftserver, networkmanager, entityplayer);
	}
}