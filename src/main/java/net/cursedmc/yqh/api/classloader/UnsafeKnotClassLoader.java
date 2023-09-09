package net.cursedmc.yqh.api.classloader;

import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.api.EnvType;
import org.quiltmc.loader.impl.game.GameProvider;

import java.security.CodeSource;

/**
 * Allows access to the instance of {@link org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader} and {@link org.quiltmc.loader.impl.launch.knot.KnotClassLoader}.
 * @since 0.2.0
 */
@SuppressWarnings("RedundantThrows")
public final class UnsafeKnotClassLoader extends org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader {
	public static final UnsafeKnotClassLoader INSTANCE;
	
	private UnsafeKnotClassLoader(boolean isDevelopment, EnvType envType, GameProvider provider) {
		super(isDevelopment, envType, provider);
	}
	
	public Class<?> defineClass(String name, byte[] b, CodeSource cs) {
		return this.defineClass(name, b, 0, b.length, cs);
	}
	
	public Class<?> defineClass(String name, byte[] b) {
		return this.defineClass(name, b, 0, b.length);
	}
	
	@Override
	public boolean isClassLoaded(String name) {
		return super.isClassLoaded(name);
	}
	
	@Override
	public Class<?> loadClass(String name, boolean resolve, boolean allowFromParent) throws ClassNotFoundException {
		return super.loadClass(name, resolve, allowFromParent);
	}
	
	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return super.loadClass(name, resolve);
	}
	
	static {
		ClassLoader knotLoader = Thread.currentThread().getContextClassLoader();
		
		UnsafeUtil.initializeClass(org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader.class);
		
		INSTANCE = (UnsafeKnotClassLoader) knotLoader;
	}
}
