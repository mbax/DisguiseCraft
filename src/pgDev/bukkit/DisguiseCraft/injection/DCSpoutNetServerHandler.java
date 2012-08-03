package pgDev.bukkit.DisguiseCraft.injection;

public class DCSpoutNetServerHandler implements DCHandler {
	/*
	private MinecraftServer minecraftServer;
	private final CraftServer server;

	public DCSpoutNetServerHandler(MinecraftServer minecraftserver, INetworkManager networkmanager, EntityPlayer entityplayer) {
		super(minecraftserver, (NetworkManager) networkmanager, entityplayer);
		this.minecraftServer = minecraftserver;
		this.server = minecraftserver.server;
	}
	
	@Override
	public void a(Packet7UseEntity packet7useentity) {
		// Normal Functionality
        super.a(packet7useentity);
        
        // The special part
        if (this.minecraftServer.getWorldServer(this.player.dimension).getEntity(packet7useentity.target) == null) {
        	PlayerInvalidInteractEvent event = new PlayerInvalidInteractEvent((org.bukkit.entity.Player) this.getPlayer(), packet7useentity.target, packet7useentity.action);
            this.server.getPluginManager().callEvent(event);
        }
    }*/
}
