package com.eldrazy.chunklimitcounter.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.eldrazy.chunklimitcounter.config.HudConfig;

class HudLayoutTest {
	private static final int GUI_W = 480;
	private static final int GUI_H = 270;
	private static final int CONTENT_W = 120;
	private static final int CONTENT_H = 50;

	@Test
	void couleurSelonLeRemplissage() {
		assertEquals(HudLayout.COLOR_OK, HudLayout.statusColor(0, 128));
		assertEquals(HudLayout.COLOR_OK, HudLayout.statusColor(89, 128), "69,5 % : encore vert");
		assertEquals(HudLayout.COLOR_WARN, HudLayout.statusColor(90, 128), "70 % : jaune");
		assertEquals(HudLayout.COLOR_WARN, HudLayout.statusColor(128, 128), "pile à la limite : pas encore rouge");
		assertEquals(HudLayout.COLOR_CRITICAL, HudLayout.statusColor(129, 128), "au-delà de la limite : rouge");
	}

	@Test
	void depassementSeulementAuDelaDeLaLimite() {
		assertFalse(HudLayout.isExceeded(128, 128));
		assertTrue(HudLayout.isExceeded(129, 128));
	}

	@Test
	void limiteNulleNePlantePas() {
		assertEquals(HudLayout.COLOR_OK, HudLayout.statusColor(0, 0));
		assertEquals(HudLayout.COLOR_CRITICAL, HudLayout.statusColor(1, 0));
	}

	@ParameterizedTest
	@CsvSource({
			"10, 10, TOP_LEFT",
			"350, 10, TOP_RIGHT",
			"10, 210, BOTTOM_LEFT",
			"350, 210, BOTTOM_RIGHT"
	})
	void sAccrocheAuCoinLePlusProche(int x, int y, HudConfig.Anchor attendu) {
		HudConfig hud = new HudConfig();
		HudLayout.applyDraggedPosition(hud, GUI_W, GUI_H, CONTENT_W, CONTENT_H, x, y);
		assertEquals(attendu, hud.anchor);
	}

	@ParameterizedTest
	@CsvSource({"10, 10", "350, 10", "10, 210", "350, 210", "180, 110"})
	void allerRetourDeLaPosition(int x, int y) {
		HudConfig hud = new HudConfig();
		HudLayout.applyDraggedPosition(hud, GUI_W, GUI_H, CONTENT_W, CONTENT_H, x, y);
		assertEquals(x, HudLayout.originX(hud, GUI_W, CONTENT_W));
		assertEquals(y, HudLayout.originY(hud, GUI_H, CONTENT_H));
	}

	@ParameterizedTest
	@CsvSource({"-30, -30", "420, 250", "-30, 250", "420, -30"})
	void horsEcranLeDecalageRestePositif(int x, int y) {
		HudConfig hud = new HudConfig();
		HudLayout.applyDraggedPosition(hud, GUI_W, GUI_H, CONTENT_W, CONTENT_H, x, y);
		assertTrue(hud.offsetX >= 0, "offsetX = " + hud.offsetX);
		assertTrue(hud.offsetY >= 0, "offsetY = " + hud.offsetY);
	}
}
