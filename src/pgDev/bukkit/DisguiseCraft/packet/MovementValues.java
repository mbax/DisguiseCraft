package pgDev.bukkit.DisguiseCraft.packet;

public class MovementValues {
	
	public int x;
	public int y;
	public int z;
	public int yaw;
	public int pitch;
	
	public MovementValues(int x, int y, int z, int yaw, int pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

}
