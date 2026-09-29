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
		if (!Files.exists(CONFIG_PATH)) {
			ModConfig defaults = ConfigDefaults.create();
			save(defaults);
			return defaults;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
			if (loaded == null) {
				throw new IOException("Fichier de config vide ou invalide");
			}
			if (loaded.categories == null) {
				loaded.categories = ConfigDefaults.create().categories;
			}
			if (loaded.hud == null) {
				loaded.hud = new HudConfig();
			}
			return loaded;
		} catch (Exception e) {
			ChunkLimitCounterClient.LOGGER.error("Impossible de lire {}, utilisation de la config par defaut", CONFIG_PATH, e);
			return ConfigDefaults.create();
		}
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
