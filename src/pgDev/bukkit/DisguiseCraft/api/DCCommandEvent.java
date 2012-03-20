package pgDev.bukkit.DisguiseCraft.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DCCommandEvent extends Event implements Cancellable {
	// Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    // Actual event things
    CommandSender sender;
    Player player;
    String label;
    String[] args;
    
    public DCCommandEvent(CommandSender sender, Player player, String label, String[] args) {
    	this.sender = sender;
    	this.player = player;
    	this.label = label;
    	this.args = args;
    }
    
    /**
	 * Get the sender
	 * @return The sender of the command.
	 */
    public CommandSender getSender() {
    	return sender;
    }
    
    /**
	 * Get the player
	 * @return The player who is involved in the command.
	 */
    public Player getPlayer() {
    	return player;
    }
    
    /**
	 * Get the command label
	 * @return The command label (e.g. "/d")
	 */
    public String getLabel() {
    	return label;
    }
    
    /**
	 * Get the command arguments
	 * @return The command arguments (e.g. aggressive)
	 */
    public String[] getArgs() {
    	return args;
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
