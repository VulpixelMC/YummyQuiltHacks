package net.cursedmc.yqh.impl.relaunch;

import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelaunchUtils {
	public static Process startProcess(boolean sendPid, boolean relaunch, @Nullable List<String> yqhMods) {
		final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		
		// get JVM args
		final List<String> args = new ArrayList<>(List.copyOf(bean.getInputArguments()));
		var side = new Object() {
			boolean defined = false;
		};
		args.forEach(action -> {
			if (action.contains("-Dloader.side")) {
				side.defined = true;
			}
		});
		if (!side.defined) args.add(0, "-Dloader.side=" + MinecraftQuiltLoader.getEnvironmentType().name().toLowerCase());
		if (yqhMods != null) args.add(0, "-Dyqh.mods=" + yqhMods.stream().reduce((s, s2) -> s + ',' + s2).orElse(""));
		if (sendPid) args.add(0, "-Dyqh.relauncher.pid=" + bean.getPid());
		args.remove("-Dyqh.relaunch=false");
		args.add(0, "-Dyqh.relaunch=" + relaunch);
		
		// get normal args
		final List<String> userArgs = new ArrayList<>(Arrays.stream(System.getProperty("sun.java.command").split(" ")).toList());
		if (userArgs.get(0).equals("net.fabricmc.devlaunchinjector.Main")) userArgs.remove(0);
		
		// get JVM executable
		final String jvmExec = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		
		// start a new JVM w/ YummyLauncher
		final List<String> cmd = new ArrayList<>(List.copyOf(args));
		cmd.add("-cp");
		cmd.add(System.getProperty("java.class.path"));
		cmd.add("net.cursedmc.yqh.impl.relaunch.YummyLauncher");
		cmd.addAll(userArgs);
		cmd.add(0, jvmExec);
		
		System.out.println(cmd);
		
		return new ProcessBuilder(cmd)
				.inheritIO()
				.directory(new File(System.getProperty("user.dir")))
				.start();
	}
	
	public static Process startProcess() {
		return startProcess(false, true, null);
	}
}
