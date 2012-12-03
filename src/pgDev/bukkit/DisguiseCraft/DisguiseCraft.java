package pgDev.bukkit.DisguiseCraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet201PlayerInfo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.*;
import pgDev.bukkit.DisguiseCraft.listeners.DCCommandListener;
import pgDev.bukkit.DisguiseCraft.listeners.DCMainListener;
import pgDev.bukkit.DisguiseCraft.listeners.DCPacketListener;
import pgDev.bukkit.DisguiseCraft.listeners.attack.AttackProcessor;
import pgDev.bukkit.DisguiseCraft.listeners.movement.DCPlayerMoveListener;
import pgDev.bukkit.DisguiseCraft.listeners.movement.DCPlayerPositionUpdater;
import pgDev.bukkit.DisguiseCraft.packet.MovementValues;
import pgDev.bukkit.DisguiseCraft.stats.Metrics;
import pgDev.bukkit.DisguiseCraft.stats.Metrics.Graph;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

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
    
    // Protocol Hooks
    public static ProtocolManager protocolManager;
    
    // Listeners
    DCMainListener mainListener;
    DCPlayerMoveListener moveListener;
    DCPacketListener packetListener; // Not a real listener o.o
    
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
    
    // Attack processor thread
    public AttackProcessor attackProcessor = new AttackProcessor();
    
    @Override
    public void onLoad() {
    	// Obtain logger
    	logger = getLogger();
    }
    
    @Override
	public void onEnable() {
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
		for (Class<?> optional : pluginSettings.optionals.values()) {
			if (optional != null) {
				try {
					pm.registerEvents((Listener) optional.getConstructor(this.getClass()).newInstance(this), this);
				} catch (InstantiationException e) {
					logger.log(Level.WARNING, "Could not instantiate a " + optional.getSimpleName() + " class", e);
				} catch (IllegalAccessException e) {
					logger.log(Level.WARNING, "Could not access constructor for a " + optional.getSimpleName() + " class", e);
				} catch (IllegalArgumentException e) {
					logger.log(Level.WARNING, "Illegal arguments for " + optional.getSimpleName() + " class constructor", e);
				} catch (InvocationTargetException e) {
					logger.log(Level.WARNING, "Something bad happened when constructing a " + optional.getSimpleName() + " class", e);
				} catch (NoSuchMethodException e) {
					logger.log(Level.WARNING, "Could not find constructor for a " + optional.getSimpleName() + " class", e);
				} catch (SecurityException e) {
					logger.log(Level.WARNING, "Could not access/construct a " + optional.getSimpleName() + " class", e);
				}
			}
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
		
        
        // Set up the protocol hook!
        if (pluginSettings.disguisePVP) {
        	if (!setupProtocol()) {
        		logger.log(Level.WARNING, "You have \"disguisePVP\" enabled in the configuration, but do not have the ProtocolLib plugin installed! Players wearing disguises can not be attacked by melee!");
        	}
        }
        
        // Check for tab list no-hide
        if (pluginSettings.noTabHide) {
        	if (protocolManager == null) {
        		logger.log(Level.SEVERE, "You have \"noTabHide\" enabled in the configuration, but do not have the ProtocolLib plugin installed!");
        	} else {
        		packetListener.setupTabListListener();
        	}
        }
        
        // Set up statistics!
        setupMetrics();
        
        // Any mobs missing?
        String missings = "";
        for (DisguiseType mob : DisguiseType.missingDisguises) {
        	if (missings.equals("")) {
				missings = mob.name();
			} else {
				missings = missings + ", " + mob.name();
			}
        	
        }
        if (!missings.equals("")) {
    		logger.log(Level.WARNING, "The following mobs are not present in this MineCraft version: " + missings);
    	}
        
        // Start up attack processing thread
        getServer().getScheduler().scheduleSyncRepeatingTask(this, attackProcessor, 1, pluginSettings.attackInterval);
        
        // Heyo!
        PluginDescriptionFile pdfFile = this.getDescription();
        version = pdfFile.getVersion();
        logger.log(Level.INFO, "Version " + version + " is enabled!");
	}
	
    @Override
	public void onDisable() {
    	// Stop executor threads
    	mainListener.invalidInteractExecutor.shutdown();
    	
    	// Stop sync threads
    	getServer().getScheduler().cancelTasks(this);
    	
    	// Wipe dropped disguises
    	for (Integer i : droppedDisguises.keySet()) {
    		DroppedDisguise dd = droppedDisguises.get(i);
    		sendPacketToWorld(dd.location.getWorld(), dd.packetGenerator.getEntityDestroyPacket());
    	}
    	
    	// Remove disguises
    	for (Player disguised : disguiseIDs.values()) {
    		unDisguisePlayer(disguised);
    	}
    	
    	// Wipe config
    	pluginSettings = null;
    	
    	// Notify success
		logger.log(Level.INFO, "Disabled!");
	}
	
	// Permissions Methods
    public boolean hasPermissions(Player player, String node) {
            return player.hasPermission(node);
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
    	if (disguise.type.isPlayer()) {
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
    		
    		// Observer Handling
    		LinkedList<Packet> toObservers = new LinkedList<Packet>();
    		DroppedDisguise disguise = new DroppedDisguise(disguiseDB.get(name), name, player.getLocation());
    		if (disguise.type.isPlayer()) {
    			toObservers.add(disguise.packetGenerator.getPlayerInfoPacket(player, false));
    		}
    		undisguiseToWorld(player.getWorld(), player, toObservers);
    		
    		// Undisguised Player Handling
    		sendPacketsToObserver(player, disguise.getSpawnPackets(player));
    		
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
    	LinkedList<Packet> toSend = new LinkedList<Packet>();
		Disguise disguise = disguiseDB.get(disguised.getName());
		toSend.add(disguise.packetGenerator.getSpawnPacket(disguised));
		if (disguise.type.isPlayer()) { // Player disguise
			if (!pluginSettings.noTabHide) {
				toSend.add(disguise.packetGenerator.getPlayerInfoPacket(disguised, true));
			}
			toSend.addAll(disguise.packetGenerator.getArmorPackets(disguised));
		} else if (disguise.type == DisguiseType.Zombie || disguise.type == DisguiseType.PigZombie || disguise.type == DisguiseType.Skeleton) {
			toSend.add(disguise.packetGenerator.getEquipmentChangePacket((short) 0, disguised.getItemInHand()));
			toSend.addAll(disguise.packetGenerator.getArmorPackets(disguised));
		}
		if (observer == null) {
			disguiseToWorld(disguised.getWorld(), disguised, toSend);
		} else {
			if (!hasPermissions(observer, "disguisecraft.seer")) {
				if (pluginSettings.noTabHide) {
					packetListener.recentlyDisguised.add(disguised.getName());
				}
				observer.hidePlayer(disguised);
			}
			sendPacketsToObserver(observer, toSend);
		}
    }
    
    public void sendUnDisguise(Player disguised, Player observer) {
    	LinkedList<Packet> toSend = new LinkedList<Packet>();
		Disguise disguise = disguiseDB.get(disguised.getName());
		toSend.add(disguise.packetGenerator.getEntityDestroyPacket());
		if (disguise.type.isPlayer() && !pluginSettings.noTabHide) {
			toSend.add(disguise.packetGenerator.getPlayerInfoPacket(disguised, false));
		}
		if (observer == null) {
			undisguiseToWorld(disguised.getWorld(), disguised, toSend);
		} else {
			sendPacketsToObserver(observer, toSend);
			observer.showPlayer(disguised);
		}
    }
    
    public void sendMovement(Player disguised, Player observer, Vector vector, Location to) {
    	LinkedList<Packet> toSend = new LinkedList<Packet>();
		Disguise disguise = disguiseDB.get(disguised.getName());
		
		// Block lock
		if (disguise.data.contains("blocklock")) {
			to = to.getBlock().getLocation();
			to.setX(to.getX() + 0.5);
			to.setZ(to.getZ() + 0.5);
		}
		
		// Vehicle fix
    	if (disguise.type.isVehicle()) {
    		to.setY(to.getY() + 0.5);
    	}
		
		MovementValues movement = disguise.packetGenerator.getMovement(to);
		
		if (pluginSettings.bandwidthReduction) {
			if (movement.x < -128 || movement.x > 128 || movement.y < -128 || movement.y > 128 || movement.z < -128 || movement.z > 128) { // That's like a teleport right there!
    			Packet packet = disguise.packetGenerator.getEntityTeleportPacket(to);
    			if (observer == null) {
					sendPacketToWorld(disguised.getWorld(), packet);
				} else {
					((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
				}
    		} else { // Relative movement
    			if (movement.x == 0 && movement.y == 0 && movement.z == 0) { // Just looked around
    				//Client doesn't seem to want to register this
    				toSend.add(disguise.packetGenerator.getEntityLookPacket(to));
    				toSend.add(disguise.packetGenerator.getHeadRotatePacket(to));
    			} else { // Moved legs
    				toSend.add(disguise.packetGenerator.getEntityMoveLookPacket(to));
    				toSend.add(disguise.packetGenerator.getHeadRotatePacket(to));
    				
    			}
    		}
		} else {
			toSend.add(disguise.packetGenerator.getHeadRotatePacket(to));
    		if (movement.x == 0 && movement.y == 0 && movement.z == 0) { // Just looked around
    			toSend.add(disguise.packetGenerator.getEntityLookPacket(to));
			} else {
				toSend.add(disguise.packetGenerator.getEntityTeleportPacket(to));
			}
		}
		if (observer == null) {
			sendPacketsToWorld(disguised.getWorld(), toSend);
		} else {
			sendPacketsToObserver(observer, toSend);
		}
    }
    
    public void sendPacketToWorld(World world, Packet packet) {
    	for (Player observer : world.getPlayers()) {
    		((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(packet);
    	}
    }
    
    public void sendPacketsToWorld(World world, LinkedList<Packet> packets) {
    	for (Player observer : world.getPlayers()) {
    		for (Packet p : packets) {
    			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
    		}
    	}
    }
    
    public void sendPacketsToObserver(Player observer, LinkedList<Packet> packets) {
    	for (Packet p : packets) {
			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
		}
    }
    
    public void disguiseToWorld(World world, Player player, LinkedList<Packet> packets) {
    	for (Player observer : world.getPlayers()) {
	    	if (observer != player) {
	    		if (!hasPermissions(observer, "disguisecraft.seer")) {
	    			if (pluginSettings.noTabHide) {
						packetListener.recentlyDisguised.add(player.getName());
					}
					observer.hidePlayer(player);
				}
	    		for (Packet p : packets) {
	    			((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(p);
	    		}
    		}
    	}
    }
    
    public void undisguiseToWorld(World world, Player player, LinkedList<Packet> packets) {
    	for (Player observer : world.getPlayers()) {
    		if (observer != player) {
	    		for (Packet p : packets) {
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
			if (pluginSettings.noTabHide) {
				((CraftPlayer) observer).getHandle().netServerHandler.sendPacket(new Packet201PlayerInfo(disguisedName, true, ((CraftPlayer) disguised).getHandle().ping));
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
