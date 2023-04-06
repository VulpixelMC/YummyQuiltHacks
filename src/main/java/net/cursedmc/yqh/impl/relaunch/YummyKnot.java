package net.cursedmc.yqh.impl.relaunch;

import com.jsoniter.JsonIterator;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Invoker;
import net.cursedmc.yqh.api.entrypoints.PreLoader;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.apache.commons.io.FileUtils;
import org.quiltmc.loader.impl.launch.knot.Knot;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * i'm so sorry<br>
 * ignore the class name<br>
 * if you don't know what i'm talking about, please don't ask
 */
public class YummyKnot {
	public static Map<String, YummyManifest> MANIFESTS = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T> void invokeEntrypoints(String id, Consumer<? super T> entrypoint) {
		MANIFESTS.forEach((modId, manifest) -> {
			String path = manifest.entrypoints.get(id);
			if (path == null) return;
			Class<T> entrypointClass = (Class<T>) Class.forName(path);
			T entrypointObj = UnsafeUtil.allocateInstance(entrypointClass); // bypass constructor
			entrypoint.accept(entrypointObj);
		});
	}
	
	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	public static void main(String[] args) {
		String[] yqhMods = System.getProperty("yqh.mods").split(",");
		for (String mod : yqhMods) {
			if (mod.isEmpty()) break; // don't loop if there aren't any YQH mods
			String[] modDef = mod.split(":");
			String modId = modDef[0];
			String modLoc = URLDecoder.decode(modDef[1], StandardCharsets.UTF_8);
			
			File modFile = new File(modLoc);
			byte[] manifestBytes = new byte[0];
			if (modFile.isDirectory()) {
				for (File file : Objects.requireNonNull(modFile.listFiles())) {
					if (file.getName().equals("yqh.mod.json")) {
						manifestBytes = FileUtils.readFileToByteArray(file);
					}
				}
			} else {
				try (JarFile modJar = new JarFile(modFile)) {
					ZipEntry manifestEntry = modJar.getJarEntry("yqh.mod.json");
					if (manifestEntry == null) continue;
					manifestBytes = modJar.getInputStream(manifestEntry).readAllBytes();
					Classes.addURL(YummyKnot.class.getClassLoader(), new URL(modLoc));
				}
			}
			if (manifestBytes.length == 0) continue;
			YummyManifest manifest = JsonIterator.deserialize(manifestBytes, YummyManifest.class);
			MANIFESTS.put(modId, manifest);
		}
		
		invokeEntrypoints("pre_loader", PreLoader::onPreLoader);
		
		if (!System.getProperties().containsKey("fabric.dli.main")) {
			Knot.main(args);
		} else {
			MethodHandle main = Invoker.findStatic(Class.forName("net.fabricmc.devlaunchinjector.Main"), "main", void.class, String[].class);
			main.invokeExact(args);
		}
	}
}
