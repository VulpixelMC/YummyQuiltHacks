package net.cursedmc.yqh.test;

import net.minecraft.client.gui.screen.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class YummyQuiltHacksTest implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Test");
	private static final Class<Screen> screen = Screen.class;
	
	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("hi");
	}
}
