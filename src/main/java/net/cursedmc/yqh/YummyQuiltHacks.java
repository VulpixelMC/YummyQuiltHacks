package net.cursedmc.yqh;

import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.cursedmc.yqh.api.instrumentation.Music;
import net.cursedmc.yqh.api.mixin.Mixout;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.launch.knot.Knot;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.jar.JarFile;

public class YummyQuiltHacks implements LanguageAdapter {
	public static final ClassLoader UNSAFE_LOADER;
	
	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);
	
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");
	
	static {
		if (Boolean.parseBoolean(System.getProperty("yqh.relaunch", "false"))) {
			final ClassLoader appLoader = Knot.class.getClassLoader();
			final ClassLoader knotLoader = YummyQuiltHacks.class.getClassLoader();
			
			String jarPath = Objects.requireNonNull(YummyQuiltHacks.class.getClassLoader().getResource("yummy_agent.jar")).getPath();
			final String vmPid = String.valueOf(ManagementFactory.getRuntimeMXBean().getPid());
			
			if (jarPath.startsWith("file:")) {
				// sanitize path
				jarPath = jarPath.replaceAll("file:|!/yummy_agent\\.jar", "");
				jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
				
				
				// find yummy_agent.jar inside jar and make a temp jar of it
				final JarFile jar = new JarFile(FileUtils.getFile(jarPath));
				final byte[] jarBytes = jar.getInputStream(jar.getJarEntry("yummy_agent.jar")).readAllBytes();
				jar.close();
				final File tempJar = File.createTempFile("tmp_", null);
				FileUtils.writeByteArrayToFile(tempJar, jarBytes);
				
				ByteBuddyAgent.attach(FileUtils.getFile(tempJar.getAbsolutePath()), vmPid);
			} else {
				if (SystemUtils.IS_OS_WINDOWS) {
					jarPath = jarPath.replaceFirst("/", ""); // windows bad
				}
				
				ByteBuddyAgent.attach(FileUtils.getFile(jarPath), vmPid);
			}
			
			final String[] manualLoad = {
					"net.gudenau.lib.unsafe.Unsafe",
			};
			
			for (final String name : manualLoad) {
				//noinspection ConstantConditions
				if (name.equals("net.gudenau.lib.unsafe.Unsafe") && QuiltLoader.isDevelopmentEnvironment()) {
					continue;
				}
				
				final byte[] classBytes = Classes.classFile(knotLoader, name);
				
				if (classBytes == null) {
					LOGGER.warn("Could not find class bytes for class " + name + " in loader " + knotLoader);
					continue;
				}
				
				ClassDefiner.make()
						.name(name)
						.classFile(classBytes)
						.loader(appLoader)
						.protectionDomain(YummyQuiltHacks.class.getProtectionDomain())
						.define();
			}
			
			UNSAFE_LOADER = UnsafeUtil.defineAndInitializeAndUnsafeCast(knotLoader, "org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", appLoader);
			
			UnsafeUtil.initializeClass(Music.class);
			UnsafeUtil.initializeClass(Mixout.class);
			
			LOGGER.fatal("Quilt has been successfully pwned >:3");
		} else {
			UNSAFE_LOADER = null;
		}
	}
}
