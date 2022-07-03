package net.cursedmc.yqh.test;

import net.cursedmc.yqh.api.entrypoints.PrePreLaunch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YummyQuiltHacksPrePreLaunch implements PrePreLaunch {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/PrePreLaunch");
	
	@Override
	public void onPrePreLaunch() {
		LOGGER.info("pre_pre_launch test");
	}
}
