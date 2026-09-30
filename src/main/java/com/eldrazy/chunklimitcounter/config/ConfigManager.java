package com.eldrazy.chunklimitcounter.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import com.eldrazy.chunklimitcounter.ChunkLimitCounterClient;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
			.resolve("chunklimitcounter").resolve("config.json");

	private ConfigManager() {
	}

	public static ModConfig load() {
		ModConfig config;

		if (!Files.exists(CONFIG_PATH)) {
			config = new ModConfig();
		} else {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				config = GSON.fromJson(reader, ModConfig.class);
				if (config == null) {
					throw new IOException("Fichier de config vide ou invalide");
				}
				if (config.hud == null) {
					config.hud = new HudConfig();
				}
			} catch (Exception e) {
				ChunkLimitCounterClient.LOGGER.error("Impossible de lire {}, utilisation de la config par defaut", CONFIG_PATH, e);
				config = new ModConfig();
			}
		}

		// Category limits are hard-coded and never read from or written to config.json,
		// so players/server admins can't loosen them by editing the file.
		config.categories = ConfigDefaults.create().categories;

		// Re-save so a fresh file appears, and so any stale "categories" list left over
		// from an older version of the mod gets wiped from the file on disk too.
		save(config);

		return config;
	}

	public static void save(ModConfig config) {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			ChunkLimitCounterClient.LOGGER.error("Impossible d'ecrire {}", CONFIG_PATH, e);
		}
	}
}
