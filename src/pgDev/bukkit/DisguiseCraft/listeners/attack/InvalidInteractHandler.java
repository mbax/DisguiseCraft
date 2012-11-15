package pgDev.bukkit.DisguiseCraft.listeners.attack;

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;
import pgDev.bukkit.DisguiseCraft.listeners.PlayerInvalidInteractEvent;

public class InvalidInteractHandler implements Runnable {
	PlayerInvalidInteractEvent event;
	DisguiseCraft plugin;
	
	public InvalidInteractHandler(PlayerInvalidInteractEvent event, DisguiseCraft plugin) {
		this.event = event;
		this.plugin = plugin;
	}

	@Override
	public void run() {
		if (plugin.disguiseIDs.containsKey(event.getTarget())) {
			Player player = event.getPlayer();
			Player attacked = plugin.disguiseIDs.get(event.getTarget());
			if (event.getAction()) {
				// Send attack to queue
				plugin.attackProcessor.queue.offer(new PlayerAttack(player, attacked));
				plugin.attackProcessor.incrementAmount();
			} else {
				if (player.getItemInHand().getType() == Material.SHEARS) {
					Disguise disguise = plugin.disguiseDB.get(attacked.getName());
					if (disguise.type == DisguiseType.MushroomCow) {
						((CraftPlayer) player).getHandle().netServerHandler.sendPacket(disguise.packetGenerator.getMobSpawnPacket(attacked.getLocation()));
					}
				}
			}
		}
	}

}
