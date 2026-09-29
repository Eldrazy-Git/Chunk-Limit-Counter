package com.eldrazy.chunklimitcounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import com.eldrazy.chunklimitcounter.config.ConfigManager;
import com.eldrazy.chunklimitcounter.config.ModConfig;
import com.eldrazy.chunklimitcounter.counting.ChunkCounter;
import com.eldrazy.chunklimitcounter.counting.CountResult;
import com.eldrazy.chunklimitcounter.counting.TrackedRegistry;
import com.eldrazy.chunklimitcounter.hud.ChunkLimitHudElement;
import com.eldrazy.chunklimitcounter.keybinds.ModKeyBindings;

public class ChunkLimitCounterClient implements ClientModInitializer {
	public static final String MOD_ID = "chunklimitcounter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static ChunkLimitCounterClient instance;

	private ModConfig config;
	private TrackedRegistry registry;
	private CountResult lastResult;

	private ClientLevel lastLevel;
	private ChunkPos lastChunkPos;
	private int ticksSinceRescan;
	private boolean dirty = true;

	public static ChunkLimitCounterClient getInstance() {
		return instance;
	}

	@Override
	public void onInitializeClient() {
		instance = this;
		reloadConfig();

		ModKeyBindings.register();

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hud"), new ChunkLimitHudElement());

		ClientPlayerBlockBreakEvents.AFTER.register((level, player, pos, state) -> markDirty());
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			markDirty();
			return net.minecraft.world.InteractionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);

		LOGGER.info("Chunk Limit Counter pret.");
	}

	public void reloadConfig() {
		this.config = ConfigManager.load();
		this.registry = new TrackedRegistry(config);
		this.dirty = true;
	}

	public void saveConfig() {
		ConfigManager.save(config);
	}

	public ModConfig config() {
		return config;
	}

	public TrackedRegistry registry() {
		return registry;
	}

	public CountResult lastResult() {
		return lastResult;
	}

	public void markDirty() {
		this.dirty = true;
	}

	private void onEndClientTick(Minecraft client) {
		ClientLevel level = client.level;
		if (level == null || client.player == null) {
			lastLevel = null;
			lastChunkPos = null;
			return;
		}

		ChunkPos currentChunk = client.player.chunkPosition();
		boolean levelChanged = level != lastLevel;
		boolean chunkChanged = levelChanged || !currentChunk.equals(lastChunkPos);

		ticksSinceRescan++;
		int interval = Math.max(1, config.safetyNetIntervalTicks);
		boolean intervalElapsed = ticksSinceRescan >= interval;

		if (levelChanged || chunkChanged || dirty || intervalElapsed) {
			lastResult = ChunkCounter.count(level, currentChunk, registry, config.showAllBlocksInDetailScreen);
			lastLevel = level;
			lastChunkPos = currentChunk;
			ticksSinceRescan = 0;
			dirty = false;
		}
	}
}
