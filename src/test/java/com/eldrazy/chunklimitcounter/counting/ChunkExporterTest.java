package com.eldrazy.chunklimitcounter.counting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.world.level.ChunkPos;

import com.eldrazy.chunklimitcounter.config.CategoryConfig;
import com.eldrazy.chunklimitcounter.config.ModConfig;

class ChunkExporterTest {
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 5, 14, 30, 7);

	@TempDir
	Path dir;

	private static ModConfig config() {
		ModConfig config = new ModConfig();
		config.categories.add(new CategoryConfig("Conteneurs", 128, "#55FF55",
				new ArrayList<>(List.of("minecraft:chest", "minecraft:barrel", "// commentaire"))));
		config.categories.add(new CategoryConfig("Cadres", 64, "#FF55FF", new ArrayList<>(List.of("minecraft:item_frame"))));
		return config;
	}

	private static CountResult result() {
		CountResult result = new CountResult(new ChunkPos(3, -7));
		result.categoryTotals.put("Conteneurs", 4);
		result.perIdCounts.put("minecraft:chest", 4);
		result.perIdCounts.put("minecraft:barrel", 0);
		return result;
	}

	@Test
	@SuppressWarnings("unchecked")
	void contenuDeLExport() {
		Map<String, Object> json = ChunkExporter.buildJson(result(), config(), NOW);
		assertEquals(3, json.get("chunkX"));
		assertEquals(-7, json.get("chunkZ"));

		List<Map<String, Object>> categories = (List<Map<String, Object>>) json.get("categories");
		assertEquals(2, categories.size());

		Map<String, Object> conteneurs = categories.get(0);
		assertEquals(4, conteneurs.get("total"));
		assertEquals(128, conteneurs.get("limit"));
		assertEquals(Map.of("minecraft:chest", 4), conteneurs.get("blocks"), "Les blocs à 0 ne sont pas listés");

		assertEquals(0, categories.get(1).get("total"), "Catégorie vide : total à 0");
		assertFalse(json.containsKey("others"), "Pas de partie « others » quand elle est vide");
	}

	@Test
	void partieOthersQuandTousLesBlocsSontAffiches() {
		CountResult result = result();
		result.otherCounts.put("minecraft:stone", 1200);
		Map<String, Object> json = ChunkExporter.buildJson(result, config(), NOW);
		assertEquals(Map.of("minecraft:stone", 1200), json.get("others"));
	}

	@Test
	void fichierEcritEtRelisible() throws IOException {
		Path file = ChunkExporter.export(result(), config(), dir.resolve("exports"), NOW);
		assertEquals("chunk_3_-7_2026-11-05_14-30-07.json", file.getFileName().toString());
		assertTrue(Files.exists(file));

		JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
		assertEquals(3, json.get("chunkX").getAsInt());
		assertEquals(4, json.getAsJsonArray("categories").get(0).getAsJsonObject().get("total").getAsInt());
	}
}
