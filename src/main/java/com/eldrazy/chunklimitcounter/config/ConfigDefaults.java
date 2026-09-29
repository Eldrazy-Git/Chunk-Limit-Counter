package com.eldrazy.chunklimitcounter.config;

import java.util.ArrayList;
import java.util.List;

/** Builds the factory-default configuration, pre-filled with the server's known chunk limits. */
public final class ConfigDefaults {
	private static final String[] WOOD_TYPES = {
			"oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
			"mangrove", "cherry", "bamboo", "crimson", "warped", "pale_oak"
	};

	private static final String[] DYE_COLORS = {
			"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
			"light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
	};

	private ConfigDefaults() {
	}

	public static ModConfig create() {
		ModConfig config = new ModConfig();
		config.categories.add(containers());
		config.categories.add(redstone());
		config.categories.add(interactive());
		config.categories.add(frames());
		return config;
	}

	private static CategoryConfig containers() {
		List<String> ids = new ArrayList<>(List.of(
				"minecraft:chest",
				"minecraft:trapped_chest",
				"minecraft:barrel",
				"minecraft:shulker_box"
		));
		for (String color : DYE_COLORS) {
			ids.add("minecraft:" + color + "_shulker_box");
		}
		return new CategoryConfig("Conteneurs", 128, "#55FF55", ids);
	}

	private static CategoryConfig redstone() {
		List<String> ids = new ArrayList<>(List.of(
				"minecraft:redstone_wire",
				"minecraft:redstone_block",
				"minecraft:repeater",
				"minecraft:comparator",
				"minecraft:observer",
				"minecraft:target",
				"minecraft:redstone_torch",
				"minecraft:redstone_wall_torch",
				"minecraft:lever",
				"minecraft:daylight_detector",
				"minecraft:tripwire_hook",
				"minecraft:tripwire",
				"minecraft:lightning_rod",
				"minecraft:sculk_sensor",
				"minecraft:calibrated_sculk_sensor",
				"minecraft:sculk_shrieker",
				"minecraft:hopper",
				"minecraft:dispenser",
				"minecraft:dropper",
				"minecraft:piston",
				"minecraft:sticky_piston",
				"minecraft:crafter",
				"minecraft:powered_rail",
				"minecraft:detector_rail",
				"minecraft:activator_rail",
				"minecraft:redstone_lamp",
				"minecraft:note_block",
				"minecraft:tnt",
				"minecraft:iron_door",
				"minecraft:iron_trapdoor"
		));
		return new CategoryConfig("Modules de redstone", 256, "#FFAA00", ids);
	}

	private static CategoryConfig interactive() {
		List<String> ids = new ArrayList<>(List.of(
				"minecraft:ender_chest",
				"minecraft:furnace",
				"minecraft:blast_furnace",
				"minecraft:smoker",
				"minecraft:brewing_stand",
				"minecraft:enchanting_table",
				"minecraft:beacon",
				"minecraft:lectern",
				"minecraft:jukebox",
				"minecraft:chiseled_bookshelf",
				"minecraft:decorated_pot",
				"minecraft:bell",
				"minecraft:conduit",
				"minecraft:campfire",
				"minecraft:soul_campfire",
				"minecraft:beehive",
				"minecraft:bee_nest",
				"minecraft:sculk_catalyst",
				"minecraft:suspicious_sand",
				"minecraft:suspicious_gravel",
				"minecraft:spawner",
				"minecraft:trial_spawner",
				"minecraft:vault",
				"minecraft:comparator",
				// Verifie dans le registre du jeu 26.2 (javap sur Blocks.class) : bloc "etagere",
				// une variante par essence de bois, ajoute le 2026-09-29.
				"minecraft:creaking_heart"
		));
		for (String wood : WOOD_TYPES) {
			ids.add("minecraft:" + wood + "_sign");
			ids.add("minecraft:" + wood + "_wall_sign");
			ids.add("minecraft:" + wood + "_hanging_sign");
			ids.add("minecraft:" + wood + "_wall_hanging_sign");
			ids.add("minecraft:" + wood + "_shelf");
		}
		for (String color : DYE_COLORS) {
			ids.add("minecraft:" + color + "_banner");
			ids.add("minecraft:" + color + "_wall_banner");
			ids.add("minecraft:" + color + "_bed");
		}
		ids.add("minecraft:skeleton_skull");
		ids.add("minecraft:skeleton_wall_skull");
		ids.add("minecraft:wither_skeleton_skull");
		ids.add("minecraft:wither_skeleton_wall_skull");
		ids.add("minecraft:zombie_head");
		ids.add("minecraft:zombie_wall_head");
		ids.add("minecraft:creeper_head");
		ids.add("minecraft:creeper_wall_head");
		ids.add("minecraft:dragon_head");
		ids.add("minecraft:dragon_wall_head");
		ids.add("minecraft:piglin_head");
		ids.add("minecraft:piglin_wall_head");
		ids.add("minecraft:player_head");
		ids.add("minecraft:player_wall_head");
		// Verifie dans le registre du jeu 26.2 : statue de golem de cuivre, WeatheringCopperCollection
		// de 8 blocs (4 etats d'oxydation x cire), suit exactement le schema des autres blocs en cuivre.
		for (String prefix : new String[] {"", "exposed_", "weathered_", "oxidized_"}) {
			ids.add("minecraft:" + prefix + "copper_golem_statue");
			ids.add("minecraft:waxed_" + prefix + "copper_golem_statue");
		}
		return new CategoryConfig("Blocs interactifs", 512, "#55FFFF", ids);
	}

	private static CategoryConfig frames() {
		List<String> ids = new ArrayList<>(List.of(
				"minecraft:item_frame",
				"minecraft:glow_item_frame"
		));
		return new CategoryConfig("Cadres", 64, "#FF55FF", ids);
	}
}
