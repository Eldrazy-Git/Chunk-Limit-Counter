package com.eldrazy.chunklimitcounter.hud;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

/**
 * Draws the HUD as a single grouped panel (chunk coords in the header, then one
 * row per category with an icon swatch, text and a fill bar), with just the
 * corner pixel shaved off each side - not a multi-pixel staircase.
 */
public final class HudPanelRenderer {
	private static final int PADDING = 6;
	private static final int ICON_SIZE = 6;
	private static final int ICON_GAP = 4;
	private static final int BAR_HEIGHT = 3;
	private static final int BAR_GAP = 2;
	private static final int ROW_GAP = 4;
	private static final int MIN_WIDTH = 130;

	private static final int COLOR_EDIT_OUTLINE = 0xFFFFD542;
	private static final int COLOR_TRACK = 0x40FFFFFF;
	private static final int COLOR_SUBTITLE = 0xFF9A9A9A;

	private HudPanelRenderer() {
	}

	public static String chunkCoordsText(ChunkPos chunkPos) {
		return Component.translatable("gui.chunklimitcounter.hud.chunk_coords", chunkPos.x(), chunkPos.z()).getString();
	}

	private static int headerHeight(Font font) {
		return font.lineHeight + 6;
	}

	private static int rowHeight(Font font) {
		return font.lineHeight + BAR_GAP + BAR_HEIGHT + ROW_GAP;
	}

	public static int width(Font font, List<HudLayout.Line> lines) {
		int textWidth = 0;
		for (HudLayout.Line line : lines) {
			// Measure the actual styled component, not the plain string: bold glyphs render
			// wider than normal ones, so a category that goes bold on overflow needs that
			// extra width accounted for here too, or its text pokes out of the panel.
			textWidth = Math.max(textWidth, font.width(HudLayout.styledText(line.text(), line.bold())));
		}
		int content = PADDING * 2 + ICON_SIZE + ICON_GAP + textWidth;
		return Math.max(MIN_WIDTH, content);
	}

	public static int height(Font font, List<HudLayout.Line> lines) {
		if (lines.isEmpty()) {
			return headerHeight(font) + PADDING;
		}
		return headerHeight(font) + lines.size() * rowHeight(font) - ROW_GAP + PADDING;
	}

	public static void render(GuiGraphicsExtractor graphics, Font font, List<HudLayout.Line> lines,
			ChunkPos chunkPos, int x, int y, int width, int height, int backgroundAlpha, boolean editHighlight) {
		drawPanelBackground(graphics, x, y, width, height, backgroundAlpha, editHighlight);

		if (chunkPos != null) {
			String coords = chunkCoordsText(chunkPos);
			int coordsWidth = font.width(coords);
			graphics.text(font, coords, x + width - PADDING - coordsWidth, y + 4, COLOR_SUBTITLE);
		}

		int rowY = y + headerHeight(font);
		int barLeft = x + PADDING + ICON_SIZE + ICON_GAP;
		int barRight = x + width - PADDING;

		for (HudLayout.Line line : lines) {
			graphics.fill(x + PADDING, rowY + 1, x + PADDING + ICON_SIZE, rowY + 1 + ICON_SIZE, line.color());
			graphics.text(font, HudLayout.styledText(line.text(), line.bold()), barLeft, rowY, line.color());

			int barY = rowY + font.lineHeight + BAR_GAP;
			graphics.fill(barLeft, barY, barRight, barY + BAR_HEIGHT, COLOR_TRACK);
			int filled = (int) (Math.min(1f, line.ratio()) * (barRight - barLeft));
			if (filled > 0) {
				graphics.fill(barLeft, barY, barLeft + filled, barY + BAR_HEIGHT, line.color());
			}

			rowY += rowHeight(font);
		}
	}

	/** Plain dark transparent fill with just the single corner pixel missing on each side - nothing fancier. */
	private static void drawPanelBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int backgroundAlpha, boolean editHighlight) {
		int x2 = x + width;
		int y2 = y + height;

		int alpha = Math.max(0, Math.min(255, backgroundAlpha));
		int background = (alpha << 24);
		graphics.fill(x + 1, y, x2 - 1, y2, background);
		graphics.fill(x, y + 1, x2, y2 - 1, background);

		if (editHighlight) {
			graphics.horizontalLine(x + 1, x2 - 2, y, COLOR_EDIT_OUTLINE);
			graphics.horizontalLine(x + 1, x2 - 2, y2 - 1, COLOR_EDIT_OUTLINE);
			graphics.verticalLine(x, y + 1, y2 - 2, COLOR_EDIT_OUTLINE);
			graphics.verticalLine(x2 - 1, y + 1, y2 - 2, COLOR_EDIT_OUTLINE);
		}
	}
}
