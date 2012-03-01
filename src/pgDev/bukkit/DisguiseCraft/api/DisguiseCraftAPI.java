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
	 * @return true for success, false if canceled
	 */
	public boolean disguisePlayer(Player player, Disguise disguise) {
		PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, disguise);
		plugin.getServer().getPluginManager().callEvent(ev);
		if (ev.isCancelled()) {
			return false;
		} else {
			plugin.disguisePlayer(player, disguise);
			return true;
		}
	}
	
	/**
	 * Change the player's disguise (For use when the player is already disguised)
	 * @param player The player whose disguise will be changed
	 * @param newDisguise The new disguise
	 * @return true for success, false if canceled
	 */
	public boolean changePlayerDisguise(Player player, Disguise newDisguise) {
		// Pass the event
		PlayerDisguiseEvent ev = new PlayerDisguiseEvent(player, newDisguise);
		plugin.getServer().getPluginManager().callEvent(ev);
		if (ev.isCancelled())  {
			return false;
		} else {
			plugin.changeDisguise(player, newDisguise);
			return true;
		}
	}
	
	/**
	 * Undisguise a player
	 * @param player The player to undisguise
	 * @return true for success, false if canceled
	 */
	public boolean undisguisePlayer(Player player) {
		// Pass the event
		PlayerUndisguiseEvent ev = new PlayerUndisguiseEvent(player);
		plugin.getServer().getPluginManager().callEvent(ev);
		if (ev.isCancelled()) {
			return false;
		} else {
			plugin.unDisguisePlayer(player);
			return true;
		}
	}
}
