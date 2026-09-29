package com.eldrazy.chunklimitcounter.config;

import java.util.ArrayList;
import java.util.List;

/**
 * One trackable category from the config file (e.g. "Conteneurs"): a limit per chunk,
 * a display color and the block/entity ids that count towards it.
 */
public class CategoryConfig {
	public String name = "Category";
	public int limit = 64;
	public String color = "#55FF55";
	public List<String> ids = new ArrayList<>();

	public CategoryConfig() {
	}

	public CategoryConfig(String name, int limit, String color, List<String> ids) {
		this.name = name;
		this.limit = limit;
		this.color = color;
		this.ids = ids;
	}
}
