package pgDev.bukkit.DisguiseCraft.api;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DisguiseCraftAPI {
	final DisguiseCraft plugin;
	
	public DisguiseCraftAPI(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Check is a player is disguised
	 * @param player The player you wish to check
	 * @return true if disguised, false otherwise
	 */
	public boolean isDisguised(Player player) {
		return plugin.disguiseDB.containsKey(player.getName());
	}
	
	/**
	 * Get a player's disguise
	 * @param player The player who you want to get the disguise of
	 * @return The disguise object (null if not disguised)
	 */
	public Disguise getDisguise(Player player) {
		return plugin.disguiseDB.get(player.getName());
	}
	
	/**
	 * Obtain the next available entity ID
	 * @return The next free and unused entity ID
	 */
	public int newEntityID() {
		return plugin.getNextAvailableID();
	}
	
	/**
	 * Disguise a player
	 * @param player The player to be disguised
	 * @param disguise The disguise to be placed on the player
	 */
	public void disguisePlayer(Player player, Disguise disguise) {
		plugin.disguisePlayer(player, disguise);
	}
	
	/**
	 * Change the player's disguise (For use when the player is already disguised)
	 * @param player The player whose disguise will be changed
	 * @param newDisguise The new disguise
	 */
	public void changePlayerDisguise(Player player, Disguise newDisguise) {
		plugin.changeDisguise(player, newDisguise);
	}
	
	/**
	 * Undisguise a player
	 * @param player The player to undisguise
	 */
	public void undisguisePlayer(Player player) {
		plugin.unDisguisePlayer(player);
	}
}
