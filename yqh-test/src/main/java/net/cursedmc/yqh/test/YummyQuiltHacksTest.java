package net.cursedmc.yqh.test;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.impl.game.minecraft.Hooks;

public class YummyQuiltHacksTest implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Test");
	
	@Override
	public void onInitialize() {
		LOGGER.info("hi");
		
		System.out.println(Hooks.QUILT);
	}
}
