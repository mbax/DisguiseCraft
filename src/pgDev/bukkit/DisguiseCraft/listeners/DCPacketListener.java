package pgDev.bukkit.DisguiseCraft.listeners;

import java.util.concurrent.ConcurrentLinkedQueue;
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

import pgDev.bukkit.DisguiseCraft.*;

public class DCPacketListener {
	final DisguiseCraft plugin;
	ProtocolManager pM = DisguiseCraft.protocolManager;
	
	public ConcurrentLinkedQueue<String> recentlyDisguised;
	
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
			            } catch (FieldAccessException e) {
			                DisguiseCraft.logger.log(Level.SEVERE, "Couldn't access a field in an 0x07-UseEntity packet!", e);
			            }
			        }
			    }
		});
	}
	
	public void setupTabListListener() {
		// Make database
		recentlyDisguised = new ConcurrentLinkedQueue<String>();
		
		// Set up listener
		pM.addPacketListener(new PacketAdapter(plugin,
			ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 0xC9) {
			    @Override
			    public void onPacketSending(PacketEvent event) {
			        if (event.getPacketID() == 0xC9) {
			        	try {
				        	if (recentlyDisguised.contains(event.getPacket().getStrings().read(0))) {
				        		event.setCancelled(true);
				        	}
			        	} catch (FieldAccessException e) {
			                DisguiseCraft.logger.log(Level.SEVERE, "Couldn't access a field in an 0xC9-PlayerInfo packet!", e);
			            }
			        }
			    }
		});
	}
	
	public void addHiddenName(String name) {
		if (DisguiseCraft.pluginSettings.noTabHide) {
			recentlyDisguised.add(name);
		}
	}
}
