package net.cursedmc.yqh.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class YummyQuiltHacksTest implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Test");
	
	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("hi");
	}
}
