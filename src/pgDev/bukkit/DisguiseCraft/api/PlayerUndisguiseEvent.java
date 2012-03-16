package pgDev.bukkit.DisguiseCraft.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This cancellable event is thrown when a player is to be
 * undisguised either by command or by another plugin. It is
 * not called on player quits.
 * @author PG Dev Team (Devil Boy)
 */
public class PlayerUndisguiseEvent extends Event implements Cancellable {
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
    
    public PlayerUndisguiseEvent(Player player) {
    	this.player = player;
    }
    
    /**
	 * Get the player
	 * @return The player who is to be undisguised.
	 */
    public Player getPlayer() {
    	return player;
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
