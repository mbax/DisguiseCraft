package pgDev.bukkit.DisguiseCraft.injection;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet7UseEntity;

import org.bukkit.craftbukkit.CraftServer;
import org.getspout.spout.SpoutNetServerHandler;

public class DCSpoutNetServerHandler extends SpoutNetServerHandler implements DCHandler {
	
	private MinecraftServer minecraftServer;
	private final CraftServer server;

	public DCSpoutNetServerHandler(MinecraftServer minecraftserver, INetworkManager networkmanager, EntityPlayer entityplayer) {
		super(minecraftserver, (NetworkManager) networkmanager, entityplayer);
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
