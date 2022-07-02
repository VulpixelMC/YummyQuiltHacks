package net.cursedmc.yqh;

import net.cursedmc.yqh.entrypoints.PreMixin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.ModContainerImpl;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader;
import org.quiltmc.loader.impl.metadata.qmj.AdapterLoadableClassEntry;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class YummyQuiltHacks implements LanguageAdapter {
	public static boolean isMixinLoaded = false;
	
	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);
	
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");
	
	static {
		LOGGER.fatal("Quilt has been successfully pwned >:3");
		
		EntrypointUtils.invoke("yqh:pre_mixin", PreMixin.class, PreMixin::onPreMixin);
		
		for (ModContainerImpl mod : (Collection<ModContainerImpl>) (Collection<?>) QuiltLoader.getAllMods()) {
			if (mod.getInternalMeta().getEntrypoints().containsKey("yqh:pre_mixin")) {
				for (AdapterLoadableClassEntry entry : mod.getInternalMeta().getEntrypoints().get("yqh:pre_mixin")) {
					try {
						PreMixin preMixin = (PreMixin) Class.forName(entry.getValue()).getConstructor().newInstance();
						preMixin.onPreMixin();
					} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
					         IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
}
