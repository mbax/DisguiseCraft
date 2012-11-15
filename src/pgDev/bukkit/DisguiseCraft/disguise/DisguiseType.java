package pgDev.bukkit.DisguiseCraft.disguise;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Entity;
import net.minecraft.server.WatchableObject;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

/**
 * This is the list of possible disguises listed by
 * their Bukkit class name.
 * @author PG Dev Team (Devil Boy)
 */
public enum DisguiseType {
	//Player
	Player(0),
	
	// Mobs
	Bat(65),
	Blaze(61),
	CaveSpider(59),
	Chicken(93),
	Cow(92),
	Creeper(50),
	EnderDragon(63),
	Enderman(58),
	Ghast(56),
	Giant(53),
	IronGolem(99),
	MagmaCube(62),
	MushroomCow(96),
	Ocelot(98),
	Pig(90),
	PigZombie(57),
	Sheep(91),
	Silverfish(60),
	Skeleton(51),
	Slime(55),
	Snowman(97),
	Spider(52),
	Squid(94),
	Villager(120),
	Witch(66),
	Wither(64),
	Wolf(95),
	Zombie(54),
	
	// Vehicles
	Boat(1),
	Minecart(10),
	PoweredMinecart(12),
	StorageMinecart(11),
	
	//Blocks
	EnderCrystal(51),
	FallingBlock(70),
	TNTPrimed(50);
	
	/**
	 * Entities that are listed in the DisguiseCraft database, but not in
	 * the current Minecraft server version
	 */
	public static LinkedList<DisguiseType> missingDisguises = new LinkedList<DisguiseType>();
	protected static HashMap<Byte, DataWatcher> modelData = new HashMap<Byte, DataWatcher>();
	public static Field mapField;
	
	static {
		// Get model datawatchers
    	try {
    		Field watcherField = net.minecraft.server.Entity.class.getDeclaredField("datawatcher");
    		watcherField.setAccessible(true);
    		
			for (DisguiseType m : values()) {
				if (m.isMob()) {
					String mobClass = "net.minecraft.server.Entity" + m.name();
					if (m == DisguiseType.Giant) {
	    				mobClass = mobClass + "Zombie";
	    			}

	        		try {
	        			Entity ent = (Entity) Class.forName(mobClass).getConstructor(net.minecraft.server.World.class).newInstance((Object) null);
	        			modelData.put(m.id, (DataWatcher) watcherField.get(ent));
	        		} catch (Exception e) {
	        			missingDisguises.add(m);
	        			
	        		}
				}
        	}
		} catch (Exception e) {
			DisguiseCraft.logger.log(Level.SEVERE, "Could not access datawatchers!");
		}
    	
    	// Set map field
    	try {
			mapField = DataWatcher.class.getDeclaredField("b");
			mapField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			DisguiseCraft.logger.log(Level.SEVERE, "Could not find datawatcher map field: b");
		} catch (SecurityException e) {
			DisguiseCraft.logger.log(Level.SEVERE, "Could not access datawatcher map field: b");
		}
	}
	
	/**
	 * The entity-type ID.
	 */
	public final byte id;
	
	DisguiseType(int i) {
		id = (byte) i;
	}
	
	/**
	 * Check if the mob type is a subclass of an Entity class from Bukkit.
	 * This is extremely useful to seeing if a mob can have a certain
	 * subtype. For example: only members of the Animal class (and villagers)
	 * can have a baby form.
	 * @param cls The class to compare to
	 * @return true if the disguisetype is a subclass, false otherwise
	 */
	public boolean isSubclass(Class<?> cls) {
		try {
			return cls.isAssignableFrom(Class.forName("org.bukkit.entity." + name()));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Check if this is a player.
	 * @return true if the type is of a player, false otherwise
	 */
	public boolean isPlayer() {
		return this == Player;
	}
	
	/**
	 * Check if this is a mob.
	 * @return true if the type is of a mob, false otherwise
	 */
	public boolean isMob() {
		return this != Player && isSubclass(LivingEntity.class);
	}
	
	/**
	 * Check if this is an object.
	 * @return true if the type is of an object, false otherwise
	 */
	public boolean isObject() {
		return !(isPlayer() || isMob());
	}
	
	/**
	 * Check if this is a vehicle.
	 * @return true if the type is of a vehicle, false otherwise
	 */
	public boolean isVehicle() {
		return this.isSubclass(Vehicle.class);
	}
	
	/**
	 * Check if this is a block.
	 * @return true if the type is of a block, false otherwise
	 */
	public boolean isBlock() {
		return 	this == EnderCrystal || this == FallingBlock || this == TNTPrimed;
	}
	
	/**
	 * Get the DisguiseType from its name
	 * Works like valueOf, but not case sensitive
	 * @param text The string to match with a DisguiseType
	 * @return The DisguiseType with the given name (null if none are found)
	 */
	public static DisguiseType fromString(String text) {
		for (DisguiseType m : DisguiseType.values()) {
			if (text.equalsIgnoreCase(m.name())) {
				if (missingDisguises.contains(m)) {
					return null;
				} else {
					return m;
				}
			}
		}
		return null;
	}
	
	//@SuppressWarnings("rawtypes")
	@SuppressWarnings("unchecked")
	public DataWatcher newMetadata() {
		if (modelData.containsKey(id)) {
			DataWatcher model = modelData.get(id);
			DataWatcher w = new DataWatcher();
			
			int i = 0;
			for (Field f : model.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				if (i == 1) {
					try {
						HashMap<Integer, WatchableObject> modelMap = ((HashMap<Integer, WatchableObject>) f.get(model));
						HashMap<Integer, WatchableObject> newMap = ((HashMap<Integer, WatchableObject>) f.get(w));
						for (Integer index : modelMap.keySet()) {
							newMap.put(index, copyWatchable(modelMap.get(index)));
						}
					} catch (Exception e) {
						DisguiseCraft.logger.log(Level.SEVERE, "Could not clone hashmap" + i + " in a " + this.name() + "'s model datawatcher!");
					}
				} else if (i == 2) {
					try {
						f.setBoolean(w, f.getBoolean(model));
					} catch (Exception e) {
						DisguiseCraft.logger.log(Level.SEVERE, "Could not clone boolean" + i + " in a " + this.name() + "'s model datawatcher!");
					}
				}
				i++;
			}
			return w;
		} else {
			return new DataWatcher();
		}
	}
	
	private WatchableObject copyWatchable(WatchableObject watchable) {
		return new WatchableObject(watchable.c(), watchable.a(), watchable.b());
	}
	
	/**
	 * Just a string containing the possible subtypes. This is mainly
	 * used for plugin help output.
	 */
	public static String subTypes = "baby, black, blue, brown, cyan, " +
		"gray, green, lightblue, lime, magenta, orange, pink, purple, red, " +
		"silver, white, yellow, sheared, charged, tiny, small, big, bigger, massive, godzilla, " +
		"tamed, aggressive, tabby, tuxedo, siamese, burning, saddled, " +
		"librarian, priest, blacksmith, butcher, nopickup";
}