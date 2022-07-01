package net.cursedmc.yqh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;

public class YummyQuiltHacks implements LanguageAdapter {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");
	
	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);
	
	static {
		LOGGER.warn("Quilt has been successfully pwned >:3");
	}
}
