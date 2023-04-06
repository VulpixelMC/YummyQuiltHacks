package net.cursedmc.yqh.api.classloader;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Invoker;
import net.cursedmc.yqh.YummyQuiltHacks;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.util.NewUrlUtil;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;

/**
 * Loads the classes loaded by Knot for transformation.
 * also replaces the app class loader used by KnotClassLoader
 */
@SuppressWarnings("RedundantThrows")
public class YummyClassLoader extends SecureClassLoader {
	public static final YummyClassLoader INSTANCE = new YummyClassLoader();
	
	private static final Object2ReferenceMap<String, Class<?>> CLASSES = new Object2ReferenceOpenHashMap<>();
	private static final ClassLoader APP_LOADER = YummyClassLoader.class.getClassLoader();
	private static final MethodHandle MIXOUT_PRE_LOADER;
	
	/**
	 * Returns the app class loader.
	 * @return {@link YummyClassLoader#APP_LOADER}
	 */
	public static ClassLoader getAppLoader() {
		return APP_LOADER;
	}
	
	/**
	 * Returns all loaded classes.
	 * @return {@link YummyClassLoader#CLASSES}
	 */
	public static Object2ReferenceMap<String, Class<?>> getClasses() {
		return CLASSES;
	}
	
	public Class<?> defineClass(String name, byte[] b, CodeSource cs) {
		return this.defineClass(name, b, 0, b.length, cs);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		synchronized (this.getClassLoadingLock(name)) {
			Class<?> klass = CLASSES.get(name);
			
			if (klass == null) {
				klass = this.findLoadedClass(name);
				
				if (klass == null) {
					String filename = name.replace('.', '/').concat(".class");
					URL classUrl = this.getResource(filename);
					if (classUrl == null) throw new ClassNotFoundException(name);
					
					if (!classUrl.getProtocol().equals("jrt")) { // make sure we don't load any Java Runtime classes
						byte[] classBytes = classUrl.openStream().readAllBytes();
						URL codeSourceUrl = NewUrlUtil.getSource(filename, classUrl);
						Certificate[] certificates = null;
						try {
							if (!Files.isDirectory(NewUrlUtil.asPath(codeSourceUrl))) {
								URLConnection connection = new URL("jar:" + codeSourceUrl + "!/").openConnection();
								if (connection instanceof JarURLConnection jarConnection) {
									certificates = jarConnection.getCertificates();
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						try {
							classBytes = (byte[]) MIXOUT_PRE_LOADER.invokeExact(name, classBytes);
						} catch (NullPointerException ignored) {} // because we load some stuff before
						
						int pkgDelimiterPos = name.lastIndexOf('.');
						
						if (pkgDelimiterPos > 0) {
							String pkgString = name.substring(0, pkgDelimiterPos);
							
							if (this.getDefinedPackage(pkgString) == null) {
								try {
									this.definePackage(pkgString, null, null, null, null, null, null, null);
								} catch (IllegalArgumentException e) { // presumably concurrent package definition
									if (this.getDefinedPackage(pkgString) == null) throw e; // still not defined?
								}
							}
						}
						
						klass = this.defineClass(name, classBytes, new CodeSource(codeSourceUrl, certificates));
					}
					
					if (klass == null) {
						klass = APP_LOADER.loadClass(name);
					}
				}
			}
			
			CLASSES.put(name, klass);
			
			return klass;
		}
	}
	
	static {
		System.out.println("Loaded YummyClassLoader with parent " + INSTANCE.getParent());
		
		CLASSES.put("net.cursedmc.yqh.api.classloader.YummyClassLoader", YummyClassLoader.class);
		
		final String[] manualAppLoad = {
				"net.gudenau.lib.unsafe.Unsafe",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil",
				"net.devtech.grossfabrichacks.unsafe.UnsafeUtil$FirstInt",
		};
		
		for (String name : manualAppLoad) {
			if (name.equals("net.gudenau.lib.unsafe.Unsafe") && QuiltLoader.isDevelopmentEnvironment()) {
				continue;
			}
			
			final byte[] classBytes = Classes.classFile(YummyClassLoader.INSTANCE, name);
			
			if (classBytes == null) {
				System.err.println("Could not find class bytes for class " + name + " in loader " + YummyClassLoader.INSTANCE);
				continue;
			}
			
			Class<?> klass = ClassDefiner.make()
					.name(name)
					.classFile(classBytes)
					.loader(APP_LOADER)
					.protectionDomain(YummyQuiltHacks.class.getProtectionDomain())
					.define();
			
			CLASSES.put(name, klass);
		}
		
		final String[] manualLoad = {
				"net.cursedmc.yqh.api.instrumentation.Music",
				"net.cursedmc.yqh.api.instrumentation.Music$1",
				"net.cursedmc.yqh.api.mixin.Mixout",
				"net.cursedmc.yqh.api.mixin.Mixout$TransformEvent",
				"net.cursedmc.yqh.api.mixin.Mixout$RawTransformEvent",
				"net.cursedmc.yqh.api.mixin.Mixout$RawTransformEvent$1",
				"ca.rttv.ASMFormatParser",
		};
		
		for (String name : manualLoad) {
			YummyClassLoader.INSTANCE.loadClass(name);
		}
		
		MIXOUT_PRE_LOADER = Invoker.findStatic(YummyClassLoader.INSTANCE.loadClass("net.cursedmc.yqh.api.mixin.Mixout$RawTransformEvent"), "preLoader", byte[].class, String.class, byte[].class);
	}
}
