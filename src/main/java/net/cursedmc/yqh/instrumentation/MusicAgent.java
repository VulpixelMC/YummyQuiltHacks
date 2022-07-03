package net.cursedmc.yqh.instrumentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.Instrumentation;

public class MusicAgent {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/MusicAgent");
	
	public static Instrumentation INST = null;
	
	public static void agentmain(String agentArgs, Instrumentation inst) {
		INST = inst;
	}
	
	static {
		LOGGER.info("Agent attached");
	}
}
