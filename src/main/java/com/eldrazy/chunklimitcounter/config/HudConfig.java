package com.eldrazy.chunklimitcounter.config;

/**
 * Where the HUD is anchored on screen. Position is stored as an offset from the
 * chosen corner so the HUD stays put relative to that corner across resolutions.
 */
public class HudConfig {
	public enum Anchor {
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

	public boolean visible = true;
	public Anchor anchor = Anchor.TOP_LEFT;
	public int offsetX = 6;
	public int offsetY = 6;
	public float scale = 1.0f;
	/** Background alpha, 0 (invisible) to 255 (opaque). */
	public int backgroundAlpha = 144;
}
