package com.eldrazy.chunklimitcounter.counting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import com.eldrazy.chunklimitcounter.MinecraftBootstrap;
import com.eldrazy.chunklimitcounter.config.CategoryConfig;
import com.eldrazy.chunklimitcounter.config.ConfigDefaults;
import com.eldrazy.chunklimitcounter.config.ModConfig;

class TrackedRegistryTest {
	@BeforeAll
	static void bootstrap() {
		MinecraftBootstrap.init();
	}

	private static EntityType<?> entity(String path) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.withDefaultNamespace(path)).orElseThrow();
	}

	private static List<String> names(List<CategoryConfig> categories) {
		return categories.stream().map(c -> c.name).toList();
	}

	@Test
	void configParDefautRangeChaqueBlocAuBonEndroit() {
		TrackedRegistry registry = new TrackedRegistry(ConfigDefaults.create());
		assertEquals(List.of("Conteneurs"), names(registry.categoriesForBlock(Blocks.CHEST)));
		assertEquals(List.of("Redstone"), names(registry.categoriesForBlock(Blocks.REPEATER)));
		assertEquals(List.of("Blocs interactifs"), names(registry.categoriesForBlock(Blocks.FURNACE)));
		assertEquals(List.of("Cadres"), names(registry.categoriesForEntityType(entity("item_frame"))));
		assertTrue(registry.isTrackedEntityType(entity("glow_item_frame")));
		assertTrue(registry.categoriesForBlock(Blocks.STONE).isEmpty());
		assertFalse(registry.isTrackedBlock(Blocks.STONE));
	}

	@Test
	void lignesVidesCommentairesEtInconnusSontIgnores() {
		ModConfig config = new ModConfig();
		config.categories.add(new CategoryConfig("Test", 10, "#FFFFFF", new ArrayList<>(Arrays.asList(
				null, "", "   ", "// minecraft:chest", "# minecraft:barrel",
				"minecraft:pas_un_bloc", "Pas Valide!", "  minecraft:hopper  "))));

		TrackedRegistry registry = new TrackedRegistry(config);
		assertTrue(registry.isTrackedBlock(Blocks.HOPPER), "Les espaces autour de l'identifiant sont retirés");
		assertFalse(registry.isTrackedBlock(Blocks.CHEST));
		assertFalse(registry.isTrackedBlock(Blocks.BARREL));
		assertEquals("minecraft:hopper", registry.idOf(Blocks.HOPPER));
	}

	@Test
	void unBlocPeutCompterDansDeuxCategories() {
		ModConfig config = new ModConfig();
		config.categories.add(new CategoryConfig("A", 10, "#FFFFFF", new ArrayList<>(List.of("minecraft:hopper"))));
		config.categories.add(new CategoryConfig("B", 10, "#FFFFFF", new ArrayList<>(List.of("minecraft:hopper"))));
		TrackedRegistry registry = new TrackedRegistry(config);
		assertEquals(List.of("A", "B"), names(registry.categoriesForBlock(Blocks.HOPPER)));
	}

	@Test
	void identifiantDUnBlocNonSuivi() {
		TrackedRegistry registry = new TrackedRegistry(new ModConfig());
		assertEquals("minecraft:stone", registry.idOf(Blocks.STONE));
		assertEquals("minecraft:zombie", registry.idOf(entity("zombie")));
	}
}
