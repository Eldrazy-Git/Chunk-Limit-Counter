package com.eldrazy.chunklimitcounter.counting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import com.eldrazy.chunklimitcounter.ChunkLimitCounterClient;
import com.eldrazy.chunklimitcounter.config.CategoryConfig;
import com.eldrazy.chunklimitcounter.config.ModConfig;

/**
 * Resolves the block/entity id strings from the config file into actual registry
 * objects once, so the per-tick counting logic never has to touch string ids or
 * registry lookups.
 */
public class TrackedRegistry {
	private final Map<Block, List<CategoryConfig>> blockCategories = new LinkedHashMap<>();
	private final Map<Block, String> blockIds = new LinkedHashMap<>();
	private final Map<EntityType<?>, List<CategoryConfig>> entityCategories = new LinkedHashMap<>();
	private final Map<EntityType<?>, String> entityIds = new LinkedHashMap<>();
	private final List<CategoryConfig> categories;

	public TrackedRegistry(ModConfig config) {
		this.categories = config.categories;

		for (CategoryConfig category : config.categories) {
			for (String rawId : category.ids) {
				String idString = rawId == null ? null : rawId.trim();
				if (idString == null || idString.isEmpty() || idString.startsWith("//") || idString.startsWith("#")) {
					continue;
				}

				Identifier id = Identifier.tryParse(idString);
				if (id == null) {
					ChunkLimitCounterClient.LOGGER.warn("Identifiant invalide dans la config: '{}' (categorie '{}')", idString, category.name);
					continue;
				}

				Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
				if (block.isPresent()) {
					blockCategories.computeIfAbsent(block.get(), b -> new ArrayList<>()).add(category);
					blockIds.putIfAbsent(block.get(), idString);
					continue;
				}

				Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
				if (entityType.isPresent()) {
					entityCategories.computeIfAbsent(entityType.get(), t -> new ArrayList<>()).add(category);
					entityIds.putIfAbsent(entityType.get(), idString);
					continue;
				}

				ChunkLimitCounterClient.LOGGER.warn("Identifiant inconnu dans la config: '{}' (categorie '{}') - ignore", idString, category.name);
			}
		}
	}

	public List<CategoryConfig> categories() {
		return categories;
	}

	public List<CategoryConfig> categoriesForBlock(Block block) {
		return blockCategories.getOrDefault(block, List.of());
	}

	public List<CategoryConfig> categoriesForEntityType(EntityType<?> type) {
		return entityCategories.getOrDefault(type, List.of());
	}

	public boolean isTrackedBlock(Block block) {
		return blockCategories.containsKey(block);
	}

	public boolean isTrackedEntityType(EntityType<?> type) {
		return entityCategories.containsKey(type);
	}

	public String idOf(Block block) {
		String known = blockIds.get(block);
		if (known != null) {
			return known;
		}
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		return id == null ? "unknown" : id.toString();
	}

	public String idOf(EntityType<?> type) {
		String known = entityIds.get(type);
		if (known != null) {
			return known;
		}
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
		return id == null ? "unknown" : id.toString();
	}

	/** Localized display name (follows the game's current language) for a block/entity id, falling back to the raw id. */
	public Component displayNameFor(String id) {
		Identifier identifier = Identifier.tryParse(id);
		if (identifier == null) {
			return Component.literal(id);
		}

		Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(identifier);
		if (block.isPresent()) {
			return block.get().getName();
		}

		Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(identifier);
		if (entityType.isPresent()) {
			return entityType.get().getDescription();
		}

		return Component.literal(id);
	}
}
