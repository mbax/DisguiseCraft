package pgDev.bukkit.DisguiseCraft.listeners;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.disguise.*;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class ArmorUpdater implements Runnable {
	final DisguiseCraft plugin;
	final Player player;
	final Disguise disguise;
	
	public ArmorUpdater(DisguiseCraft plugin, Player player, Disguise disguise) {
		this.plugin = plugin;
		this.player = player;
		this.disguise = disguise;
	}

	@Override
	public void run() {
		plugin.sendPacketsToWorld(player.getWorld(), disguise.packetGenerator.getArmorPackets(player));
	}
}
