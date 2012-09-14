package pgDev.bukkit.DisguiseCraft.injection;

import org.bukkit.craftbukkit.CraftServer;

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