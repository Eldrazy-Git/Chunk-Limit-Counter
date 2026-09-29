package com.eldrazy.chunklimitcounter.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.eldrazy.chunklimitcounter.config.CategoryConfig;
import com.eldrazy.chunklimitcounter.config.HudConfig;
import com.eldrazy.chunklimitcounter.counting.CountResult;
import com.eldrazy.chunklimitcounter.counting.TrackedRegistry;

/** Shared layout math used by both the real HUD overlay and its drag-to-move preview. */
public final class HudLayout {
	// Flat/pastel palette, closer to the StarHUD look than pure RGB primaries.
	public static final int COLOR_OK = 0xFF6FCF97;
	public static final int COLOR_WARN = 0xFFF2C94C;
	public static final int COLOR_CRITICAL = 0xFFEB5757;

	private HudLayout() {
	}

	public record Line(String categoryName, int count, int limit, String text, int color, boolean bold, float ratio) {
	}

	/** Red only once the limit is actually exceeded; bold follows the same rule. */
	public static int statusColor(int count, int limit) {
		if (count > limit) {
			return COLOR_CRITICAL;
		}
		float ratio = (float) count / (float) Math.max(1, limit);
		return ratio >= 0.7f ? COLOR_WARN : COLOR_OK;
	}

	public static boolean isExceeded(int count, int limit) {
		return count > limit;
	}

	/** Bold only when over the limit, so the overflow actually stands out. */
	public static MutableComponent styledText(String text, boolean bold) {
		Component component = Component.literal(text);
		return ((MutableComponent) component).withStyle(style -> style.withBold(bold));
	}

	public static List<Line> buildLines(TrackedRegistry registry, CountResult result) {
		List<Line> lines = new ArrayList<>();
		if (registry == null) {
			return lines;
		}

		for (CategoryConfig category : registry.categories()) {
			int count = result == null ? 0 : result.categoryTotal(category.name);
			int limit = Math.max(1, category.limit);
			float ratio = (float) count / (float) limit;
			boolean exceeded = isExceeded(count, category.limit);

			lines.add(new Line(category.name, count, category.limit,
					category.name + " : " + count + "/" + category.limit,
					statusColor(count, category.limit), exceeded, ratio));
		}
		return lines;
	}

	public static int originX(HudConfig hud, int guiWidth, int contentWidth) {
		return switch (hud.anchor) {
			case TOP_LEFT, BOTTOM_LEFT -> hud.offsetX;
			case TOP_RIGHT, BOTTOM_RIGHT -> guiWidth - hud.offsetX - contentWidth;
		};
	}

	public static int originY(HudConfig hud, int guiHeight, int contentHeight) {
		return switch (hud.anchor) {
			case TOP_LEFT, TOP_RIGHT -> hud.offsetY;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> guiHeight - hud.offsetY - contentHeight;
		};
	}

	/** Re-anchors the HUD to whichever corner is now closest, from an absolute top-left position. */
	public static void applyDraggedPosition(HudConfig hud, int guiWidth, int guiHeight, int contentWidth, int contentHeight, int newX, int newY) {
		int centerX = newX + contentWidth / 2;
		int centerY = newY + contentHeight / 2;
		boolean left = centerX < guiWidth / 2;
		boolean top = centerY < guiHeight / 2;

		hud.anchor = top
				? (left ? HudConfig.Anchor.TOP_LEFT : HudConfig.Anchor.TOP_RIGHT)
				: (left ? HudConfig.Anchor.BOTTOM_LEFT : HudConfig.Anchor.BOTTOM_RIGHT);

		hud.offsetX = Math.max(0, left ? newX : guiWidth - newX - contentWidth);
		hud.offsetY = Math.max(0, top ? newY : guiHeight - newY - contentHeight);
	}
}
