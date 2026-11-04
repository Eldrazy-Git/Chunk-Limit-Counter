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
		Path dir = FabricLoader.getInstance().getConfigDir().resolve("chunklimitcounter").resolve("exports");
		return export(result, config, dir, LocalDateTime.now());
	}

	public static Path export(CountResult result, ModConfig config, Path dir, LocalDateTime now) throws IOException {
		Map<String, Object> root = buildJson(result, config, now);

		Files.createDirectories(dir);
		String fileName = "chunk_" + result.chunkPos.x() + "_" + result.chunkPos.z() + "_" + now.format(FILE_STAMP) + ".json";
		Path file = dir.resolve(fileName);

		try (Writer writer = Files.newBufferedWriter(file)) {
			GSON.toJson(root, writer);
		}

		return file;
	}

	/** The export's content, kept separate from file writing so it can be checked on its own. */
	public static Map<String, Object> buildJson(CountResult result, ModConfig config, LocalDateTime now) {
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("chunkX", result.chunkPos.x());
		root.put("chunkZ", result.chunkPos.z());
		root.put("exportedAt", now.toString());

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

		return root;
	}
}
