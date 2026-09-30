package com.eldrazy.chunklimitcounter.config;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	/** Safety-net rescan interval in client ticks (20 ticks = 1 second). */
	public int safetyNetIntervalTicks = 20;

	/** If true, the detail screen also lists every other block/block-entity found in the chunk. */
	public boolean showAllBlocksInDetailScreen = false;

	public HudConfig hud = new HudConfig();

	/**
	 * Hard-coded by {@link ConfigDefaults}, never read from or written to config.json:
	 * the category limits are not meant to be user-editable.
	 */
	public transient List<CategoryConfig> categories = new ArrayList<>();
}
