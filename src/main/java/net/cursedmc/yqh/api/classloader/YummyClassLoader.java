package net.cursedmc.yqh.api.classloader;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.cursedmc.yqh.api.mixin.Mixout;
import org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader;
import org.quiltmc.loader.impl.util.NewUrlUtil;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.Objects;

/**
 * Loads the classes loaded by Knot for transformation.
 */
@SuppressWarnings("RedundantThrows")
public class YummyClassLoader extends SecureClassLoader {
	public static final ClassLoader APP_LOADER = YummyClassLoader.class.getClassLoader();
	public static final YummyClassLoader INSTANCE = new YummyClassLoader();
	public static final Object2ReferenceMap<String, Class<?>> CLASSES = new Object2ReferenceOpenHashMap<>();
	
	public Class<?> defineClass(String name, byte[] b, CodeSource cs) {
		return this.defineClass(name, b, 0, b.length, cs);
	}
	
	@Override
	protected PermissionCollection getPermissions(CodeSource codesource) {
		PermissionCollection perms = super.getPermissions(codesource);
		perms.add(new AllPermission());
		return perms;
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		synchronized (this.getClassLoadingLock(name)) {
			Class<?> klass = CLASSES.get(name);
			
			if (klass == null) {
				klass = this.findLoadedClass(name);
				
				if (klass == null) {
					if (!name.startsWith("java.")) {
						String filename = name.replace('.', '/').concat(".class");
						URL classUrl = Objects.requireNonNull(this.getResource(filename));
						byte[] classBytes = classUrl.openStream().readAllBytes();
						URL codeSourceUrl = NewUrlUtil.getSource(filename, classUrl);
						System.out.println(codeSourceUrl);
						System.out.println(classUrl);
						Certificate[] certificates = null;
						try {
							if (!Files.isDirectory(NewUrlUtil.asPath(codeSourceUrl))) {
								URLConnection connection = new URL("jar:" + codeSourceUrl.getFile() + "!/").openConnection();
								if (connection instanceof JarURLConnection jarConnection) {
									certificates = jarConnection.getCertificates();
								}
							}
						} catch (Exception ignored) {}
						
						Mixout.RawTransformEvent.preLoader(name, classBytes);
						
						klass = this.defineClass(name, classBytes, new CodeSource(codeSourceUrl, certificates));
						
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
					}
					
					if (klass == null) {
						klass = APP_LOADER.loadClass(name);
					}
				}
			}
			
			return klass;
		}
	}
	
	static {
		System.out.println("Loaded YummyClassLoader with parent " + INSTANCE.getParent());
		
		CLASSES.put("net.cursedmc.yqh.api.classloader.YummyClassLoader", YummyClassLoader.class);
		CLASSES.put("org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader", UnsafeKnotClassLoader.class);
	}
}
