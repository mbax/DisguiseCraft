package pgDev.bukkit.DisguiseCraft;

import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet24MobSpawn;

import org.bukkit.Location;

/**
 * This is the class for a disguise no longer
 * being used by a player, but still being displayed
 * to other players.
 * @author PG Dev Team (Devil Boy)
 */
public class DroppedDisguise extends Disguise {
	/**
	 * The name of the player who created this disguise.
	 */
	public String owner;
	/**
	 * The location of this disguise.
	 */
	public Location location;
	
	/**
	 * Constructs a new DroppedDisguise from a Disguise object
	 * @param disguise The original disguise object
	 * @param owner The name of the owner (currently arbitrary)
	 * @param location The location of this disguise
	 */
	public DroppedDisguise(Disguise disguise, String owner, Location location) {
		super(disguise.entityID, disguise.data, disguise.mob);
		this.owner = owner;
		this.location = location;
	}
	
	// Packet Creation Methods
	public Packet24MobSpawn getMobSpawnPacket() {
		return super.getMobSpawnPacket(location);
	}
	
	public Packet20NamedEntitySpawn getPlayerSpawnPacket(short item) {
		return super.getPlayerSpawnPacket(location, item);
	}
}
