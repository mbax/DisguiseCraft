package pgDev.bukkit.DisguiseCraft.api;

import org.bukkit.entity.Player;
import org.bukkit.event.*;

import pgDev.bukkit.DisguiseCraft.disguise.*;

/**
 * This cancellable event is called whenever a player is to be
 * disguised either by command or another plugin.
 * @author PG Dev Team (Devil Boy)
 */
public class PlayerDisguiseEvent extends Event implements Cancellable {
	// Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    // Actual event things
    Player player;
    Disguise disguise;
    
    public PlayerDisguiseEvent(Player player, Disguise disguise) {
    	this.player = player;
    	this.disguise = disguise;
    }
    
    /**
	 * Get the player
	 * @return The player who is to be disguised.
	 */
    public Player getPlayer() {
    	return player;
    }
    
    /**
	 * Get the disguise
	 * @return The disguise that the player is going to wear.
	 */
    public Disguise getDisguise() {
    	return disguise;
    }
    
    // Cancel methods
    boolean cancelled = false;
    
    /**
     * Whether or not the event is canceled
     * @return true if canceled, false otherwise
     */
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Set the cancel status of the event
	 */
	public void setCancelled(boolean toggle) {
		cancelled = toggle;
	}
}
