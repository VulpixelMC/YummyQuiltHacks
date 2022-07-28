package net.cursedmc.yqh;

import com.sun.tools.attach.VirtualMachine;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.cursedmc.yqh.api.entrypoints.PreMixin;
import net.cursedmc.yqh.api.mixin.Mixout;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.ModContainerImpl;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader;
import org.quiltmc.loader.impl.metadata.qmj.AdapterLoadableClassEntry;

import java.io.File;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YummyQuiltHacks implements LanguageAdapter {
	public static final ClassLoader UNSAFE_LOADER;
	
	public static boolean isMixinLoaded = false;
	
	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);
	
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");
	
	static {
		ClassLoader appLoader = Knot.class.getClassLoader();
		ClassLoader knotLoader = YummyQuiltHacks.class.getClassLoader();
		Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true");
		
		VirtualMachine vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));
		String jarPath = Objects.requireNonNull(YummyQuiltHacks.class.getClassLoader().getResource("yummy_agent.jar")).getPath();
		// file:/home/tehc/Projects/CursedMC/YummyQuiltHacks/yqh-test/.gradle/quilt-loom-cache/remapped_mods/loom_mappings_1_19_layered_hash_2066822153_v2/net/cursedmc/yqh/0.1.0/yqh-0.1.0.jar!/yummy_agent.jar
		// /home/tehc/Projects/CursedMC/YummyQuiltHacks/build/resources/main/yummy_agent.jar
		if (jarPath.startsWith("file:")) {
			// sanitize path
			jarPath = jarPath.replaceAll("file:|!/yummy_agent\\.jar", "");
			jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
			
			// find yummy_agent.jar inside jar and make a temp jar of it
			JarFile jar = new JarFile(FileUtils.getFile(jarPath));
			byte[] jarBytes = jar.getInputStream(jar.getJarEntry("yummy_agent.jar")).readAllBytes();
			jar.close();
			File tempJar = File.createTempFile("tmp_", null);
			FileUtils.writeByteArrayToFile(tempJar, jarBytes);
			
			vm.loadAgent(tempJar.getAbsolutePath());
		} else {
			vm.loadAgent(jarPath);
		}
		
		final String[] manualLoad = {
				"net.gudenau.lib.unsafe.Unsafe",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil$FirstInt",
		};
		
		for (String name : manualLoad) {
			if (name.equals("net.gudenau.lib.unsafe.Unsafe") && QuiltLoader.isDevelopmentEnvironment()) {
				continue;
			}
			
			InputStream classStream = knotLoader.getResourceAsStream(name.replace('.', '/').concat(".class"));
			
			if (classStream == null) {
				LOGGER.warn("Could not find class bytes for class " + name + " in loader " + knotLoader);
				continue;
			}
			
			byte[] classBytes = classStream.readAllBytes();
			classStream.close();
			
			UnsafeUtil.defineClass(name, classBytes, appLoader, YummyQuiltHacks.class.getProtectionDomain());
		}
		
		UNSAFE_LOADER = UnsafeUtil.defineAndInitializeAndUnsafeCast(knotLoader, "org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", knotLoader.getClass().getClassLoader());
		
		LOGGER.fatal("Quilt has been successfully pwned >:3");
		
		//noinspection unchecked
		for (ModContainerImpl mod : (Collection<ModContainerImpl>) (Collection<?>) QuiltLoader.getAllMods()) {
			if (mod.getInternalMeta().getEntrypoints().containsKey("yqh:pre_mixin")) {
				for (AdapterLoadableClassEntry entry : mod.getInternalMeta().getEntrypoints().get("yqh:pre_mixin")) {
					Class<?> preMixinClass = UnsafeUtil.findAndDefineAndInitializeClass(entry.getValue(), appLoader);
					Object preMixin = preMixinClass.getConstructor().newInstance();
					preMixin.getClass().getMethod("onPreMixin").invoke(preMixin);
				}
			}
		}
	}
}
