package pgDev.bukkit.DisguiseCraft;

import java.io.File;
import java.util.HashMap;

import net.minecraft.server.Packet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class DisguiseCraft extends JavaPlugin {
	// File Locations
    static String pluginMainDir = "./plugins/DisguiseCraft";
    static String pluginConfigLocation = pluginMainDir + "/DisguiseCraft.cfg";
	
    // Permissions support
    static PermissionHandler Permissions;
    
    // Listener
    DCMainListener mainListener = new DCMainListener(this);
    
    // Disguise database
    HashMap<String, Disguise> disguiseDB = new HashMap<String, Disguise>();
    
    // Custom display nick saving
    HashMap<String, String> customNick = new HashMap<String, String>();
    
	public void onEnable() {
		// Check for the plugin directory (create if it does not exist)
    	File pluginDir = new File(pluginMainDir);
		if(!pluginDir.exists()) {
			boolean dirCreation = pluginDir.mkdirs();
			if (dirCreation) {
				System.out.println("New DisguiseCraft directory created!");
			}
		}
		
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(mainListener, this);
		
		// Toss over the command events
		DCCommandListener commandListener = new DCCommandListener(this);
		String[] commandList = {"disguise", "d", "undisguise", "u"};
        for (String command : commandList) {
        	try {
        		this.getCommand(command).setExecutor(commandListener);
        	} catch (NullPointerException e) {
        		System.out.println("Another plugin is using the /" + command + " command. You will need to use one of DisguiseCraft's alternate commands.");
        	}
        }
		
		// Get permissions in the game!
        setupPermissions();
        
        // Heyo!
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}
	
	public void onDisable() {
		System.out.println("DisguiseCraft disabled!");
	}
	
	// Permissions Methods
    private void setupPermissions() {
        Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");

        if (Permissions == null) {
            if (permissions != null) {
                Permissions = ((Permissions)permissions).getHandler();
            } else {
            }
        }
    }
    
    public static boolean hasPermissions(Player player, String node) {
        if (Permissions != null) {
        	return Permissions.has(player, node);
        } else {
            return player.hasPermission(node);
        }
    }
    
    // Obtaining the API
    public DisguiseCraftAPI api = new DisguiseCraftAPI(this);
    public static DisguiseCraftAPI getAPI() {
    	return ((DisguiseCraft) Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft")).api;
    }
    
    // Important Disguise Methods
    protected int nextID = Integer.MIN_VALUE;
    public int getNextAvailableID() {
    	return nextID++;
    }
    
    public void disguisePlayer(Player player, Disguise disguise) {
    	if (disguise.isPlayer()) {
    		if (!player.getName().equals(player.getDisplayName())) {
        		customNick.put(player.getName(), player.getDisplayName());
        	}
    		player.setDisplayName(disguise.data);
    	}
    	disguiseDB.put(player.getName(), disguise);
    	sendDisguise(player, null);
    }
    
    public void changeDisguise(Player player, Disguise disguise) {
    	Disguise original = disguiseDB.get(player.getName());
    	if (original.isPlayer()) {
    		sendPacketToWorld(player.getWorld(), original.getEntityDestroyPacket(), original.getPlayerInfoPacket(player, false));
    	} else {
    		sendPacketToWorld(player.getWorld(), original.getEntityDestroyPacket());
    	}
    	if (customNick.containsKey(player.getName())) {
    		player.setDisplayName(customNick.get(player.getName()));
    		customNick.remove(player.getName());
    	} else {
    		player.setDisplayName(player.getName());
    	}
    	disguisePlayer(player, disguise);
    }
    
    public void unDisguisePlayer(Player player) {
    	String name = player.getName();
    	if (disguiseDB.containsKey(name)) {
    		Disguise disguise = disguiseDB.get(name);
    		if (disguise.isPlayer()) {
    	    	if (customNick.containsKey(name)) {
    	    		player.setDisplayName(customNick.get(name));
    	    		customNick.remove(name);
    	    	} else {
    	    		player.setDisplayName(name);
    	    	}
        	}
    		sendUnDisguise(player, null);
    		disguiseDB.remove(name);
    	}
    }
    
    public static byte degreeToByte(float degree) {
    	return (byte) ((int) degree * 256.0F / 360.0F);
    }
    
    public void sendDisguise(Player disguised, Player observer) {
    	if (disguiseDB.containsKey(disguised.getName())) {
    		Disguise disguise = disguiseDB.get(disguised.getName());
    		//((CraftPlayer) observer).hidePlayer(disguised);
    		if (disguise.mob == null) { // Non-mob disguise
    			if (disguise.data.equals("$")) { // Invisible
    				if (observer == null) {
    					disguiseToWorld(disguised.getWorld(), disguised, (Packet[]) null);
    				} else {
    					((CraftPlayer) observer).hidePlayer(disguised);
    				}
    			} else { // Player disguise
    				Packet packet = disguise.getPlayerSpawnPacket(disguised.getLocation(), (short) disguised.getItemInHand().getTypeId());
    				Packet packet2 = disguise.getPlayerInfoPacket(disguised, true);
    				if (observer == null) {
    					if (packet2 == null) {
    						disguiseToWorld(disguised.getWorld(), disguised, packet);
    					} else {
    						disguiseToWorld(disguised.getWorld(), disguised, packet, packet2);
    					}
    				} else {
    					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    					if (packet2 != null) {
    						((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
    					}
    				}
    			}
    		} else { // Mob Disguise
    			Packet packet = disguise.getMobSpawnPacket(disguised.getLocation());
    			if (observer == null) {
    				disguiseToWorld(disguised.getWorld(), disguised, packet);
    			} else {
    				((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    			}
    		}
    	}
    }
    
    public void sendUnDisguise(Player disguised, Player observer) {
    	if (disguiseDB.containsKey(disguised.getName())) {
    		Disguise disguise = disguiseDB.get(disguised.getName());
    		Packet packet = disguise.getEntityDestroyPacket();
    		Packet packet2 = disguise.getPlayerInfoPacket(disguised, false);
    		if (observer == null) {
				if (packet2 == null) {
					undisguiseToWorld(disguised.getWorld(), disguised, packet);
				} else {
					undisguiseToWorld(disguised.getWorld(), disguised, packet, packet2);
				}
			} else {
				if (packet2 != null) {
					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
				}
				((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
				((CraftPlayer) observer).showPlayer(disguised);
			}
    	}
    }
    
    public void sendMovement(Player disguised, Player observer, Location from, Location to) {
    	if (disguiseDB.containsKey(disguised.getName())) {
    		Disguise disguise = disguiseDB.get(disguised.getName());
    		Vector difference = to.subtract(from).toVector();
    		if (difference.length() < 4) { // Relative movement
    			if (difference.length() == 0) { // Just looked around
    				Packet packet = disguise.getEntityLookPacket(to);
    				if (observer == null) {
    					sendPacketToWorld(disguised.getWorld(), packet);
    				} else {
    					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    				}
    			} else { // Moved legs
    				Packet packet = disguise.getEntityMoveLookPacket(difference, to);
    				if (observer == null) {
    					sendPacketToWorld(disguised.getWorld(), packet);
    				} else {
    					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    				}
    			}
    		} else { // That's like a teleport right there!
    			Packet packet = disguise.getEntityTeleportPacket(to);
    			if (observer == null) {
					sendPacketToWorld(disguised.getWorld(), packet);
				} else {
					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
				}
    		}
    	}
    }
    
    public void sendPacketToWorld(World world, Packet... packet) {
    	for (Player observer : world.getPlayers()) {
    		for (Packet p : packet) {
    			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
    		}
    	}
    }
    
    public void disguiseToWorld(World world, Player player, Packet... packet) {
    	for (Player observer : world.getPlayers()) {
	    	if (observer != player) {
	    		((CraftPlayer) observer).hidePlayer(player);
	    		for (Packet p : packet) {
	    			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
	    		}
    		}
    	}
    }
    
    public void undisguiseToWorld(World world, Player player, Packet... packet) {
    	for (Player observer : world.getPlayers()) {
    		if (observer != player) {
	    		for (Packet p : packet) {
	    			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
	    		}
				((CraftPlayer) observer).showPlayer(player);
    		}
    	}
    }
}
