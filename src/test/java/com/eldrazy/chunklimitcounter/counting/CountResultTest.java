package com.eldrazy.chunklimitcounter.counting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.world.level.ChunkPos;

import com.eldrazy.chunklimitcounter.config.CategoryConfig;

class CountResultTest {
	private static CategoryConfig category(String name) {
		return new CategoryConfig(name, 10, "#FFFFFF", List.of());
	}

	@Test
	void additionneDansChaqueCategorie() {
		CountResult result = new CountResult(new ChunkPos(0, 0));
		CategoryConfig a = category("A");
		CategoryConfig b = category("B");

		result.addToCategories(List.of(a, b), 3);
		result.addToCategories(List.of(a), 2);

		assertEquals(5, result.categoryTotal("A"));
		assertEquals(3, result.categoryTotal("B"));
	}

	@Test
	void categorieAbsenteVautZero() {
		assertEquals(0, new CountResult(new ChunkPos(0, 0)).categoryTotal("Inconnue"));
	}

	@Test
	void compteParIdentifiant() {
		CountResult result = new CountResult(new ChunkPos(0, 0));
		result.addToId("minecraft:chest", 2);
		result.addToId("minecraft:chest", 1);
		result.addOther("minecraft:stone", 40);
		assertEquals(3, result.perIdCounts.get("minecraft:chest"));
		assertEquals(40, result.otherCounts.get("minecraft:stone"));
	}
}
