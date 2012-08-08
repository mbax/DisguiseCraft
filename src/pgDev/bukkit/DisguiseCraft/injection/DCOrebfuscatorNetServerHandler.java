package pgDev.bukkit.DisguiseCraft.injection;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet7UseEntity;

import org.bukkit.craftbukkit.CraftServer;

import com.lishid.orebfuscator.hook.OrebfuscatorNetServerHandler;

public class DCOrebfuscatorNetServerHandler extends OrebfuscatorNetServerHandler implements DCHandler {
	
	private MinecraftServer minecraftServer;
	private final CraftServer server;

	public DCOrebfuscatorNetServerHandler(MinecraftServer minecraftserver, NetServerHandler instance) {
		super(minecraftserver, instance);
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
