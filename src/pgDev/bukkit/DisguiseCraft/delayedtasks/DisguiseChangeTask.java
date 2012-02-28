package pgDev.bukkit.DisguiseCraft.delayedtasks;

import java.util.TimerTask;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DisguiseChangeTask extends TimerTask {
	DisguiseCraft plugin;
	Player player;
	Disguise newDisguise;
	
	public DisguiseChangeTask(DisguiseCraft plugin, Player player, Disguise newDisguise) {
		this.plugin = plugin;
		this.player = player;
		this.newDisguise = newDisguise;
	}
	
	public void run() {
		plugin.unDisguisePlayer(player);
		try {
			wait(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		plugin.disguisePlayer(player, newDisguise);
	}
}
