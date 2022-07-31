package org.quiltmc.loader.impl.launch.knot;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.util.LoaderUtil;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;

/**
 * makes stuff in the app ClassLoader available to anything loaded by KnotClassLoader
 */
//https://github.com/Devan-Kerman/GrossFabricHacks/blob/ae137cd46b262c0ef2ed6f982d1bbbeca0a6c4da/src/main/java/net/fabricmc/loader/launch/knot/UnsafeKnotClassLoader.java
@SuppressWarnings("RedundantThrows")
public class UnsafeKnotClassLoader extends KnotClassLoader {
	public static final ClassLoader appLoader = UnsafeKnotClassLoader.class.getClassLoader();
	public static final ClassLoader knotLoader = Thread.currentThread().getContextClassLoader();
	public static final URLClassLoader parent = (URLClassLoader) knotLoader.getParent();
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/UnsafeKnotClassLoader");
	public static final Object2ReferenceMap<String, Class<?>> classes = new Object2ReferenceOpenHashMap<>();
	public static final KnotClassDelegate delegate = ((KnotClassLoader) knotLoader).getDelegate();
	
	UnsafeKnotClassLoader(final boolean isDevelopment, final EnvType envType, final GameProvider provider) {
		super(isDevelopment, envType, provider);
	}
	
	@Override
	public Class<?> findClass(final String name) {
		return this.loadClass(name, false);
	}
	
	@Override
	public PermissionCollection getPermissions(final CodeSource codesource) {
		return super.getPermissions(codesource);
	}
	
	@Override
	public URL findResource(final String moduleName, final String name) throws IOException {
		return super.findResource(moduleName, name);
	}
	
	@Override
	public Package[] getPackages() {
		return super.getPackages();
	}
	
	@Override
	public String findLibrary(@SuppressWarnings("SpellCheckingInspection") final String libname) {
		return super.findLibrary(libname);
	}
	
	@Override
	public boolean isClassLoaded(final String name) {
		synchronized (super.getClassLoadingLock(name)) {
			return super.findLoadedClass(name) != null || classes.containsKey(name);
		}
	}
	
	public Class<?> loadClass(final String name, final boolean resolve, final boolean allowFromParent) {
		synchronized (this.getClassLoadingLock(name)) {
			Class<?> klass = classes.get(name);
			
			if (klass == null) {
				klass = this.findLoadedClass(name);
				
				if (klass == null) {
					if (!name.startsWith("java.")) {
						final byte[] input = delegate.getPostMixinClassByteArray(name, allowFromParent);
						
						if (input != null) {
							final KnotClassDelegate.Metadata metadata = delegate.getMetadata(name, parent.getResource(LoaderUtil.getClassFileName(name)));
							
							final int pkgDelimiterPos = name.lastIndexOf('.');
							
							if (pkgDelimiterPos > 0) {
								final String pkgString = name.substring(0, pkgDelimiterPos);
								
								if (this.getPackage(pkgString) == null) {
									this.definePackage(pkgString, null, null, null, null, null, null, null);
								}
							}
							
							klass = super.defineClass(name, input, 0, input.length, metadata.codeSource);
						} else {
							klass = appLoader.loadClass(name);
						}
					} else {
						klass = appLoader.loadClass(name);
					}
				}
			}
			
			classes.put(name, klass);
			
			if (resolve) {
				this.resolveClass(klass);
			}
			
			return klass;
		}
	}
	
	@Override
	public Class<?> loadClass(final String name, final boolean resolve) {
		return this.loadClass(name, resolve, false);
	}
	
	static {
		LOGGER.info("Loaded UnsafeKnotClassLoader");
		
		classes.put("org.quiltmc.loader.impl.launch.knot.KnotClassLoader", KnotClassLoader.class);
		classes.put("org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", UnsafeKnotClassLoader.class);
		
		classes.put("net.devtech.grossfabrichacks.unsafe.UnsafeUtil", UnsafeUtil.class);
		classes.put("net.devtech.grossfabrichacks.unsafe.UnsafeUtil$FirstInt", UnsafeUtil.FirstInt.class);
		
		final String[] manualLoad = {
				"net.cursedmc.yqh.api.instrumentation.Music",
				"net.cursedmc.yqh.api.instrumentation.Music$1",
				"net.cursedmc.yqh.api.mixin.Mixout",
				"net.cursedmc.yqh.api.mixin.Mixout$TransformEvent",
				"ca.rttv.ASMFormatParser",
				"net.cursedmc.yqh.api.entrypoints.PreMixin",
				"org.spongepowered.asm.mixin.transformer.HackedMixinProcessor",
		};
		
		for (final String name : manualLoad) {
			classes.put(name, UnsafeUtil.findAndDefineClass(name, appLoader));
		}
		
		LOGGER.info("Loaded classes with app loader");
	}
}
