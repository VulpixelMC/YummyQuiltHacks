package net.cursedmc.yqh.test;

import net.cursedmc.yqh.entrypoints.PreMixin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YummyQuiltHacksPreMixin implements PreMixin {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/PreMixin");
	
	@Override
	public void onPreMixin() {
		LOGGER.info("pre_mixin test");
	}
}
