package pgDev.bukkit.DisguiseCraft.delayedtasks;

import java.util.TimerTask;

import net.minecraft.server.Packet;

import org.bukkit.World;
import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class WorldDisguiseTask extends TimerTask {
	DisguiseCraft plugin;
	World world;
	Player player;
	Packet[] packet;
	
	public WorldDisguiseTask(DisguiseCraft plugin, World world, Player player, Packet... packet) {
		this.plugin = plugin;
		this.world = world;
		this.player = player;
		this.packet = packet;
	}
	
	public void run() {
		plugin.disguiseToWorld(player.getWorld(), player, packet);
	}

}
