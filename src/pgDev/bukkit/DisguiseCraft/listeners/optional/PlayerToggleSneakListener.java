package pgDev.bukkit.DisguiseCraft.listeners.optional;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;

public class PlayerToggleSneakListener implements Listener {
	final DisguiseCraft plugin;
	
	public PlayerToggleSneakListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (!event.isCancelled()) {
			if (plugin.disguiseDB.containsKey(event.getPlayer().getName())) {
				Disguise disguise = plugin.disguiseDB.get(event.getPlayer().getName());
				if (disguise.type.isPlayer()) {
					disguise.setCrouch(event.isSneaking());
					plugin.sendPacketToWorld(event.getPlayer().getWorld(), disguise.packetGenerator.getEntityMetadataPacket());
				}
			}
		}
	}
}
