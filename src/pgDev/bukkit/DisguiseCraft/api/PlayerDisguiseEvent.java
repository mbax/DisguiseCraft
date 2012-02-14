package pgDev.bukkit.DisguiseCraft.api;

import org.bukkit.entity.Player;
import org.bukkit.event.*;

import pgDev.bukkit.DisguiseCraft.Disguise;

@SuppressWarnings("serial")
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
    
    public Player getPlayer() {
    	return player;
    }
    
    public Disguise getDisguise() {
    	return disguise;
    }
    
    // Cancel methods
    boolean cancelled = false;
	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean toggle) {
		cancelled = toggle;
	}
}
