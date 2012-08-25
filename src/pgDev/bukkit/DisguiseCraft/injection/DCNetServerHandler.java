package pgDev.bukkit.DisguiseCraft.injection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.craftbukkit.CraftServer;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet7UseEntity;

public class DCNetServerHandler extends NetServerHandler {

	private MinecraftServer minecraftServer;
	private final CraftServer server;

	public DCNetServerHandler(MinecraftServer minecraftserver, INetworkManager networkmanager, EntityPlayer entityplayer) {
		super(minecraftserver, networkmanager, entityplayer);
		this.minecraftServer = minecraftserver;
		this.server = minecraftserver.server;
	}
	
	public void copyFields(NetServerHandler from, String... excludes) {
		for (Field f : from.getClass().getDeclaredFields()) {
			if (excludes != null && Arrays.asList(excludes).contains(f.getName())) continue;
			try {
				f.setAccessible(true);
				f.set(this, f.get(from));
			} catch (IllegalArgumentException e) {
				DisguiseCraft.logger.log(Level.WARNING, "A DCNSH instance could not set the field: " + f.getName(), e);
			} catch (IllegalAccessException e) {
				DisguiseCraft.logger.log(Level.WARNING, "A DCNSH instance could not access the field:" + f.getName(), e);
			}
		}
	}
	
	@Override
	public void a(Packet7UseEntity packet7useentity) {
		// Normal Functionality
        super.a(packet7useentity);
        
        // The special part
        if (this.minecraftServer.getWorldServer(this.player.dimension).getEntity(packet7useentity.target) == null) {
        	PlayerInvalidInteractEvent event = new PlayerInvalidInteractEvent((org.bukkit.entity.Player) this.getPlayer(), packet7useentity.target, packet7useentity.action);
            this.server.getPluginManager().callEvent(event);
        }
    }
}