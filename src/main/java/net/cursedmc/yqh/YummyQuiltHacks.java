package net.cursedmc.yqh;

import com.enderzombi102.enderlib.RuntimeUtil;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.ModContainerImpl;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.launch.knot.MixinServiceKnot;
import org.quiltmc.loader.impl.metadata.qmj.AdapterLoadableClassEntry;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.util.Collection;

import static com.enderzombi102.enderlib.reflection.Getters.getStatic;

public class YummyQuiltHacks implements LanguageAdapter {
	public static final ClassLoader UNSAFE_LOADER;

	public static boolean isMixinLoaded = false;

	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);

	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");

	static {
		ClassLoader appLoader = Knot.class.getClassLoader();

		RuntimeUtil.attachAgent(
			RuntimeUtil.findJar(
				YummyQuiltHacks.class,
				"yqh",
				QuiltLoader.getModContainer( "yqh" ).orElseThrow().metadata().version().raw()
			)
		);

		var transformer = getStatic( MixinServiceKnot.class, "transformer", IMixinTransformer.class );

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
