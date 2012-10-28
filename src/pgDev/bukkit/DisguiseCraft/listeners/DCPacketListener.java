package pgDev.bukkit.DisguiseCraft.listeners;

import java.util.logging.Level;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DCPacketListener {
	final DisguiseCraft plugin;
	ProtocolManager pM = DisguiseCraft.protocolManager;
	
	public DCPacketListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
		setupListeners();
	}
	
	void setupListeners() {
		pM.addPacketListener(new PacketAdapter(plugin,
			ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL, 0x07) {
			    @Override
			    public void onPacketReceiving(PacketEvent event) {
			    	Player player = event.getPlayer();
			        if (event.getPacketID() == 0x07) {
			            try {
			            	CraftPlayer cPlayer = (CraftPlayer) player;
			            	CraftServer cServer = (CraftServer) cPlayer.getServer();
			            	
			                PacketContainer packet = event.getPacket();
			                int target = packet.getSpecificModifier(int.class).read(1);
			                int action = packet.getSpecificModifier(int.class).read(2);
			                
			                if (packet.getEntityModifier(player.getWorld()).read(1) == null) {
			                	PlayerInvalidInteractEvent newEvent = new PlayerInvalidInteractEvent(player, target, action);
			                    cServer.getPluginManager().callEvent(newEvent);
			                }
			                /* Replaced by ProtocolLib API method
			                if (cServer.getServer().getWorldServer(cPlayer.getHandle().dimension).getEntity(target) == null) {
			                	PlayerInvalidInteractEvent newEvent = new PlayerInvalidInteractEvent(player, target, action);
			                    cServer.getPluginManager().callEvent(newEvent);
			                }*/
			            } catch (FieldAccessException e) {
			                DisguiseCraft.logger.log(Level.SEVERE, "Couldn't access a field in an 0x07-UseEntity packet!", e);
			            }
			        }
			    }
		});
	}
}
