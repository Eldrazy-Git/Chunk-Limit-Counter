package com.eldrazy.chunklimitcounter.counting;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.world.level.ChunkPos;

/**
 * Snapshot of every tracked count for a single chunk.
 */
public class CountResult {
	public final ChunkPos chunkPos;
	/** Category name -> total count in this chunk. */
	public final Map<String, Integer> categoryTotals = new LinkedHashMap<>();
	/** Tracked block/entity id -> exact count in this chunk. */
	public final Map<String, Integer> perIdCounts = new LinkedHashMap<>();
	/** Untracked block/block-entity id -> count, only populated when "show all" is enabled. */
	public final Map<String, Integer> otherCounts = new LinkedHashMap<>();

	public CountResult(ChunkPos chunkPos) {
		this.chunkPos = chunkPos;
	}

	public int categoryTotal(String categoryName) {
		return categoryTotals.getOrDefault(categoryName, 0);
	}

	void addToCategories(Iterable<com.eldrazy.chunklimitcounter.config.CategoryConfig> categories, int amount) {
		for (com.eldrazy.chunklimitcounter.config.CategoryConfig category : categories) {
			categoryTotals.merge(category.name, amount, Integer::sum);
		}
	}

	void addToId(String id, int amount) {
		perIdCounts.merge(id, amount, Integer::sum);
	}

	void addOther(String id, int amount) {
		otherCounts.merge(id, amount, Integer::sum);
	}
}
