package pgDev.bukkit.DisguiseCraft.injection;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;

public class OrebfuscatorHandleProducer {
	
	public static NetServerHandler getHandle(MinecraftServer minecraftserver, NetServerHandler instance) {
		return new DCOrebfuscatorNetServerHandler(minecraftserver, instance);
	}
}