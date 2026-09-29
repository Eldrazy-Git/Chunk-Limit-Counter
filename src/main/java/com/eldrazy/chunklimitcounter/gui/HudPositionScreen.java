package com.eldrazy.chunklimitcounter.gui;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import com.eldrazy.chunklimitcounter.ChunkLimitCounterClient;
import com.eldrazy.chunklimitcounter.config.HudConfig;
import com.eldrazy.chunklimitcounter.hud.HudLayout;
import com.eldrazy.chunklimitcounter.hud.HudPanelRenderer;

/** A lightweight editor screen: shows a live preview of the HUD box and lets the player drag it to a new spot. */
public class HudPositionScreen extends Screen {
	private static final int ALPHA_STEP = 17;
	private static final int OPACITY_BUTTON_SIZE = 20;

	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	private int previewX;
	private int previewY;

	public HudPositionScreen() {
		super(Component.translatable("gui.chunklimitcounter.move_hud.title"));
	}

	@Override
	protected void init() {
		HudConfig hud = ChunkLimitCounterClient.getInstance().config().hud;
		Font font = this.font;
		List<HudLayout.Line> lines = currentLines();
		int contentWidth = HudPanelRenderer.width(font, lines);
		int contentHeight = HudPanelRenderer.height(font, lines);
		previewX = HudLayout.originX(hud, this.width, contentWidth);
		previewY = HudLayout.originY(hud, this.height, contentHeight);

		int centerX = this.width / 2;
		int rowY = this.height - 30;
		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> adjustAlpha(-ALPHA_STEP))
				.bounds(centerX - 70, rowY, OPACITY_BUTTON_SIZE, OPACITY_BUTTON_SIZE)
				.build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> adjustAlpha(ALPHA_STEP))
				.bounds(centerX + 50, rowY, OPACITY_BUTTON_SIZE, OPACITY_BUTTON_SIZE)
				.build());
	}

	private void adjustAlpha(int delta) {
		ChunkLimitCounterClient client = ChunkLimitCounterClient.getInstance();
		HudConfig hud = client.config().hud;
		hud.backgroundAlpha = Math.max(0, Math.min(255, hud.backgroundAlpha + delta));
		client.saveConfig();
	}

	private List<HudLayout.Line> currentLines() {
		ChunkLimitCounterClient client = ChunkLimitCounterClient.getInstance();
		return HudLayout.buildLines(client.registry(), client.lastResult());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0x60000000);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
		graphics.centeredText(this.font, Component.translatable("gui.chunklimitcounter.move_hud.hint"), this.width / 2, 12 + this.font.lineHeight + 4, 0xFFAAAAAA);

		HudConfig hud = ChunkLimitCounterClient.getInstance().config().hud;
		int opacityPercent = Math.round(hud.backgroundAlpha / 255f * 100);
		graphics.centeredText(this.font, Component.translatable("gui.chunklimitcounter.move_hud.opacity", opacityPercent),
				this.width / 2, this.height - 30 + (OPACITY_BUTTON_SIZE - this.font.lineHeight) / 2, 0xFFFFFFFF);

		List<HudLayout.Line> lines = currentLines();
		Font font = this.font;
		int contentWidth = HudPanelRenderer.width(font, lines);
		int contentHeight = HudPanelRenderer.height(font, lines);

		ChunkLimitCounterClient client = ChunkLimitCounterClient.getInstance();
		var result = client.lastResult();
		HudPanelRenderer.render(graphics, font, lines, result == null ? null : result.chunkPos,
				previewX, previewY, contentWidth, contentHeight, hud.backgroundAlpha, true);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		List<HudLayout.Line> lines = currentLines();
		int contentWidth = HudPanelRenderer.width(this.font, lines);
		int contentHeight = HudPanelRenderer.height(this.font, lines);

		int mx = (int) event.x();
		int my = (int) event.y();
		if (mx >= previewX && mx <= previewX + contentWidth && my >= previewY && my <= previewY + contentHeight) {
			dragging = true;
			dragOffsetX = mx - previewX;
			dragOffsetY = my - previewY;
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging) {
			previewX = (int) event.x() - dragOffsetX;
			previewY = (int) event.y() - dragOffsetY;
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging) {
			dragging = false;
			applyAndSave();
			return true;
		}
		return super.mouseReleased(event);
	}

	private void applyAndSave() {
		ChunkLimitCounterClient client = ChunkLimitCounterClient.getInstance();
		HudConfig hud = client.config().hud;
		List<HudLayout.Line> lines = currentLines();
		int contentWidth = HudPanelRenderer.width(this.font, lines);
		int contentHeight = HudPanelRenderer.height(this.font, lines);
		HudLayout.applyDraggedPosition(hud, this.width, this.height, contentWidth, contentHeight, previewX, previewY);
		client.saveConfig();
	}

	@Override
	public void onClose() {
		applyAndSave();
		super.onClose();
	}
}
