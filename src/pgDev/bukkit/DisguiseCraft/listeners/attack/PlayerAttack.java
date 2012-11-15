package pgDev.bukkit.DisguiseCraft.listeners.attack;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerAttack {
	public EntityPlayer attacker;
	public EntityPlayer victim;
	
	public PlayerAttack(Player attacker, Player victim) {
		this.attacker = ((CraftPlayer) attacker).getHandle();
		this.victim = ((CraftPlayer) victim).getHandle();
	}
}
