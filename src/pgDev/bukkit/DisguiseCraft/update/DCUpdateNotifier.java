package pgDev.bukkit.DisguiseCraft.update;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DCUpdateNotifier  implements Runnable {
	final DisguiseCraft plugin;
	Player player;
	
	public DCUpdateNotifier(final DisguiseCraft plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}
	
	@Override
	public void run() {
		if (player.isOnline()) {
			String update = DCUpdateChecker.getLatestVersion();
			try {
				if (Integer.parseInt(plugin.version.replace(".", "")) < Integer.parseInt(update.split(" ")[0].replace("v", "").replace(".", ""))) {
					player.sendMessage(ChatColor.BLUE + "There is a new update for DisguiseCraft available: " + update);
				}
			} catch (NumberFormatException e) {
				DisguiseCraft.logger.log(Level.WARNING, "Could not parse version updates.");
			}
		}
	}
}
