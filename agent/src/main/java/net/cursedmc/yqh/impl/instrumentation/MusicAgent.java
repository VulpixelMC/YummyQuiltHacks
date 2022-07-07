package net.cursedmc.yqh.impl.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MusicAgent {
	public static Instrumentation INST = null;
	
	public static void agentmain(String agentArgs, Instrumentation inst) {
		INST = inst;
	}
	
	static {
		System.out.println("Agent Attached");
	}
}
