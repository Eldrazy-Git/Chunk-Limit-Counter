package com.eldrazy.chunklimitcounter.keybinds;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import com.eldrazy.chunklimitcounter.ChunkLimitCounterClient;
import com.eldrazy.chunklimitcounter.gui.ChunkDetailScreen;
import com.eldrazy.chunklimitcounter.gui.HudPositionScreen;

public final class ModKeyBindings {
	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ChunkLimitCounterClient.MOD_ID, "main"));

	public static KeyMapping toggleHud;
	public static KeyMapping openDetailScreen;
	public static KeyMapping moveHud;

	private ModKeyBindings() {
	}

	public static void register() {
		toggleHud = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chunklimitcounter.toggle_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));

		openDetailScreen = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chunklimitcounter.open_detail", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY));

		moveHud = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.chunklimitcounter.move_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(ModKeyBindings::handleTick);
	}

	private static void handleTick(Minecraft client) {
		if (client.player == null) {
			return;
		}

		while (toggleHud.consumeClick()) {
			ChunkLimitCounterClient.getInstance().config().hud.visible =
					!ChunkLimitCounterClient.getInstance().config().hud.visible;
			ChunkLimitCounterClient.getInstance().saveConfig();
		}

		while (openDetailScreen.consumeClick()) {
			if (client.gui.screen() == null) {
				client.setScreenAndShow(new ChunkDetailScreen());
			}
		}

		while (moveHud.consumeClick()) {
			if (client.gui.screen() == null) {
				client.setScreenAndShow(new HudPositionScreen());
			}
		}
	}
}
