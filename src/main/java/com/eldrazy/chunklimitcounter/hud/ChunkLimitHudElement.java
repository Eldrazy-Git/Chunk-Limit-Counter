package com.eldrazy.chunklimitcounter.hud;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import com.eldrazy.chunklimitcounter.ChunkLimitCounterClient;
import com.eldrazy.chunklimitcounter.config.HudConfig;
import com.eldrazy.chunklimitcounter.counting.CountResult;

public class ChunkLimitHudElement implements HudElement {
	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		ChunkLimitCounterClient client = ChunkLimitCounterClient.getInstance();
		if (client == null || client.config() == null) {
			return;
		}

		HudConfig hud = client.config().hud;
		if (!hud.visible) {
			return;
		}

		CountResult result = client.lastResult();
		List<HudLayout.Line> lines = HudLayout.buildLines(client.registry(), result);
		if (lines.isEmpty()) {
			return;
		}

		Font font = Minecraft.getInstance().font;
		var chunkPos = result == null ? null : result.chunkPos;
		int width = HudPanelRenderer.width(font, lines);
		int height = HudPanelRenderer.height(font, lines);
		int x = HudLayout.originX(hud, graphics.guiWidth(), width);
		int y = HudLayout.originY(hud, graphics.guiHeight(), height);

		HudPanelRenderer.render(graphics, font, lines, chunkPos, x, y, width, height, hud.backgroundAlpha, false);
	}
}
