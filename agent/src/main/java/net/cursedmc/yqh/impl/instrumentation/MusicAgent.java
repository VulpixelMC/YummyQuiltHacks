package net.cursedmc.yqh.impl.instrumentation;

import org.jetbrains.annotations.ApiStatus;

import java.lang.instrument.Instrumentation;

@ApiStatus.Internal
public class MusicAgent {
	public static Instrumentation INST = null;
	
	public static void agentmain(final String agentArgs, final Instrumentation inst) {
		INST = inst;
	}
	
	static {
		System.out.println("Agent Attached");
	}
}
