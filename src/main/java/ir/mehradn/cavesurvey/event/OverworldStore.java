package ir.mehradn.cavesurvey.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class OverworldStore {
	private static MinecraftServer instance;

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTING.register(OverworldStore::storeServerInstance);
	}

	private static void storeServerInstance(MinecraftServer minecraftServer) {
		OverworldStore.instance = minecraftServer;
	}

	public static ServerLevel get() {
		return instance.overworld();
	}
}
