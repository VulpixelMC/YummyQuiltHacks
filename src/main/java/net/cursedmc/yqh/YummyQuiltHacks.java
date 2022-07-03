package net.cursedmc.yqh;

import com.sun.tools.attach.VirtualMachine;
import net.auoeke.reflect.Accessor;
import net.cursedmc.yqh.api.entrypoints.PreMixin;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.ModContainerImpl;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.metadata.qmj.AdapterLoadableClassEntry;

import java.util.Collection;
import java.util.Map;

public class YummyQuiltHacks implements LanguageAdapter {
	public static final ClassLoader UNSAFE_LOADER;
	
	public static boolean isMixinLoaded = false;
	
	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);
	
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");
	
	static {
		ClassLoader appLoader = Knot.class.getClassLoader();
		Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true");
		
		// todo: add dev-env support
		VirtualMachine vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));
		String jarPath = YummyQuiltHacks.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		vm.loadAgent(jarPath);
		
		final String[] classes = {
				"net.gudenau.lib.unsafe.Unsafe",
				"net.cursedmc.yqh.api.instrumentation.Music",
				"net.cursedmc.yqh.api.mixin.Mixout",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil$FirstInt",
		};
		
		for (String className : classes) {
			Class<?> klass = appLoader.loadClass(className);
			UnsafeUtil.initializeClass(klass);
		}
		
		LOGGER.info("Loaded classes with app loader");
		
		UNSAFE_LOADER = UnsafeUtil.defineAndInitializeAndUnsafeCast(YummyQuiltHacks.class.getClassLoader(), "org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", appLoader);
		
		LOGGER.fatal("Quilt has been successfully pwned >:3");
		
		//noinspection unchecked
		for (ModContainerImpl mod : (Collection<ModContainerImpl>) (Collection<?>) QuiltLoader.getAllMods()) {
			if (mod.getInternalMeta().getEntrypoints().containsKey("yqh:pre_mixin")) {
				for (AdapterLoadableClassEntry entry : mod.getInternalMeta().getEntrypoints().get("yqh:pre_mixin")) {
					Object preMixin = Class.forName(entry.getValue(), true, appLoader).getConstructor().newInstance();
					preMixin.getClass().getMethod("onPreMixin").invoke(preMixin);
				}
			}
		}
	}
}
