package pgDev.bukkit.DisguiseCraft;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.MathHelper;
import net.minecraft.server.Packet201PlayerInfo;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet24MobSpawn;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet32EntityLook;
import net.minecraft.server.Packet33RelEntityMoveLook;
import net.minecraft.server.Packet34EntityTeleport;
import net.minecraft.server.Packet40EntityMetadata;
import net.minecraft.server.Packet5EntityEquipment;

public class Disguise {
	// MobType Enum
	public enum MobType {
		Blaze(61),
		CaveSpider(59),
		Chicken(93),
		Cow(92),
		Creeper(50),
		EnderDragon(63),
		Enderman(58),
		Ghast(56),
		Giant(53),
		MagmaCube(62),
		MushroomCow(96),
		Pig(90),
		PigZombie(57),
		Sheep(91),
		Silverfish(60),
		Skeleton(51),
		Slime(55),
		Snowman(97),
		Spider(52),
		Squid(94),
		Villager(120),
		Wolf(95),
		Zombie(54);
		
		public final byte id;
		MobType(int i) {
			id = (byte) i;
		}
		
		public boolean isSubclass(Class<?> cls) {
			try {
				return cls.isAssignableFrom(Class.forName("org.bukkit.entity." + name()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		public static MobType fromString(String text) {
			for (MobType m : MobType.values()) {
				if (text.equalsIgnoreCase(m.name())) {
					return m;
				}
			}
			return null;
		}
		
		public static String[] subTypes = {"baby", "charged"};
	}
	
	// Individual disguise stuff
	public int entityID;
	public String data; // $ means invisible player
	public MobType mob; // null if player
	DataWatcher metadata = new DataWatcher();
	private double lastVectorX;
	private double lastVectorY;
	private double lastVectorZ;
	
	private double lastposX;
	private double lastposY;
	private double lastposZ;
	
	private int encposX;
	private int encposY;
	private int encposZ;
	
	private boolean firstpos = true;
	
	public Disguise(int entityID, String data, MobType mob) {
		this.entityID = entityID;
		this.data = data;
		this.mob = mob;
		
		initializeData();
		handleData();
	}
	
	public Disguise setEntityID(int entityID) {
		this.entityID = entityID;
		return this;
	}
	
	public Disguise setData(String data) {
		this.data = data;
		handleData();
		return this;
	}
	
	public Disguise setMob(MobType mob) {
		this.mob = mob;
		return this;
	}
	
	public void initializeData() {
		metadata.a(12, 0);
	}
	
	public void handleData() {
		if (mob != null) {
			if (data != null) {
				if (data.contains("baby")) {
					metadata.watch(12, -23999);
				} else {
					metadata.watch(12, 0);
				}
			} else {
				metadata.watch(12, 0);
			}
		}
	}
	
	public Disguise clone() {
		return new Disguise(entityID, data, mob);
	}
	
	public boolean isPlayer() {
		return (mob == null && !data.equals("$"));
	}
	
	// Packet creation methods
	public Packet24MobSpawn getMobSpawnPacket(Location loc) {
		if (mob != null) {
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
			packet.a = entityID;
			packet.b = mob.id;
			packet.c = (int) x;
			packet.d = (int) y;
			packet.e = (int) z;
			packet.f = DisguiseCraft.degreeToByte(loc.getYaw());
			packet.g = DisguiseCraft.degreeToByte(loc.getPitch());
			try {
				Field metadataField = packet.getClass().getDeclaredField("h");
				metadataField.setAccessible(true);
				metadataField.set(packet, metadata);
			} catch (Exception e) {
				System.out.println("DisguiseCraft was unable to set the metadata for a " + mob.name() +  " disguise!");
			}
			
			// Ender Dragon fix
			if (mob == MobType.EnderDragon) {
				packet.f = (byte) (packet.f - 128);
			}
			// Chicken fix
			if (mob == MobType.Chicken) {
				packet.g = (byte) (packet.g * -1);
			}
			return packet;
		} else {
			return null;
		}
	}
	
	public Packet20NamedEntitySpawn getPlayerSpawnPacket(Location loc, short item) {
		if (mob == null && !data.equals("$")) {
			Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn();
	        packet.a = entityID;
	        packet.b = data;
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
	        return packet;
		} else {
			return null;
		}
	}
	
	public Packet29DestroyEntity getEntityDestroyPacket() {
		return new Packet29DestroyEntity(entityID);
	}
	
	public Packet5EntityEquipment getEquipmentChangePacket(short slot, ItemStack item) {
		if (mob == null && !data.equals("$")) {
			return new Packet5EntityEquipment(entityID, slot, ((CraftItemStack) item).getHandle());
		} else {
			return null;
		}
	}
	
	public Packet32EntityLook getEntityLookPacket(Location loc) {
		Packet32EntityLook packet = new Packet32EntityLook();
		packet.a = entityID;
		packet.b = 0;
		packet.c = 0;
		packet.d = 0;
		packet.e = DisguiseCraft.degreeToByte(loc.getYaw());
		packet.f = DisguiseCraft.degreeToByte(loc.getPitch());
		
		// EnderDragon specific
		if (mob == MobType.EnderDragon) {
			packet.e = (byte) (packet.e - 128);
		}
		// Chicken fix
		if (mob == MobType.Chicken) {
			packet.f = (byte) (packet.f * -1);
		}
		return packet;
	}
	
	public Packet33RelEntityMoveLook getEntityMoveLookPacket(Location look) {
		Packet33RelEntityMoveLook packet = new Packet33RelEntityMoveLook();
		packet.a = entityID;
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
		if (mob == MobType.EnderDragon) {
			packet.e = (byte) (packet.e - 128);
		}
		// Chicken fix
		if (mob == MobType.Chicken) {
			packet.f = (byte) (packet.f * -1);
		}
		return packet;
	}
	
	public Packet34EntityTeleport getEntityTeleportPacket(Location loc) {
		Packet34EntityTeleport packet = new Packet34EntityTeleport();
		packet.a = entityID;
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
		if (mob == MobType.EnderDragon) {
			packet.e = (byte) (packet.e - 128);
		}
		// Chicken fix
		if (mob == MobType.Chicken) {
			packet.f = (byte) (packet.f * -1);
		}
		return packet;
	}
	
	public Packet40EntityMetadata getEntityMetadataPacket() {
		Packet40EntityMetadata packet = new Packet40EntityMetadata();
		packet.a = entityID;
		try {
			Field metadataField = packet.getClass().getDeclaredField("b");
			metadataField.setAccessible(true);
			metadataField.set(packet, metadata);
		} catch (Exception e) {
			System.out.println("DisguiseCraft was unable to set the metadata for a " + mob.name() +  " disguise!");
		}
		return packet;
	}
	
	public Packet201PlayerInfo getPlayerInfoPacket(Player player, boolean show) {
		Packet201PlayerInfo packet = null;
		if (isPlayer()) {
			int ping;
			if (show) {
				ping = ((CraftPlayer) player).getHandle().ping;
			} else {
				ping = 9999;
			}
			packet = new Packet201PlayerInfo(data, show, ping);
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
}
