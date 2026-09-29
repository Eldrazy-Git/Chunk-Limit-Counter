package com.eldrazy.chunklimitcounter.gui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.eldrazy.chunklimitcounter.ChunkLimitCounterClient;
import com.eldrazy.chunklimitcounter.config.CategoryConfig;
import com.eldrazy.chunklimitcounter.config.ModConfig;
import com.eldrazy.chunklimitcounter.counting.ChunkExporter;
import com.eldrazy.chunklimitcounter.counting.CountResult;
import com.eldrazy.chunklimitcounter.counting.TrackedRegistry;
import com.eldrazy.chunklimitcounter.hud.HudLayout;

/** Full breakdown screen: exact count per tracked block/entity id, not just the per-category total. */
public class ChunkDetailScreen extends Screen {
	private static final int TOP_MARGIN = 34;
	private static final int LINE_HEIGHT = 11;
	private static final int INDENT = 10;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_WIDTH = 110;
	private static final int BOTTOM_MARGIN = 8;
	private static final int GAP = 4;
	private static final int CATEGORY_GAP = 6;
	private static final int DIVIDER_COLOR = 0x33FFFFFF;
	private static final int STATUS_TICKS = 100;

	private int scrollOffset;
	private int maxScroll;
	private Component statusMessage;
	private int statusMessageTicksLeft;

	public ChunkDetailScreen() {
		super(Component.translatable("gui.chunklimitcounter.detail.title"));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private int buttonY() {
		return this.height - BOTTOM_MARGIN - BUTTON_HEIGHT;
	}

	@Override
	protected void init() {
		int buttonY = buttonY();
		int gap = 10;
		int totalWidth = BUTTON_WIDTH * 2 + gap;
		int startX = (this.width - totalWidth) / 2;

		this.addRenderableWidget(Button.builder(Component.translatable("gui.chunklimitcounter.detail.export"), button -> exportJson())
				.bounds(startX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());

		this.addRenderableWidget(Button.builder(Component.translatable("gui.chunklimitcounter.detail.screenshot"), button -> takeScreenshot())
				.bounds(startX + BUTTON_WIDTH + gap, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
	}

	private void exportJson() {
		ChunkLimitCounterClient client = ChunkLimitCounterClient.getInstance();
		CountResult result = client.lastResult();
		if (result == null) {
			showStatus(Component.translatable("gui.chunklimitcounter.detail.export_no_data"));
			return;
		}

		try {
			Path file = ChunkExporter.export(result, client.config());
			showStatus(Component.translatable("gui.chunklimitcounter.detail.export_success", file.getFileName().toString()));
		} catch (IOException e) {
			ChunkLimitCounterClient.LOGGER.error("Echec de l'export JSON", e);
			showStatus(Component.translatable("gui.chunklimitcounter.detail.export_failure"));
		}
	}

	private void takeScreenshot() {
		Screenshot.grab(Minecraft.getInstance(), true);
	}

	private void showStatus(Component message) {
		this.statusMessage = message;
		this.statusMessageTicksLeft = STATUS_TICKS;
	}

	@Override
	public void tick() {
		super.tick();
		if (statusMessageTicksLeft > 0) {
			statusMessageTicksLeft--;
		}
	}

	private record Row(String text, int color, boolean bold, boolean header) {
	}

	private List<Row> buildRows() {
		List<Row> rows = new ArrayList<>();
		ChunkLimitCounterClient client = ChunkLimitCounterClient.getInstance();
		ModConfig config = client.config();
		TrackedRegistry registry = client.registry();
		CountResult result = client.lastResult();

		if (result == null) {
			rows.add(new Row(Component.translatable("gui.chunklimitcounter.detail.no_data").getString(), 0xFFAAAAAA, false, false));
			return rows;
		}

		for (CategoryConfig category : config.categories) {
			int total = result.categoryTotal(category.name);
			boolean exceeded = HudLayout.isExceeded(total, category.limit);
			int headerColor = HudLayout.statusColor(total, category.limit);
			rows.add(new Row(category.name + " - " + total + "/" + category.limit, headerColor, exceeded, true));

			boolean any = false;
			for (String rawId : category.ids) {
				String id = rawId == null ? null : rawId.trim();
				if (id == null || id.isEmpty() || id.startsWith("//") || id.startsWith("#")) {
					continue;
				}
				Integer count = result.perIdCounts.get(id);
				if (count != null && count > 0) {
					String name = registry.displayNameFor(id).getString();
					rows.add(new Row(name + " : " + count, 0xFFE0E0E0, false, false));
					any = true;
				}
			}
			if (!any) {
				rows.add(new Row(Component.translatable("gui.chunklimitcounter.detail.empty").getString(), 0xFF808080, false, false));
			}
		}

		if (config.showAllBlocksInDetailScreen && !result.otherCounts.isEmpty()) {
			rows.add(new Row(Component.translatable("gui.chunklimitcounter.detail.others").getString(), 0xFFFFFFFF, false, true));
			for (Map.Entry<String, Integer> entry : result.otherCounts.entrySet()) {
				String name = registry.displayNameFor(entry.getKey()).getString();
				rows.add(new Row(name + " : " + entry.getValue(), 0xFFA0A0A0, false, false));
			}
		}

		return rows;
	}

	/** Relative y-offset of each row, adding extra breathing room before every category header but the first. */
	private int[] computeRowOffsets(List<Row> rows) {
		int[] offsets = new int[rows.size()];
		int y = 0;
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0 && rows.get(i).header()) {
				y += CATEGORY_GAP;
			}
			offsets[i] = y;
			y += LINE_HEIGHT;
		}
		return offsets;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		Font font = this.font;

		graphics.fill(0, 0, this.width, this.height, 0xC0101010);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, this.title, this.width / 2, 12, 0xFFFFFFFF);

		List<Row> rows = buildRows();
		int[] offsets = computeRowOffsets(rows);
		int statusY = buttonY() - GAP - font.lineHeight;
		int viewportTop = TOP_MARGIN;
		int viewportBottom = statusY - GAP;
		int contentHeight = rows.isEmpty() ? 0 : offsets[rows.size() - 1] + LINE_HEIGHT;
		maxScroll = Math.max(0, contentHeight - (viewportBottom - viewportTop));
		scrollOffset = Math.min(scrollOffset, maxScroll);

		int columnWidth = 0;
		for (Row row : rows) {
			columnWidth = Math.max(columnWidth, font.width(row.text()) + (row.header() ? 0 : INDENT));
		}
		int columnX = Math.max(8, (this.width - columnWidth) / 2);

		graphics.enableScissor(0, viewportTop, this.width, viewportBottom);
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			int y = viewportTop - scrollOffset + offsets[i];
			if (y + LINE_HEIGHT >= viewportTop && y <= viewportBottom) {
				if (i > 0 && row.header()) {
					int dividerY = y - CATEGORY_GAP / 2 - 1;
					graphics.horizontalLine(columnX, columnX + columnWidth, dividerY, DIVIDER_COLOR);
				}
				int x = row.header() ? columnX : columnX + INDENT;
				graphics.text(font, HudLayout.styledText(row.text(), row.bold()), x, y, row.color());
			}
		}
		graphics.disableScissor();

		if (statusMessageTicksLeft > 0 && statusMessage != null) {
			graphics.centeredText(font, statusMessage, this.width / 2, statusY, 0xFF55FF55);
		} else {
			graphics.centeredText(font, Component.translatable("gui.chunklimitcounter.detail.close_hint"), this.width / 2, statusY, 0xFF808080);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (scrollY * LINE_HEIGHT * 2)));
		return true;
	}
}
