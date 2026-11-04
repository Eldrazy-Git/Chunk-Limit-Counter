package com.eldrazy.chunklimitcounter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import com.eldrazy.chunklimitcounter.MinecraftBootstrap;

/** Protège la liste des limites contre les fautes de frappe et les blocs renommés par une mise à jour du jeu. */
class ConfigDefaultsTest {
	@BeforeAll
	static void bootstrap() {
		MinecraftBootstrap.init();
	}

	@Test
	void lesQuatreCategoriesEtLeursLimites() {
		ModConfig config = ConfigDefaults.create();
		List<String> noms = config.categories.stream().map(c -> c.name).toList();
		assertEquals(List.of("Conteneurs", "Redstone", "Blocs interactifs", "Cadres"), noms);
		assertEquals(List.of(128, 256, 512, 64), config.categories.stream().map(c -> c.limit).toList());
	}

	@Test
	void chaqueIdentifiantExisteDansLeJeu() {
		List<String> inconnus = new ArrayList<>();
		for (CategoryConfig category : ConfigDefaults.create().categories) {
			for (String id : category.ids) {
				Identifier identifier = Identifier.tryParse(id);
				boolean existe = identifier != null
						&& (BuiltInRegistries.BLOCK.containsKey(identifier) || BuiltInRegistries.ENTITY_TYPE.containsKey(identifier));
				if (!existe) {
					inconnus.add(category.name + " : " + id);
				}
			}
		}
		assertTrue(inconnus.isEmpty(), "Identifiants absents du jeu : " + inconnus);
	}

	@Test
	void aucunDoublonDansUneCategorie() {
		for (CategoryConfig category : ConfigDefaults.create().categories) {
			Set<String> vus = new HashSet<>();
			List<String> doublons = category.ids.stream().filter(id -> !vus.add(id)).toList();
			assertTrue(doublons.isEmpty(), category.name + " contient des doublons : " + doublons);
		}
	}

	@Test
	void couleursAuFormatHexadecimal() {
		for (CategoryConfig category : ConfigDefaults.create().categories) {
			assertTrue(category.color.matches("#[0-9A-Fa-f]{6}"), category.name + " : " + category.color);
		}
	}
}
