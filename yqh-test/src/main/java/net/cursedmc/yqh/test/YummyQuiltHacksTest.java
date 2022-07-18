package net.cursedmc.yqh.test;

import net.minecraft.client.gui.screen.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;

public class YummyQuiltHacksTest {
	public static final Class<Screen> screen = Screen.class;

	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Test");

	public void onInitialize(ModContainer mod) {
		LOGGER.info("hi");
	}
}
