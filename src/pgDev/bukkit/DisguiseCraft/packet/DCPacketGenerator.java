package pgDev.bukkit.DisguiseCraft.packet;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.logging.Level;

import net.minecraft.server.MathHelper;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.Packet201PlayerInfo;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet22Collect;
import net.minecraft.server.Packet23VehicleSpawn;
import net.minecraft.server.Packet24MobSpawn;
import net.minecraft.server.Packet28EntityVelocity;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet32EntityLook;
import net.minecraft.server.Packet33RelEntityMoveLook;
import net.minecraft.server.Packet34EntityTeleport;
import net.minecraft.server.Packet35EntityHeadRotation;
import net.minecraft.server.Packet38EntityStatus;
import net.minecraft.server.Packet40EntityMetadata;
import net.minecraft.server.Packet5EntityEquipment;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.*;

public class DCPacketGenerator {
	final Disguise d;
	
	protected int encposX;
	protected int encposY;
	protected int encposZ;
	protected boolean firstpos = true;
	
	public DCPacketGenerator(final Disguise disguise) {
		d = disguise;
	}
	
	// Vital packet methods
	public Packet getSpawnPacket(Player disguisee) {
		if (d.type.isMob()) {
			return getMobSpawnPacket(disguisee.getLocation());
		} else if (d.type.isPlayer()) {
			return getPlayerSpawnPacket(disguisee.getLocation(), (short) disguisee.getItemInHand().getTypeId());
		} else {
			return getObjectSpawnPacket(disguisee.getLocation());
		}
	}
	
	public LinkedList<Packet> getArmorPackets(Player player) {
		LinkedList<Packet> packets = new LinkedList<Packet>();
		ItemStack[] armor = player.getInventory().getArmorContents();
		for (byte i=0; i < armor.length; i++) {
			packets.add(getEquipmentChangePacket((short) (i + 1), armor[i]));
		}
		return packets;
	}
	
	// Individual packet generation methods
	public int[] getLocationVariables(Location loc) {
		int x = MathHelper.floor(loc.getX() *32D);
		int y = MathHelper.floor(loc.getY() *32D);
		int z = MathHelper.floor(loc.getZ() *32D);
		if(firstpos) {
			encposX = x;
			encposY = y;
			encposZ = z;
			firstpos = false;
		}
		return new int[] {x, y, z};
	}
	
	public Packet24MobSpawn getMobSpawnPacket(Location loc) {
		int[] locVars = getLocationVariables(loc);
		Packet24MobSpawn packet = new Packet24MobSpawn();
		packet.a = d.entityID;
		packet.b = d.type.id;
		packet.c = locVars[0];
		packet.d = locVars[1];
		packet.e = locVars[2];
		packet.i = DisguiseCraft.degreeToByte(loc.getYaw());
		packet.j = DisguiseCraft.degreeToByte(loc.getPitch());
		if (d.type == DisguiseType.EnderDragon) { // Ender Dragon fix
			packet.i = (byte) (packet.i - 128);
		} else if (d.type == DisguiseType.Chicken) { // Chicken fix
			packet.j = (byte) (packet.j * -1);
		}
		packet.k = packet.i;
		try {
			Field metadataField = packet.getClass().getDeclaredField("s");
			metadataField.setAccessible(true);
			metadataField.set(packet, d.metadata);
		} catch (Exception e) {
			DisguiseCraft.logger.log(Level.SEVERE, "Unable to set the metadata for a " + d.type.name() +  " disguise!", e);
		}
		return packet;
	}
	
	public Packet20NamedEntitySpawn getPlayerSpawnPacket(Location loc, short item) {
		int[] locVars = getLocationVariables(loc);
		Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn();
        packet.a = d.entityID;
        packet.b = d.data.getFirst();
        packet.c = locVars[0];
        packet.d = locVars[1];
        packet.e = locVars[2];
        packet.f = DisguiseCraft.degreeToByte(loc.getYaw());
        packet.g = DisguiseCraft.degreeToByte(loc.getPitch());
        packet.h = item;
        try {
			Field metadataField = packet.getClass().getDeclaredField("i");
			metadataField.setAccessible(true);
			metadataField.set(packet, d.metadata);
		} catch (Exception e) {
			DisguiseCraft.logger.log(Level.SEVERE, "Unable to set the metadata for a player disguise!", e);
		}
        return packet;
	}
	
	public Packet23VehicleSpawn getObjectSpawnPacket(Location loc) {
		Packet23VehicleSpawn packet = new Packet23VehicleSpawn();
		
		packet.i = 1;
		
		// Block specific
    	if (d.type.isBlock()) {
    		loc.setY(loc.getY() + 0.5);
    		
    		Byte blockID = d.getBlockID();
    		if (blockID != null) {
    			packet.i = (int) blockID;
    			
    			Byte blockData = d.getBlockData();
    			if (blockData != null) {
    				packet.i = packet.i | (((int) blockData) << 0xC);
    			}
    		}
    	}
		
		int[] locVars = getLocationVariables(loc);
		packet.a = d.entityID;
		packet.b = locVars[0];
		packet.c = locVars[1];
		packet.d = locVars[2];
		packet.h = d.type.id;
		packet.e = packet.f = packet.g = 0;
		
		return packet;
	}
	
	public Packet29DestroyEntity getEntityDestroyPacket() {
		return new Packet29DestroyEntity(d.entityID);
	}
	
	public Packet5EntityEquipment getEquipmentChangePacket(short slot, ItemStack item) {
		Packet5EntityEquipment packet;
		if (item == null) {
			packet = new Packet5EntityEquipment();
			packet.a = d.entityID;
			packet.b = slot;
			
			try{
				Field itemField = packet.getClass().getDeclaredField("c");
				itemField.setAccessible(true);
				itemField.set(packet, null);
			} catch (Exception e) {
				DisguiseCraft.logger.log(Level.SEVERE, "Unable to set the item type for a player disguise!", e);
			}
		} else {
			packet = new Packet5EntityEquipment(d.entityID, slot, ((CraftItemStack) item).getHandle());
		}
		return packet;
	}
	
	public Packet32EntityLook getEntityLookPacket(Location loc) {
		Packet32EntityLook packet = new Packet32EntityLook();
		packet.a = d.entityID;
		packet.b = 0;
		packet.c = 0;
		packet.d = 0;
		packet.e = DisguiseCraft.degreeToByte(loc.getYaw());
		packet.f = DisguiseCraft.degreeToByte(loc.getPitch());
		
		
		if (d.type == DisguiseType.EnderDragon) { // EnderDragon specific
			packet.e = (byte) (packet.e - 128);
		} else if (d.type == DisguiseType.Chicken) { // Chicken fix
			packet.f = (byte) (packet.f * -1);
		} else if (d.type.isVehicle()) { // Vehicle fix
			packet.e = (byte) (packet.e - 64);
		}
		return packet;
	}
	
	public Packet33RelEntityMoveLook getEntityMoveLookPacket(Location look) {
		Packet33RelEntityMoveLook packet = new Packet33RelEntityMoveLook();
		packet.a = d.entityID;
		MovementValues movement = getMovement(look);
		encposX += movement.x;
		encposY += movement.y;
		encposZ += movement.z;
		packet.b = (byte) movement.x;
		packet.c = (byte) movement.y;
		packet.d = (byte) movement.z;
		packet.e = DisguiseCraft.degreeToByte(look.getYaw());
		packet.f = DisguiseCraft.degreeToByte(look.getPitch());
		
		if (d.type == DisguiseType.EnderDragon) { // EnderDragon specific
			packet.e = (byte) (packet.e - 128);
		} else if (d.type == DisguiseType.Chicken) { // Chicken fix
			packet.f = (byte) (packet.f * -1);
		} else if (d.type.isVehicle()) { // Vehicle fix
			packet.e = (byte) (packet.e - 64);
		}
		return packet;
	}
	
	public Packet34EntityTeleport getEntityTeleportPacket(Location loc) {
		Packet34EntityTeleport packet = new Packet34EntityTeleport();
		packet.a = d.entityID;
		int x = (int) MathHelper.floor(32D * loc.getX());
		int y = (int) MathHelper.floor(32D * loc.getY());
		int z = (int) MathHelper.floor(32D * loc.getZ());
		packet.b = x;
		packet.c = y;
		packet.d = z;
		encposX = x;
		encposY = y;
		encposZ = z;
		packet.e = DisguiseCraft.degreeToByte(loc.getYaw());
		packet.f = DisguiseCraft.degreeToByte(loc.getPitch());
		
		if (d.type == DisguiseType.EnderDragon) { // EnderDragon specific
			packet.e = (byte) (packet.e - 128);
		} else if (d.type == DisguiseType.Chicken) { // Chicken fix
			packet.f = (byte) (packet.f * -1);
		} else if (d.type.isVehicle()) { // Vehicle fix
			packet.e = (byte) (packet.e - 64);
		}
		return packet;
	}
	
	public Packet40EntityMetadata getEntityMetadataPacket() {
		return new Packet40EntityMetadata(d.entityID, d.metadata, true); // 1.4.2 update: true-same method as 1.3.2
	}
	
	public Packet201PlayerInfo getPlayerInfoPacket(Player player, boolean show) {
		Packet201PlayerInfo packet = null;
		if (d.type.isPlayer()) {
			int ping;
			if (show) {
				ping = ((CraftPlayer) player).getHandle().ping;
			} else {
				ping = 9999;
			}
			packet = new Packet201PlayerInfo(d.data.getFirst(), show, ping);
		}
		return packet;
	}
	
	public MovementValues getMovement(Location to) {
		int x = MathHelper.floor(to.getX() *32D);
		int y = MathHelper.floor(to.getY() *32D);
		int z = MathHelper.floor(to.getZ() *32D);
		int diffx = x - encposX;
		int diffy = y - encposY;
		int diffz = z - encposZ;
		return new MovementValues(diffx, diffy, diffz, DisguiseCraft.degreeToByte(to.getYaw()), DisguiseCraft.degreeToByte(to.getPitch()));
	}
	
	public Packet35EntityHeadRotation getHeadRotatePacket(Location loc) {
		return new Packet35EntityHeadRotation(d.entityID, DisguiseCraft.degreeToByte(loc.getYaw()));
	}
	
	public Packet18ArmAnimation getAnimationPacket(int animation) {
		// 1 - Swing arm
		// 2 Damage animation
		// 5 Eat food
		Packet18ArmAnimation packet = new Packet18ArmAnimation();
		packet.a = d.entityID;
		packet.b = (byte) animation;
		return packet;
	}
	
	public Packet38EntityStatus getStatusPacket(int status) {
		// 2 - entity hurt
		// 3 - entity dead
		// 6 - wolf taming
		// 7 - wolf tamed
		// 8 - wolf shaking water
		// 10 - sheep eating grass
		return new Packet38EntityStatus(d.entityID, (byte) status);
	}

	public Packet22Collect getPickupPacket(int item) {
		return new Packet22Collect(item, d.entityID);
	}
	
	public Packet28EntityVelocity getVelocityPacket(int x, int y, int z) {
		Packet28EntityVelocity packet = new Packet28EntityVelocity();
		packet.a = d.entityID;
		packet.b = x;
		packet.c = y;
		packet.d = z;
		return packet;
	}
}
