package pgDev.bukkit.DisguiseCraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Entity;
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

import pgDev.bukkit.DisguiseCraft.Disguise.MobType;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.listeners.DCCommandListener;
import pgDev.bukkit.DisguiseCraft.listeners.DCMainListener;
import pgDev.bukkit.DisguiseCraft.listeners.DCOptionalListener;
import pgDev.bukkit.DisguiseCraft.listeners.DCPacketListener;
import pgDev.bukkit.DisguiseCraft.listeners.movement.DCPlayerMoveListener;
import pgDev.bukkit.DisguiseCraft.listeners.movement.DCPlayerPositionUpdater;
import pgDev.bukkit.DisguiseCraft.stats.Metrics;
import pgDev.bukkit.DisguiseCraft.stats.Metrics.Graph;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * The DisguiseCraft plugin main class. With the exception of the
 * getAPI() function, methods in this class should not be used
 * by other plugins.
 * @author PG Dev Team (Devil Boy)
 */
public class DisguiseCraft extends JavaPlugin {
	
	// Plugin Version
	public String version;
	
	// File Locations
    static String pluginMainDir = "./plugins/DisguiseCraft";
    static String pluginConfigLocation = pluginMainDir + "/DisguiseCraft.cfg";
    
    // Bukkit Logger (Console Output)
    public static Logger logger;
	
    // Permissions support
    static PermissionHandler Permissions;
    
    // Protocol Hooks
    public static ProtocolManager protocolManager;
    
    // Listeners
    DCMainListener mainListener;
    DCPlayerMoveListener moveListener;
    DCPacketListener packetListener; // Not a real listener o.o
    DCOptionalListener optionalListener;
    
    // Disguise database
    public ConcurrentHashMap<String, Disguise> disguiseDB = new ConcurrentHashMap<String, Disguise>();
    public LinkedList<String> disguiseQuitters = new LinkedList<String>();
    public ConcurrentHashMap<Integer, Player> disguiseIDs = new ConcurrentHashMap<Integer, Player>();
    public ConcurrentHashMap<Integer, DroppedDisguise> droppedDisguises = new ConcurrentHashMap<Integer, DroppedDisguise>();
    public ConcurrentHashMap<Player, Integer> positionUpdaters = new ConcurrentHashMap<Player, Integer>();
    
    // Custom display nick saving
    public HashMap<String, String> customNick = new HashMap<String, String>();
    
    // Plugin Configuration
    static public DCConfig pluginSettings;
    
	public void onEnable() {
		// Obtain logger
		logger = getLogger();
		
		// Check for the plugin directory (create if it does not exist)
    	File pluginDir = new File(pluginMainDir);
		if(!pluginDir.exists()) {
			boolean dirCreation = pluginDir.mkdirs();
			if (dirCreation) {
				logger.log(Level.INFO, "New directory created!");
			}
		}
		
		// Load the Configuration
    	try {
        	Properties preSettings = new Properties();
        	if ((new File(pluginConfigLocation)).exists()) {
        		preSettings.load(new FileInputStream(new File(pluginConfigLocation)));
        		pluginSettings = new DCConfig(preSettings, this);
        		if (!pluginSettings.upToDate) {
        			pluginSettings.createConfig();
        			logger.log(Level.INFO, "Configuration updated!");
        		}
        	} else {
        		pluginSettings = new DCConfig(preSettings, this);
        		pluginSettings.createConfig();
        		logger.log(Level.INFO, "Configuration created!");
        	}
        } catch (Exception e) {
        	logger.log(Level.WARNING, "Could not load configuration!", e);
        }
		
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(mainListener  = new DCMainListener(this), this);
		if (!pluginSettings.movementUpdateThreading) {
			pm.registerEvents(moveListener = new DCPlayerMoveListener(this), this);
		}
		if (pluginSettings.optionalListeners) {
			pm.registerEvents(optionalListener = new DCOptionalListener(this), this);
		}
		
		// Toss over the command events
		DCCommandListener commandListener = new DCCommandListener(this);
		String[] commandList = {"disguise", "undisguise"};
        for (String command : commandList) {
        	try {
        		this.getCommand(command).setExecutor(commandListener);
        	} catch (NullPointerException e) {
        		logger.log(Level.INFO, "Another plugin is using the /" + command + " command. You will need to use one of the alternate commands.");
        	}
        }
		
		// Get permissions in the game!
        setupPermissions();
        
        // Set up the protocol hook!
        if (pluginSettings.disguisePVP) {
        	if (!setupProtocol()) {
        		logger.log(Level.WARNING, "You have \"disguisePVP\" enabled in the configuration, but do not have the ProtocolLib plugin installed! Players wearing disguises can not be attacked by melee!");
        	}
        }
        
        // Set up statistics!
        setupMetrics();
        
        // Set up compatibility
        if (pluginSettings.compatibility) {
        	MobType.missingMobs = new LinkedList<MobType>();
        	MobType.modelData = new HashMap<Byte, DataWatcher>();
        	String missings = "";
        	
        	try {
        		Field watcherField = net.minecraft.server.Entity.class.getDeclaredField("datawatcher");
        		watcherField.setAccessible(true);
				
				for (MobType m : MobType.values()) {
					String entPrefix = "net.minecraft.server.Entity";
					String mobClass = entPrefix + m.name();
					if (m == MobType.Giant) {
        				mobClass = mobClass + "Zombie";
        			}
					
	        		try {
	        			Entity ent = (Entity) Class.forName(mobClass).getConstructor(net.minecraft.server.World.class).newInstance((Object) null);
	        			MobType.modelData.put(m.id, (DataWatcher) watcherField.get(ent));
	        		} catch (Exception e) {
	        			MobType.missingMobs.add(m);
	        			if (missings.equals("")) {
	        				missings = m.name();
	        			} else {
	        				missings = missings + ", " + m.name();
	        			}
	        		}
	        	}
	        	if (!missings.equals("")) {
	        		logger.log(Level.WARNING, "The following mobs are not present in this MineCraft version: " + missings);
	        	}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not access datawatcher! Compatibility mode failed!");
			}
        }
        
        // Heyo!
        PluginDescriptionFile pdfFile = this.getDescription();
        version = pdfFile.getVersion();
        logger.log(Level.INFO, "Version " + version + " is enabled!");
	}
	
	public void onDisable() {
		logger.log(Level.INFO, "Disabled!");
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
    
    public boolean hasPermissions(Player player, String node) {
        if (Permissions != null) {
        	return Permissions.has(player, node);
        } else {
            return player.hasPermission(node);
        }
    }
    
    // Stats
    public void setupMetrics() {
    	try {
    		Metrics metrics = new Metrics(this);
    		
    		// Total Disguises Graph
    		Graph disguiseGraph = metrics.createGraph("Default");
    		disguiseGraph.addPlotter(new Metrics.Plotter("Total Disguises") {
    			@Override
    			public int getValue() {
    				return disguiseDB.size();
    			}
    		});
    		
    		Graph protocolGraph = metrics.createGraph("protocolGraph");
    		protocolGraph.addPlotter(new Metrics.Plotter("Using ProtocolLib") {
    			@Override
    			public int getValue() {
    				return 1;
    			}
    			
    			@Override
    			public String getColumnName() {
    				if (protocolManager == null) {
    					if (pluginSettings.disguisePVP) {
    						return "should be";
    					} else {
    						return "no";
    					}
    				} else {
    					return "yes";
    				}
    			}
    		});
    		
    		metrics.start();
    	} catch (IOException e) {
    		
    	}
    }
    
    // Protocol Library
    public boolean setupProtocol() {
    	Plugin protocolLib = this.getServer().getPluginManager().getPlugin("ProtocolLib");
    	
    	if (protocolLib == null) {
    		return false;
    	} else {
    		protocolManager = ProtocolLibrary.getProtocolManager();
    		packetListener = new DCPacketListener(this);
    		return true;
    	}
    }
    
    // Obtaining the API
    public DisguiseCraftAPI api = new DisguiseCraftAPI(this);
    /**
     * Get the DisguiseCraft API
     * @return The API (null if it was not found)
     */
    public static DisguiseCraftAPI getAPI() {
    	try {
    		return ((DisguiseCraft) Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft")).api;
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, "The DisguiseCraft API could not be obtained!");
    		return null;
    	}
    }
    
    // Important Disguise Methods
    protected int nextID = Integer.MIN_VALUE;
    public int getNextAvailableID() {
    	return nextID++;
    }
    
    public void disguisePlayer(Player player, Disguise disguise) {
    	if (disguise.isPlayer()) {
    		if (!customNick.containsKey(player.getName()) && !player.getName().equals(player.getDisplayName())) {
        		customNick.put(player.getName(), player.getDisplayName());
        	}
    		player.setDisplayName(disguise.data.getFirst());
    	}
    	disguiseDB.put(player.getName(), disguise);
    	disguiseIDs.put(disguise.entityID, player);
    	sendDisguise(player, null);
    	
    	// Start position updater
		setPositionUpdater(player, disguise);
    }
    
    public void changeDisguise(Player player, Disguise newDisguise) {
    	unDisguisePlayer(player);
    	disguisePlayer(player, newDisguise);
    }
    
    public void unDisguisePlayer(Player player) {
    	String name = player.getName();
    	if (disguiseDB.containsKey(name)) {
    		if (customNick.containsKey(name)) {
	    		player.setDisplayName(customNick.get(name));
	    		customNick.remove(name);
	    	} else {
	    		player.setDisplayName(name);
	    	}
    		sendUnDisguise(player, null);
    		disguiseIDs.remove(disguiseDB.get(player.getName()).entityID);
    		disguiseDB.remove(name);
    		
    		// Stop position updater
    		removePositionUpdater(player);
    	}
    }
    
    public void dropDisguise(Player player) {
    	String name = player.getName();
    	if (disguiseDB.containsKey(name)) {
    		// Database Handling
    		if (customNick.containsKey(name)) {
	    		player.setDisplayName(customNick.get(name));
	    		customNick.remove(name);
	    	} else {
	    		player.setDisplayName(name);
	    	}
    		
    		// Client Handling
    		DroppedDisguise disguise = new DroppedDisguise(disguiseDB.get(name), name, player.getLocation());
    		Packet packet = disguise.getPlayerInfoPacket(player, false);
    		if (packet == null) {
    			undisguiseToWorld(player.getWorld(), player);
    			((CraftPlayer) player).getHandle().netServerHandler.sendPacket(disguise.getMobSpawnPacket());
    		} else {
    			undisguiseToWorld(player.getWorld(), player, packet);
    			((CraftPlayer) player).getHandle().netServerHandler.sendPacket(disguise.getPlayerSpawnPacket((short) player.getItemInHand().getTypeId()));
    		}
    		((CraftPlayer) player).getHandle().netServerHandler.sendPacket(disguise.getHeadRotatePacket());
    		
    		// More Database Handling
    		disguiseIDs.remove(disguise.entityID);
    		disguiseDB.remove(name);
    		droppedDisguises.put(disguise.entityID, disguise);
    	}
    }
    
    public void halfUndisguiseAllToPlayer(Player observer) {
    	World world = observer.getWorld();
    	for (String name : disguiseDB.keySet()) {
    		Player disguised = getServer().getPlayer(name);
    		if (disguised != null) {
    			if (world == disguised.getWorld()) {
    				observer.showPlayer(disguised);
    			}
    		}
    	}
    }
    
    public static byte degreeToByte(float degree) {
    	return (byte) ((int) degree * 256.0F / 360.0F);
    }
    
    public void sendDisguise(Player disguised, Player observer) {
    	if (disguiseDB.containsKey(disguised.getName())) {
    		Disguise disguise = disguiseDB.get(disguised.getName());
    		if (disguise.mob == null) { // Non-mob disguise
    			if (disguise.data.equals("$")) { // Invisible
    				if (observer == null) {
    					disguiseToWorld(disguised.getWorld(), disguised, (Packet[]) null);
    				} else {
    					observer.hidePlayer(disguised);
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
    					if (!hasPermissions(observer, "disguisecraft.seer")) {
    						observer.hidePlayer(disguised);
    					}
    					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    					if (packet2 != null) {
    						((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
    					}
    				}
    			}
    		} else { // Mob Disguise
    			Packet packet = disguise.getMobSpawnPacket(disguised.getLocation());
    			Packet packet2 = null;
    			if (disguise.mob == MobType.Zombie || disguise.mob == MobType.PigZombie || disguise.mob == MobType.Skeleton) {
    				packet2 = disguise.getEquipmentChangePacket((short) 0, disguised.getItemInHand());
    			}
    			
    			if (observer == null) {
    				if (packet2 == null) {
						disguiseToWorld(disguised.getWorld(), disguised, packet);
					} else {
						disguiseToWorld(disguised.getWorld(), disguised, packet, packet2);
					}
    			} else {
    				if (!hasPermissions(observer, "disguisecraft.seer")) {
						observer.hidePlayer(disguised);
					}
    				((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    				if (packet2 != null) {
						((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
					}
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
				observer.showPlayer(disguised);
			}
    	}
    }
    
    public void sendMovement(Player disguised, Player observer, Vector vector, Location to) {
    	if (disguiseDB.containsKey(disguised.getName())) {
    		Disguise disguise = disguiseDB.get(disguised.getName());
    		MovementValues movement = disguise.getMovement(to);
    		
    		if (pluginSettings.bandwidthReduction) {
    			if (movement.x < -128 || movement.x > 128 || movement.y < -128 || movement.y > 128 || movement.z < -128 || movement.z > 128) { // That's like a teleport right there!
        			Packet packet = disguise.getEntityTeleportPacket(to);
        			if (observer == null) {
    					sendPacketToWorld(disguised.getWorld(), packet);
    				} else {
    					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    				}
        		} else { // Relative movement
        			if (movement.x == 0 && movement.y == 0 && movement.z == 0) { // Just looked around
        				//Client doesn't seem to want to register this
        				Packet packet = disguise.getEntityLookPacket(to);
        				Packet packet2 = disguise.getHeadRotatePacket(to);
        				if (observer == null) {
        					sendPacketToWorld(disguised.getWorld(), packet, packet2);
        				} else {
        					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
        					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
        				}
        			} else { // Moved legs
        				Packet packet = disguise.getEntityMoveLookPacket(to);
        				Packet packet2 = disguise.getHeadRotatePacket(to);
        				if (observer == null) {
        					sendPacketToWorld(disguised.getWorld(), packet, packet2);
        				} else {
        					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
        					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet2);
        				}
        			}
        		}
    		} else {
	    		Packet movePacket;
	    		Packet lookPacket = disguise.getHeadRotatePacket(to);
	    		if (movement.x == 0 && movement.y == 0 && movement.z == 0) { // Just looked around
					movePacket = disguise.getEntityLookPacket(to);
				} else {
					movePacket = disguise.getEntityTeleportPacket(to);
				}
	    		if (observer == null) {
					sendPacketToWorld(disguised.getWorld(), movePacket, lookPacket);
				} else {
					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(movePacket);
					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(lookPacket);
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
	    		if (!hasPermissions(observer, "disguisecraft.seer")) {
					observer.hidePlayer(player);
				}
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
				observer.showPlayer(player);
    		}
    	}
    }
    
    public void showWorldDisguises(Player observer) {
    	for (String disguisedName : disguiseDB.keySet()) {
			Player disguised = getServer().getPlayer(disguisedName);
			if (disguised != null && disguised != observer) {
				if (disguised.getWorld() == observer.getWorld()) {
					sendDisguise(disguised, observer);
				}
			}
		}
    }
    
    public void setPositionUpdater(Player player, Disguise disguise) {
    	if (DisguiseCraft.pluginSettings.movementUpdateThreading) {
    		positionUpdaters.put(player, getServer().getScheduler().scheduleSyncRepeatingTask(this, new DCPlayerPositionUpdater(this, player, disguise), 1, pluginSettings.movementUpdateFrequency));
    	}
    }
    
    public void removePositionUpdater(Player player) {
    	if (DisguiseCraft.pluginSettings.movementUpdateThreading) {
			if (positionUpdaters.containsKey(player)) {
				getServer().getScheduler().cancelTask(positionUpdaters.get(player));
			}
		}
    }
}
