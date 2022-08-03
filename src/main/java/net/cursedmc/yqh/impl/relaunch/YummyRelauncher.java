package net.cursedmc.yqh.impl.relaunch;

import net.auoeke.reflect.Fields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.discovery.ModCandidate;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@SuppressWarnings("unchecked")
public class YummyRelauncher implements LanguageAdapter {
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Relauncher");
	
	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);
	
	static {
		if (!Boolean.parseBoolean(System.getProperty("yqh.relaunch", "false"))) { // make sure we don't infinitely relaunch
			LOGGER.info("Relaunching game...");
			
			// find mods that define YQH manifests, put them in the mod path array
			final List<String> yqhMods = new ArrayList<>(); // paths to mods that have a yqh.mod.json
			final Field modCandidatesField = Fields.of(QuiltLoaderImpl.class, "modCandidates");
			modCandidatesField.setAccessible(true);
			final List<ModCandidate> modCandidates = (List<ModCandidate>) modCandidatesField.get(QuiltLoaderImpl.INSTANCE);
			for (final ModCandidate candidate : modCandidates) {
				File modFile = new File(URLDecoder.decode(candidate.getOriginPath().toAbsolutePath().toString(), StandardCharsets.UTF_8));
				if (!modFile.exists()) continue;
				if (QuiltLoader.isDevelopmentEnvironment() && modFile.isDirectory()) {
					for (File file : Objects.requireNonNull(modFile.listFiles())) {
						if (file.getName().equals("yqh.mod.json")) {
							yqhMods.add(candidate.getId() + ':' + modFile.getAbsolutePath());
						}
					}
				}
				if (modFile.isDirectory()) continue;
				try (final JarFile modJar = new JarFile(modFile)) {
					final ZipEntry manifestEntry = modJar.getEntry("yqh.mod.json");
					if (manifestEntry == null) continue;
					yqhMods.add(modFile.getAbsolutePath());
				}
			}
			
			try {
				Process process = RelaunchUtils.startProcess(true, false, yqhMods);
				
				// kill this process
				System.exit(process.waitFor());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
