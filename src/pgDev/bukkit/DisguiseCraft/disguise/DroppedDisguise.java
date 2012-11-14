package pgDev.bukkit.DisguiseCraft.disguise;

import java.util.LinkedList;

import net.minecraft.server.Packet;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
		super(disguise.entityID, disguise.data, disguise.type);
		this.owner = owner;
		this.location = location;
	}
	
	public LinkedList<Packet> getSpawnPackets(Player player) {
		LinkedList<Packet> packets = new LinkedList<Packet>();
		packets.add(packetGenerator.getSpawnPacket(player));
		packets.add(packetGenerator.getEquipmentChangePacket((short) 0, player.getItemInHand()));
		packets.addAll(packetGenerator.getArmorPackets(player));
		return packets;
	}
}
