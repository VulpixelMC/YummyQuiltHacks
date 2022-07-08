package org.quiltmc.loader.impl.launch.knot;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.cursedmc.yqh.YummyQuiltHacks;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.util.LoaderUtil;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Enumeration;

/**
 * makes stuff in the app ClassLoader available to anything loaded by KnotClassLoader
 */
//https://github.com/Devan-Kerman/GrossFabricHacks/blob/ae137cd46b262c0ef2ed6f982d1bbbeca0a6c4da/src/main/java/net/fabricmc/loader/launch/knot/UnsafeKnotClassLoader.java
public class UnsafeKnotClassLoader extends KnotClassLoader {
	public static final ClassLoader appLoader = UnsafeKnotClassLoader.class.getClassLoader();
	public static final ClassLoader knotLoader = Thread.currentThread().getContextClassLoader();
	public static final URLClassLoader urlLoader = (URLClassLoader) knotLoader.getParent();
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/UnsafeKnotClassLoader");
	public static final Object2ReferenceMap<String, Class<?>> classes = new Object2ReferenceOpenHashMap<>();
	
	UnsafeKnotClassLoader(boolean isDevelopment, EnvType envType, GameProvider provider) {
		super(isDevelopment, envType, provider);
	}
	
	public Class<?> defineClass(String name, byte[] bytes) {
		Class<?> klass = UnsafeUtil.defineClass(name, bytes, null, null);
		
		classes.put(name, klass);
		
		return klass;
	}
	
	@Override
	public Class<?> defineClassFwd(String name, byte[] b, int off, int len, CodeSource cs) {
		Class<?> klass = UnsafeUtil.defineClass(name, b, off, len, knotLoader, null);
		
		classes.put(name, klass);
		
		return klass;
	}
	
	@Override
	public boolean isClassLoaded(String name) {
		synchronized (this.getClassLoadingLock(name)) {
			return this.findLoadedClass(name) != null || classes.containsKey(name);
		}
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) {
		synchronized (this.getClassLoadingLock(name)) {
			Class<?> klass = classes.get(name);
			
			if (klass == null) {
				klass = super.findLoadedClass(name);
				
				if (klass == null) {
					if (!name.startsWith("java.")) {
						byte[] input = getDelegate().getPostMixinClassByteArray(name, false);
						
						if (input != null) {
							KnotClassDelegate.Metadata metadata = getDelegate().getMetadata(name, this.getResource(LoaderUtil.getClassFileName(name)));
							
							int pkgDelimiterPos = name.lastIndexOf('.');
							
							if (pkgDelimiterPos > 0) {
								String pkgString = name.substring(0, pkgDelimiterPos);
								
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
	
	static {
		LOGGER.info("Loaded UnsafeKnotClassLoader");
		
		classes.put("org.quiltmc.loader.impl.launch.knot.KnotClassLoader", KnotClassLoader.class);
		classes.put("org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", UnsafeKnotClassLoader.class);
		
		String[] manualLoad = {
				"net.cursedmc.yqh.api.instrumentation.Music",
				"net.cursedmc.yqh.api.instrumentation.Music$1",
				"net.cursedmc.yqh.api.mixin.Mixout",
				"net.cursedmc.yqh.api.mixin.Mixout$TransformEvent",
				"ca.rttv.ASMFormatParser",
				"net.cursedmc.yqh.api.entrypoints.PreMixin",
				"org.spongepowered.asm.mixin.transformer.HackedMixinProcessor",
		};
		
		for (String name : manualLoad) {
			byte[] classBytes = Classes.classFile(knotLoader, name);
			
			Class<?> klass = ClassDefiner.make()
					.name(name)
					.classFile(classBytes)
					.loader(appLoader)
					.protectionDomain(UnsafeKnotClassLoader.class.getProtectionDomain())
					.define();
			classes.put(name, klass);
		}
		
		String[] manualLoadKnot = {
				"net.cursedmc.yqh.api.util.MapUtils",
		};
		
		for (String name : manualLoadKnot) {
			Class<?> klass = knotLoader.loadClass(name);
			classes.put(name, klass);
		}
		
		LOGGER.info("Loaded classes with app loader");
	}
}
