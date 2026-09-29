package com.eldrazy.chunklimitcounter.counting;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import com.eldrazy.chunklimitcounter.config.CategoryConfig;
import com.eldrazy.chunklimitcounter.config.ModConfig;

/** Dumps the current chunk's counts to a standalone JSON file for sharing/record-keeping. */
public final class ChunkExporter {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

	private ChunkExporter() {
	}

	public static Path export(CountResult result, ModConfig config) throws IOException {
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("chunkX", result.chunkPos.x());
		root.put("chunkZ", result.chunkPos.z());
		root.put("exportedAt", LocalDateTime.now().toString());

		List<Map<String, Object>> categories = new ArrayList<>();
		for (CategoryConfig category : config.categories) {
			Map<String, Object> categoryJson = new LinkedHashMap<>();
			categoryJson.put("name", category.name);
			categoryJson.put("limit", category.limit);
			categoryJson.put("total", result.categoryTotal(category.name));

			Map<String, Integer> blocks = new LinkedHashMap<>();
			for (String rawId : category.ids) {
				String id = rawId == null ? null : rawId.trim();
				if (id == null || id.isEmpty() || id.startsWith("//") || id.startsWith("#")) {
					continue;
				}
				Integer count = result.perIdCounts.get(id);
				if (count != null && count > 0) {
					blocks.put(id, count);
				}
			}
			categoryJson.put("blocks", blocks);
			categories.add(categoryJson);
		}
		root.put("categories", categories);

		if (!result.otherCounts.isEmpty()) {
			root.put("others", result.otherCounts);
		}

		Path dir = FabricLoader.getInstance().getConfigDir().resolve("chunklimitcounter").resolve("exports");
		Files.createDirectories(dir);
		String fileName = "chunk_" + result.chunkPos.x() + "_" + result.chunkPos.z() + "_" + LocalDateTime.now().format(FILE_STAMP) + ".json";
		Path file = dir.resolve(fileName);

		try (Writer writer = Files.newBufferedWriter(file)) {
			GSON.toJson(root, writer);
		}

		return file;
	}
}
