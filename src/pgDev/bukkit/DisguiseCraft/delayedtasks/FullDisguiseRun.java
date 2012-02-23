package pgDev.bukkit.DisguiseCraft.delayedtasks;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class FullDisguiseRun implements Runnable {
	DisguiseCraft plugin;
	Player player;
	Disguise newDisguise;
	
	public FullDisguiseRun(DisguiseCraft plugin, Player player, Disguise newDisguise) {
		this.plugin = plugin;
		this.player = player;
		this.newDisguise = newDisguise;
	}
	
	public void run() {
		plugin.disguisePlayer(player, newDisguise);
	}
}
