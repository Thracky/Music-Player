package info.u_team.music_player.proxy;

import info.u_team.music_player.dependency.DependencyManager;
import info.u_team.music_player.event.EventHandlerMusicPlayer;
import info.u_team.music_player.init.MusicPlayerKeys;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import info.u_team.u_team_core.api.IModProxy;
import info.u_team.u_team_core.registry.util.CommonRegistry;
import net.minecraftforge.api.distmarker.*;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy implements IModProxy {
	
	@Override
	public void construct() {
		super.construct();
		DependencyManager.construct();
		MusicPlayerManager.construct();
		MusicPlayerKeys.construct();
	}
	
	@Override
	public void setup() {
		super.setup();
		CommonRegistry.registerEventHandler(EventHandlerMusicPlayer.class);
	}
	
	@Override
	public void complete() {
		super.complete();
	}
}