package pgDev.bukkit.DisguiseCraft.debug;

import java.lang.reflect.Field;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet24MobSpawn;
import net.minecraft.server.Packet28EntityVelocity;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet30Entity;
import net.minecraft.server.Packet31RelEntityMove;
import net.minecraft.server.Packet32EntityLook;
import net.minecraft.server.Packet33RelEntityMoveLook;
import net.minecraft.server.Packet34EntityTeleport;

import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.packet.listener.PacketListener;
import org.getspout.spoutapi.packet.standard.MCPacket;
import org.getspout.spoutapi.packet.standard.MCPacketUnknown;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DebugPacketOutput implements PacketListener {
	//24 = entity spawn
	//28 = entity velocity
	//29 = destroy entity
	//30 = just an entity packet
	//31 = entity relative move
	//32 = entity look
	//33 = Entity Look and Relative Move
	//34 = Entity Teleport
	
	DisguiseCraft plugin;
	
	int[] listenPackets = new int[] {24, 28, 29, 30, 31, 32, 33, 34}; // The entity look related packets
	
	public DebugPacketOutput(DisguiseCraft plugin) {
		this.plugin = plugin;
		
		for (int id : listenPackets) {
			SpoutManager.getPacketManager().addListener(id, this);
		}
	}

	@Override
	public boolean checkPacket(Player player, MCPacket packet) {
		Packet packt = (Packet) ((MCPacketUnknown) packet).getRawPacket();
		if (packet.getId() == 24) {
			Packet24MobSpawn mpacket = (Packet24MobSpawn)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity has been spawned with ID " + mpacket.a);
			System.out.println("Entity " + mpacket.a + " mob type: " + mpacket.b);
			System.out.println("Entity " + mpacket.a + " X: " + mpacket.c);
			System.out.println("Entity " + mpacket.a + " Y: " + mpacket.d);
			System.out.println("Entity " + mpacket.a + " Z: " + mpacket.e);
			System.out.println("Entity " + mpacket.a + " Yaw: " + mpacket.f);
			System.out.println("Entity " + mpacket.a + " Pitch: " + mpacket.g);
			try {
				Field metadataField = packet.getClass().getDeclaredField("h");
				metadataField.setAccessible(true);
				System.out.println("Entity " + mpacket.a + " MetaData: " + metadataField.get(mpacket));
			} catch (Exception e) {
				
			}
		}else if (packet.getId() == 28) {
			Packet28EntityVelocity mpacket = (Packet28EntityVelocity)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity has been set a velocity with ID " + mpacket.a);
			System.out.println("Entity " + mpacket.a + " X velocity: " + mpacket.b);
			System.out.println("Entity " + mpacket.a + " Y velocity: " + mpacket.c);
			System.out.println("Entity " + mpacket.a + " Z velocity: " + mpacket.d);
		}else if (packet.getId() == 29) {
			Packet29DestroyEntity mpacket = (Packet29DestroyEntity)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity has been destroyed with ID " + mpacket.a);
		}else if (packet.getId() == 30) {
			Packet30Entity mpacket = (Packet30Entity)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity packet sent for entity ID " + mpacket.a);
		}else if (packet.getId() == 31) {
			Packet31RelEntityMove mpacket = (Packet31RelEntityMove)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity relative move packet sent for entity ID " + mpacket.a);
			System.out.println("Entity " + mpacket.a + " relative X: " + mpacket.b);
			System.out.println("Entity " + mpacket.a + " relative Y: " + mpacket.c);
			System.out.println("Entity " + mpacket.a + " relative Z: " + mpacket.d);
		}else if (packet.getId() == 32) {
			Packet32EntityLook mpacket = (Packet32EntityLook)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity look packet sent for entity ID " + mpacket.a);
			System.out.println("Entity " + mpacket.a + " b: " + mpacket.b);
			System.out.println("Entity " + mpacket.a + " c: " + mpacket.c);
			System.out.println("Entity " + mpacket.a + " d: " + mpacket.d);
			System.out.println("Entity " + mpacket.a + " e: " + mpacket.e);
			System.out.println("Entity " + mpacket.a + " f: " + mpacket.f);
			System.out.println("Entity " + mpacket.a + " g: " + mpacket.g);
		}else if (packet.getId() == 33) {
			Packet33RelEntityMoveLook mpacket = (Packet33RelEntityMoveLook)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity move and look packet sent for entity ID " + mpacket.a);
			System.out.println("Entity " + mpacket.a + " X: " + mpacket.b);
			System.out.println("Entity " + mpacket.a + " Y: " + mpacket.c);
			System.out.println("Entity " + mpacket.a + " Z: " + mpacket.d);
			System.out.println("Entity " + mpacket.a + " Yaw: " + mpacket.e);
			System.out.println("Entity " + mpacket.a + " Pitch: " + mpacket.f);
		}else if (packet.getId() == 34) {
			Packet34EntityTeleport mpacket = (Packet34EntityTeleport)packt;
			if(!plugin.disguisedentID.containsKey(Integer.toString(mpacket.a))) {
				return true;
			}
			System.out.println("Entity teleport packet sent for entity ID " + mpacket.a);
			System.out.println("Entity " + mpacket.a + " X: " + mpacket.b);
			System.out.println("Entity " + mpacket.a + " Y: " + mpacket.c);
			System.out.println("Entity " + mpacket.a + " Z: " + mpacket.d);
			System.out.println("Entity " + mpacket.a + " Yaw: " + mpacket.e);
			System.out.println("Entity " + mpacket.a + " Pitch: " + mpacket.f);
		}
		return true;
	}

}
