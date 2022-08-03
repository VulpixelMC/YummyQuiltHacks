package net.cursedmc.yqh.impl.relaunch;

import net.auoeke.reflect.Invoker;
import net.cursedmc.yqh.api.classloader.YummyClassLoader;

import java.lang.invoke.MethodHandle;

public class YummyLauncher {
	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	public static void main(String[] args) {
		if (Boolean.parseBoolean(System.getProperty("yqh.relaunch"))) { // if we're in the third process,
			MethodHandle main = Invoker.findStatic(Class.forName("net.cursedmc.yqh.impl.relaunch.YummyKnot"), "main", void.class, String[].class);
			main.invokeExact(args); // launch YummyKnot
			return;
		} // else,
		// start the third process
		try {
			Process process = RelaunchUtils.startProcess();
			ProcessHandle.of(Long.parseLong(System.getProperty("yqh.relauncher.pid"))).ifPresent(handle -> {
				while (true) {
					if (!handle.isAlive()) break;
					if (!process.isAlive()) break;
				}
				process.destroy();
				System.exit(process.exitValue());
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(-1);
	}
}
