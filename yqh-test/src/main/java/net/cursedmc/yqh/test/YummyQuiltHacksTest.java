package net.cursedmc.yqh.test;

import net.cursedmc.yqh.entrypoints.PrePreLaunch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YummyQuiltHacksTest implements PrePreLaunch {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacksTest");
	
	@Override
	public void onPrePreLaunch() {
		LOGGER.info("quilt pwn test");
	}
}
