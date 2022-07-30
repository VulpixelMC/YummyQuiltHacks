package net.cursedmc.yqh.impl.instrumentation;

import java.lang.instrument.Instrumentation;

public class MusicAgent {
	public static Instrumentation INST = null;
	
	public static void agentmain(final String agentArgs, final Instrumentation inst) {
		INST = inst;
	}
	
	static {
		System.out.println("Agent Attached");
	}
}
