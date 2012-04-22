package pgDev.bukkit.DisguiseCraft.listeners;

import net.minecraft.server.Packet18ArmAnimation;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInvalidInteractEvent;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class DCCustomListener implements Listener {
	final DisguiseCraft plugin;
	
	public DCCustomListener(final DisguiseCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDisguiseHit(PlayerInvalidInteractEvent event) {
		if (event.getAction()) {
			Player attacked = plugin.getPlayerFromDisguiseID(event.getTarget());
			if (attacked != null) {
				// Do the attack
				((CraftPlayer) event.getPlayer()).getHandle().attack(((CraftPlayer) attacked).getHandle());
				
				// Send the damage animation
				Packet18ArmAnimation packet = new Packet18ArmAnimation();
				packet.a = event.getTarget();
				packet.b = (byte) 2;
				plugin.sendPacketToWorld(event.getPlayer().getWorld(), packet);
			}
		}
	}
}
