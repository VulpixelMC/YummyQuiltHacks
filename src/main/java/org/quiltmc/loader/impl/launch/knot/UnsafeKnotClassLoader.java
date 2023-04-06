package org.quiltmc.loader.impl.launch.knot;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.util.LoaderUtil;

import java.net.URLClassLoader;

/**
 * makes stuff in the app ClassLoader available to anything loaded by KnotClassLoader
 * Do not use this! Use {@link net.cursedmc.yqh.api.classloader.UnsafeKnotClassLoader} instead!
 * @see net.cursedmc.yqh.api.classloader.UnsafeKnotClassLoader
 */
//https://github.com/Devan-Kerman/GrossFabricHacks/blob/ae137cd46b262c0ef2ed6f982d1bbbeca0a6c4da/src/main/java/net/fabricmc/loader/launch/knot/UnsafeKnotClassLoader.java
@SuppressWarnings("RedundantThrows")
@ApiStatus.Internal
@ApiStatus.NonExtendable
public class UnsafeKnotClassLoader extends KnotClassLoader {
	private static final ClassLoader YUMMY_LOADER = Knot.class.getClassLoader();
	private static final ClassLoader KNOT_LOADER = Thread.currentThread().getContextClassLoader();
	
	/**
	 * <b>Warning</b>: This isn't actually the app loader!
	 */
	@Deprecated(
			since = "0.2.0",
			forRemoval = true
	)
	public static final ClassLoader appLoader = YUMMY_LOADER;
	@Deprecated(
			since = "0.2.0",
			forRemoval = true
	)
	public static final ClassLoader knotLoader = KNOT_LOADER;
	
	private static final URLClassLoader PARENT = (URLClassLoader) KNOT_LOADER.getParent();
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/UnsafeKnotClassLoader");
	private static final Object2ReferenceMap<String, Class<?>> CLASSES = new Object2ReferenceOpenHashMap<>();
	private static final KnotClassDelegate DELEGATE = ((KnotClassLoader) KNOT_LOADER).getDelegate();
	
	protected UnsafeKnotClassLoader(boolean isDevelopment, EnvType envType, GameProvider provider) {
		super(isDevelopment, envType, provider);
	}
	
	@Override
	public boolean isClassLoaded(String name) {
		synchronized (super.getClassLoadingLock(name)) {
			return super.findLoadedClass(name) != null || CLASSES.containsKey(name);
		}
	}
	
	public Class<?> loadClass(String name, boolean resolve, boolean allowFromParent) throws ClassNotFoundException {
		synchronized (this.getClassLoadingLock(name)) {
			Class<?> klass = CLASSES.get(name);
			
			if (klass == null) {
				klass = this.findLoadedClass(name);
				
				if (klass == null) {
					if (!name.startsWith("java.")) {
						final byte[] input = DELEGATE.getPostMixinClassByteArray(name, allowFromParent);
						
						if (input != null) {
							final KnotClassDelegate.Metadata metadata = DELEGATE.getMetadata(name, PARENT.getResource(LoaderUtil.getClassFileName(name)));
							
							final int pkgDelimiterPos = name.lastIndexOf('.');
							
							if (pkgDelimiterPos > 0) {
								final String pkgString = name.substring(0, pkgDelimiterPos);
								
								if (this.getPackage(pkgString) == null) {
									this.definePackage(pkgString, null, null, null, null, null, null, null);
								}
							}
							
							klass = super.defineClass(name, input, 0, input.length, metadata.codeSource);
						} else {
							klass = YUMMY_LOADER.loadClass(name);
						}
					} else {
						klass = YUMMY_LOADER.loadClass(name);
					}
				}
			}
			
			CLASSES.put(name, klass);
			
			if (resolve) {
				this.resolveClass(klass);
			}
			
			return klass;
		}
	}
	
	@Override
	public Class<?> loadClass(String name, boolean resolve) {
		return this.loadClass(name, resolve, false);
	}
	
	static {
		LOGGER.info("Loaded UnsafeKnotClassLoader");
		
		ClassLoader knotLoader = Thread.currentThread().getContextClassLoader();
		
		UnsafeUtil.unsafeCast(knotLoader, UnsafeKnotClassLoader.class);
		
		CLASSES.put("org.quiltmc.loader.impl.launch.knot.KnotClassLoader", KnotClassLoader.class);
		CLASSES.put("org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", UnsafeKnotClassLoader.class);
		
		final String[] manualLoad = {
				"net.cursedmc.yqh.api.entrypoints.PreMixin",
				"org.spongepowered.asm.mixin.transformer.HackedMixinProcessor",
		};
		
		for (final String name : manualLoad) {
			CLASSES.put(name, UnsafeUtil.findAndDefineClass(name, YUMMY_LOADER));
		}
		
		LOGGER.info("Loaded classes with yummy loader");
	}
}
