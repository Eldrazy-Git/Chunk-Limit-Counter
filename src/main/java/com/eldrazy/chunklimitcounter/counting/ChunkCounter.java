package com.eldrazy.chunklimitcounter.counting;

import java.util.List;
import java.util.Map;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.AABB;

import com.eldrazy.chunklimitcounter.config.CategoryConfig;

/**
 * Counts tracked blocks/block-entities/entities within a single chunk column
 * (all sections, full world height). Never looks at neighboring chunks.
 */
public final class ChunkCounter {
	private ChunkCounter() {
	}

	public static CountResult count(ClientLevel level, ChunkPos chunkPos, TrackedRegistry registry, boolean showAll) {
		CountResult result = new CountResult(chunkPos);

		LevelChunk chunk = level.getChunk(chunkPos.x(), chunkPos.z());
		if (chunk == null) {
			return result;
		}

		countSimpleBlocks(chunk, registry, showAll, result);
		countBlockEntities(chunk, registry, showAll, result);
		countEntities(level, chunkPos, registry, result);

		return result;
	}

	/**
	 * Scans each 16x16x16 section's block-state palette (not every block position)
	 * to tally simple, non-block-entity blocks such as redstone dust or rails.
	 */
	private static void countSimpleBlocks(LevelChunk chunk, TrackedRegistry registry, boolean showAll, CountResult result) {
		for (LevelChunkSection section : chunk.getSections()) {
			if (section == null || section.hasOnlyAir()) {
				continue;
			}

			PalettedContainer<BlockState> states = section.getStates();
			states.count((state, count) -> {
				if (state.isAir()) {
					return;
				}

				Block block = state.getBlock();
				if (block instanceof EntityBlock) {
					// Counted precisely via the chunk's block-entity map instead.
					return;
				}

				List<CategoryConfig> cats = registry.categoriesForBlock(block);
				if (!cats.isEmpty()) {
					result.addToCategories(cats, count);
					result.addToId(registry.idOf(block), count);
				} else if (showAll) {
					result.addOther(registry.idOf(block), count);
				}
			});
		}
	}

	/** Block-entities (chests, furnaces, signs, ...) are read straight from the chunk's map, no scanning needed. */
	private static void countBlockEntities(LevelChunk chunk, TrackedRegistry registry, boolean showAll, CountResult result) {
		for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
			BlockState state = entry.getValue().getBlockState();
			Block block = state.getBlock();

			List<CategoryConfig> cats = registry.categoriesForBlock(block);
			if (!cats.isEmpty()) {
				result.addToCategories(cats, 1);
				result.addToId(registry.idOf(block), 1);
			} else if (showAll) {
				result.addOther(registry.idOf(block), 1);
			}
		}
	}

	/** Item frames and similar decorations are entities, not blocks: query them by bounding box. */
	private static void countEntities(ClientLevel level, ChunkPos chunkPos, TrackedRegistry registry, CountResult result) {
		AABB columnBounds = new AABB(
				chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
				chunkPos.getMaxBlockX() + 1, level.getMaxY(), chunkPos.getMaxBlockZ() + 1
		);

		List<Entity> entities = level.getEntities((Entity) null, columnBounds,
				entity -> registry.isTrackedEntityType(entity.getType()));

		for (Entity entity : entities) {
			EntityType<?> type = entity.getType();
			List<CategoryConfig> cats = registry.categoriesForEntityType(type);
			if (!cats.isEmpty()) {
				result.addToCategories(cats, 1);
				result.addToId(registry.idOf(type), 1);
			}
		}
	}
}
