package com.eldrazy.chunklimitcounter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigManagerTest {
	@TempDir
	Path dir;

	private Path file() {
		return dir.resolve("chunklimitcounter").resolve("config.json");
	}

	private void write(String content) throws IOException {
		Files.createDirectories(file().getParent());
		Files.writeString(file(), content);
	}

	private static void assertDefaults(ModConfig config) {
		ModConfig defaults = new ModConfig();
		assertEquals(defaults.safetyNetIntervalTicks, config.safetyNetIntervalTicks);
		assertEquals(defaults.showAllBlocksInDetailScreen, config.showAllBlocksInDetailScreen);
		assertNotNull(config.hud);
		assertEquals(4, config.categories.size());
	}

	@Test
	void fichierAbsentCreeLaConfigParDefaut() {
		ModConfig config = ConfigManager.load(file());
		assertDefaults(config);
		assertTrue(Files.exists(file()), "Le fichier aurait dû être créé");
	}

	@Test
	void fichierVide() throws IOException {
		write("");
		assertDefaults(ConfigManager.load(file()));
	}

	@Test
	void fichierCasse() throws IOException {
		write("{ \"safetyNetIntervalTicks\": 5, ");
		assertDefaults(ConfigManager.load(file()));
	}

	@Test
	void sansPartieHud() throws IOException {
		write("{ \"safetyNetIntervalTicks\": 5 }");
		ModConfig config = ConfigManager.load(file());
		assertEquals(5, config.safetyNetIntervalTicks);
		assertNotNull(config.hud);
		assertEquals(HudConfig.Anchor.TOP_LEFT, config.hud.anchor);
	}

	@Test
	void lesLimitesDuFichierSontIgnorees() throws IOException {
		write("{ \"categories\": [ { \"name\": \"Conteneurs\", \"limit\": 99999, \"ids\": [\"minecraft:chest\"] } ] }");
		ModConfig config = ConfigManager.load(file());
		assertEquals(128, config.categories.get(0).limit);
		assertEquals(4, config.categories.size());
		// Le fichier réécrit ne garde pas l'ancienne liste.
		assertFalse(Files.readString(file()).contains("categories"));
	}

	@Test
	void allerRetourDesReglages() {
		ModConfig config = ConfigManager.load(file());
		config.safetyNetIntervalTicks = 40;
		config.showAllBlocksInDetailScreen = true;
		config.hud.anchor = HudConfig.Anchor.BOTTOM_RIGHT;
		config.hud.offsetX = 12;
		config.hud.scale = 1.5f;
		config.hud.backgroundAlpha = 200;
		ConfigManager.save(config, file());

		ModConfig relu = ConfigManager.load(file());
		assertEquals(40, relu.safetyNetIntervalTicks);
		assertTrue(relu.showAllBlocksInDetailScreen);
		assertEquals(HudConfig.Anchor.BOTTOM_RIGHT, relu.hud.anchor);
		assertEquals(12, relu.hud.offsetX);
		assertEquals(1.5f, relu.hud.scale);
		assertEquals(200, relu.hud.backgroundAlpha);
	}
}
