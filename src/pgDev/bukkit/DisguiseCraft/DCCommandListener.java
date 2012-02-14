package pgDev.bukkit.DisguiseCraft;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.Disguise.MobType;

public class DCCommandListener implements CommandExecutor {
	final DisguiseCraft plugin;
	
	public DCCommandListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Differentiate console input
		boolean isConsole = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			if (args.length == 0 || (player = plugin.getServer().getPlayer(args[0])) == null) {
				System.out.println("Because you are using the console, you must specify a player as your first argyment.");
				return true;
			} else {
				isConsole = true;
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}
		
		// Start command parsing
		if (label.toLowerCase().startsWith("d")) {
			if (args.length == 0) { // He needs help!
				if (isConsole) { // Console output
					sender.sendMessage("Usage: /" + label + " " + player.getName() + " [subtype] <mob/playername>");
					String types = MobType.values().toString();
					sender.sendMessage("Available types: " + types.substring(1, types.length() - 1));
					String subTypes = MobType.subTypes.toString();
					sender.sendMessage("Available subtypes: " + subTypes.substring(1, subTypes.length() - 1));
				} else { // Player output
					player.sendMessage(ChatColor.GREEN + "Usage: /" + label + " [subtype] <mob/playername>");
					String types = "";
					for (MobType type : MobType.values()) {
						if (plugin.hasPermissions(player, "disguisecraft." + type.name().toLowerCase())) {
							if (types.equals("")) {
								types = type.name();
							} else {
								types = types + ", " + type.name();
							}
						}
					}
					if (!types.equals("")) {
						player.sendMessage(ChatColor.GREEN + "Available types: " + types);
					}
					String subTypes = MobType.subTypes.toString();
					player.sendMessage(ChatColor.GREEN + "Available subtypes: " + subTypes.substring(1, subTypes.length() - 1));
				}
			} else if (args[0].equalsIgnoreCase("player")) {
				if (isConsole || plugin.hasPermissions(player, "disguisecraft.player")) {
					if (args.length > 1) {
						Disguise disguise;
						if (plugin.disguiseDB.containsKey(player.getName())) {
							disguise = plugin.disguiseDB.get(player.getName());
							disguise.setData(args[1]);
							disguise.setMob(null);
							plugin.sendPacketToWorld(player.getWorld(), disguise.getEntityDestroyPacket());
						} else {
							disguise = new Disguise(plugin.getNextAvailableID(), args[1], null);
						}
						plugin.disguisePlayer(player, disguise);
						player.sendMessage(ChatColor.GOLD + "You have been disguised as player: " + args[1]);
						if (isConsole) {
							sender.sendMessage(player.getName() + " was disguised as player: " + args[1]);
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You must specify the player to disguis as.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have the permission to diguise as another player.");
				}
			} else if (args[0].equalsIgnoreCase("baby")) {
				if (args.length > 1) { // New disguise
					
				} else { // Current mob
					if (plugin.disguiseDB.containsKey(player.getName())) {
						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
						if (Arrays.asList(disguise.data.split(",")).contains("baby")) {
							sender.sendMessage(ChatColor.RED + "Already in baby form.");
						} else {
							if (disguise.mob.isSubclass(Animals.class)) {
								if (disguise.data == null) {
									disguise.setData("baby");
								} else {
									disguise.setData(disguise.data + ",baby");
								}
								plugin.changeDisguise(player, disguise);
								player.sendMessage(ChatColor.GOLD + "You have been disguised as a baby " + disguise.mob.name());
								if (isConsole) {
									sender.sendMessage(player.getName() + " was disguised as a baby " + disguise.mob.name());
								}
							} else {
								sender.sendMessage(ChatColor.RED + "No baby form for: " + disguise.mob.name());
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Not currently disguised. A mobtype must be given.");
					}
				}
			} else {
				MobType type = MobType.fromString(args[0]);
				if (type == null) {
					sender.sendMessage(ChatColor.RED + "That mob type was not recognized.");
				} else {
					if (plugin.disguiseDB.containsKey(player.getName())) {
						Disguise disguise = plugin.disguiseDB.get(player.getName()).clone();
						disguise.setData(null);
						disguise.setMob(type);
						plugin.changeDisguise(player, disguise);
					} else {
						plugin.disguisePlayer(player, new Disguise(plugin.getNextAvailableID(), null, type));
					}
					player.sendMessage(ChatColor.GOLD + "You have been disguised as a " + type.name());
					if (isConsole) {
						sender.sendMessage(player.getName() + " was disguised as a " + type.name());
					}
				}
			}
		} else if (label.toLowerCase().startsWith("u")) {
			if (plugin.disguiseDB.containsKey(player.getName())) {
				plugin.unDisguisePlayer(player);
				player.sendMessage(ChatColor.GOLD + "You were undisguised.");
				if (isConsole) {
					sender.sendMessage(player.getName() + " was undisguised.");
				}
			} else {
				if (isConsole) {
					sender.sendMessage(player.getName() + " is not disguised.");
				} else {
					player.sendMessage(ChatColor.RED + "You are not disguised.");
				}
			}
		}
		return true;
	}
}
