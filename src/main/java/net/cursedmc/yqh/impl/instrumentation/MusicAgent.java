package net.cursedmc.yqh.impl.instrumentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class MusicAgent {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/MusicAgent");

	public static Instrumentation INST = null;

	public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
		INST = inst;
	}

	static {
		LOGGER.info("Agent attached");
	}
}
