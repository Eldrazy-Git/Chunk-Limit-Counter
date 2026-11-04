package com.eldrazy.chunklimitcounter;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/** Remplit les registres de Minecraft (blocs, entités) une seule fois, sans lancer le jeu. */
public final class MinecraftBootstrap {
	private static boolean done;

	private MinecraftBootstrap() {
	}

	public static synchronized void init() {
		if (!done) {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
			done = true;
		}
	}
}
