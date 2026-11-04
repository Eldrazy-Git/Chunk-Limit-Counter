package com.eldrazy.chunklimitcounter.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import com.eldrazy.chunklimitcounter.ChunkLimitCounterClient;
import com.eldrazy.chunklimitcounter.counting.CountResult;
import com.eldrazy.chunklimitcounter.gui.ChunkDetailScreen;

/**
 * Pose de vrais blocs et entités dans le chunk du joueur, puis vérifie que le mod les compte.
 * On compare avant/après : le terrain généré peut déjà contenir des blocs suivis.
 */
public class ChunkCountGameTest implements FabricClientGameTest {
	private static final int CHESTS = 3;
	private static final int REPEATERS = 2;
	private static final int FRAMES = 1;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder()
				.adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
				.create()) {
			world.getConnection().waitForChunksRender();
			world.getServer().runCommand("time set day");
			context.waitTicks(20);

			BlockPos playerPos = context.computeOnClient(mc -> mc.player.blockPosition());
			ChunkPos chunk = new ChunkPos(playerPos.getX() >> 4, playerPos.getZ() >> 4);
			int x0 = chunk.getMinBlockX();
			int z0 = chunk.getMinBlockZ();
			// Plateforme dans le ciel, toujours dans le chunk du joueur : rien ne gêne la pose.
			int y = playerPos.getY() + 6;

			CountResult avant = recount(context);

			world.getServer().runCommand("fill " + x0 + " " + y + " " + z0 + " " + (x0 + 15) + " " + y + " " + (z0 + 15) + " minecraft:stone");
			for (int i = 0; i < CHESTS; i++) {
				// Un bloc d'écart : des coffres collés formeraient un coffre double (toujours 2 blocs, mais restons simples).
				world.getServer().runCommand("setblock " + (x0 + 2 * i) + " " + (y + 1) + " " + z0 + " minecraft:chest");
			}
			for (int i = 0; i < REPEATERS; i++) {
				world.getServer().runCommand("setblock " + (x0 + 2 * i) + " " + (y + 1) + " " + (z0 + 4) + " minecraft:repeater");
			}
			world.getServer().runCommand("summon minecraft:item_frame " + (x0 + 8) + " " + (y + 1) + " " + (z0 + 8)
					+ " {Facing:1b,Fixed:1b}");
			context.waitTicks(40);

			CountResult apres = recount(context);
			assertDelta(avant, apres, "Conteneurs", CHESTS);
			assertDelta(avant, apres, "Redstone", REPEATERS);
			assertDelta(avant, apres, "Cadres", FRAMES);

			// Un bloc cassé doit disparaître du compte.
			world.getServer().runCommand("setblock " + x0 + " " + (y + 1) + " " + z0 + " minecraft:air");
			context.waitTicks(10);
			assertDelta(avant, recount(context), "Conteneurs", CHESTS - 1);

			// Regarde la plateforme pour la capture, puis photographie le HUD et l'écran de détail.
			world.getServer().runCommand("tp @p " + (x0 + 8) + " " + (y + 3) + " " + (z0 + 12) + " 180 40");
			context.waitTicks(20);
			context.takeScreenshot("chunklimitcounter_hud");

			context.runOnClient(mc -> mc.gui.setScreen(new ChunkDetailScreen()));
			context.waitTicks(5);
			context.takeScreenshot("chunklimitcounter_detail");
			context.runOnClient(mc -> mc.gui.setScreen(null));
		}
	}

	/** Demande un nouveau comptage au mod et attend qu'il soit fait (au tick suivant). */
	private static CountResult recount(ClientGameTestContext context) {
		context.runOnClient(mc -> ChunkLimitCounterClient.getInstance().markDirty());
		context.waitTicks(2);
		return context.computeOnClient(mc -> ChunkLimitCounterClient.getInstance().lastResult());
	}

	private static void assertDelta(CountResult avant, CountResult apres, String category, int attendu) {
		int delta = apres.categoryTotal(category) - avant.categoryTotal(category);
		if (delta != attendu) {
			throw new AssertionError(category + " : " + attendu + " en plus attendu(s), " + delta + " compté(s) ("
					+ avant.categoryTotal(category) + " -> " + apres.categoryTotal(category) + ")");
		}
	}
}
