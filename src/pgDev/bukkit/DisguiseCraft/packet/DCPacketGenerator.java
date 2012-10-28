package pgDev.bukkit.DisguiseCraft.packet;

import java.lang.reflect.Field;
import java.util.logging.Level;

import net.minecraft.server.MathHelper;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.Packet201PlayerInfo;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet24MobSpawn;
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

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.MovementValues;
import pgDev.bukkit.DisguiseCraft.Disguise.MobType;

public class DCPacketGenerator {
	final Disguise d;
	
	protected int encposX;
	protected int encposY;
	protected int encposZ;
	protected boolean firstpos = true;
	
	public DCPacketGenerator(final Disguise disguise) {
		d = disguise;
	}
	
	// Packet creation methods
	public Packet24MobSpawn getMobSpawnPacket(Location loc) {
		if (d.mob != null) {
			int x = MathHelper.floor(loc.getX() *32D);
			int y = MathHelper.floor(loc.getY() *32D);
			int z = MathHelper.floor(loc.getZ() *32D);
			if(firstpos) {
				encposX = x;
				encposY = y;
				encposZ = z;
				firstpos = false;
			}
			Packet24MobSpawn packet = new Packet24MobSpawn();
			packet.a = d.entityID;
			packet.b = d.mob.id;
			packet.c = (int) x;
			packet.d = (int) y;
			packet.e = (int) z;
			packet.i = DisguiseCraft.degreeToByte(loc.getYaw());
			packet.j = DisguiseCraft.degreeToByte(loc.getPitch());
			if (d.mob == MobType.EnderDragon) { // Ender Dragon fix
				packet.i = (byte) (packet.i - 128);
			}
			if (d.mob == MobType.Chicken) { // Chicken fix
				packet.j = (byte) (packet.j * -1);
			}
			packet.k = packet.i;
			try {
				Field metadataField = packet.getClass().getDeclaredField("s");
				metadataField.setAccessible(true);
				metadataField.set(packet, d.metadata);
			} catch (Exception e) {
				DisguiseCraft.logger.log(Level.SEVERE, "Unable to set the metadata for a " + d.mob.name() +  " disguise!", e);
			}
			return packet;
		} else {
			return null;
		}
	}
	
	public Packet20NamedEntitySpawn getPlayerSpawnPacket(Location loc, short item) {
		if (d.mob == null && d.data != null) {
			Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn();
	        packet.a = d.entityID;
	        packet.b = d.data.getFirst();
	        int x = MathHelper.floor(loc.getX() *32D);
			int y = MathHelper.floor(loc.getY() *32D);
			int z = MathHelper.floor(loc.getZ() *32D);
			if(firstpos) {
				encposX = x;
				encposY = y;
				encposZ = z;
				firstpos = false;
			}
	        packet.c = (int) x;
	        packet.d = (int) y;
	        packet.e = (int) z;
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
		} else {
			return null;
		}
	}
	
	public Packet29DestroyEntity getEntityDestroyPacket() {
		return new Packet29DestroyEntity(d.entityID);
	}
	
	public Packet5EntityEquipment getEquipmentChangePacket(short slot, ItemStack item) {
		if (d.isPlayer()) {
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
		} else {
			return null;
		}
	}
	
	public Packet32EntityLook getEntityLookPacket(Location loc) {
		Packet32EntityLook packet = new Packet32EntityLook();
		packet.a = d.entityID;
		packet.b = 0;
		packet.c = 0;
		packet.d = 0;
		packet.e = DisguiseCraft.degreeToByte(loc.getYaw());
		packet.f = DisguiseCraft.degreeToByte(loc.getPitch());
		
		// EnderDragon specific
		if (d.mob == MobType.EnderDragon) {
			packet.e = (byte) (packet.e - 128);
		}
		// Chicken fix
		if (d.mob == MobType.Chicken) {
			packet.f = (byte) (packet.f * -1);
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
		
		// EnderDragon specific
		if (d.mob == MobType.EnderDragon) {
			packet.e = (byte) (packet.e - 128);
		}
		// Chicken fix
		if (d.mob == MobType.Chicken) {
			packet.f = (byte) (packet.f * -1);
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
		
		// EnderDragon specific
		if (d.mob == MobType.EnderDragon) {
			packet.e = (byte) (packet.e - 128);
		}
		// Chicken fix
		if (d.mob == MobType.Chicken) {
			packet.f = (byte) (packet.f * -1);
		}
		return packet;
	}
	
	public Packet40EntityMetadata getEntityMetadataPacket() {
		return new Packet40EntityMetadata(d.entityID, d.metadata, true); // 1.4.2 update: true-same method as 1.3.2
	}
	
	public Packet201PlayerInfo getPlayerInfoPacket(Player player, boolean show) {
		Packet201PlayerInfo packet = null;
		if (d.isPlayer()) {
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
}
