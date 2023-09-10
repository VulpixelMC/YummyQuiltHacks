package net.cursedmc.yqh;

import com.jsoniter.JsonIterator;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import it.unimi.dsi.fastutil.Pair;
import net.auoeke.reflect.Accessor;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.cursedmc.yqh.api.entrypoints.PreMixin;
import net.cursedmc.yqh.impl.manifest.YummyManifest;
import net.cursedmc.yqh.impl.manifest.YummyManifestSchema;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.LanguageAdapter;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.launch.knot.Knot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@ApiStatus.Internal
public class YummyQuiltHacks implements LanguageAdapter {
	@Deprecated(
			since = "0.2.0",
			forRemoval = true
	)
	public static final ClassLoader UNSAFE_LOADER;
	
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks");
	private static final boolean RELAUNCH = false;
	static final Map<String, YummyManifest> MANIFESTS = new HashMap<>();
	
	@Override
	public native <T> T create(ModContainer mod, String value, Class<T> type);
	
	private static void selfAttach(String path, String pid) {
		try {
			Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true");
		} catch (ClassNotFoundException e) {
			LOGGER.error("Oops! Looks like we can't force self-attaching. If you are reading this and the game has crashed, please add \"-Djdk.attach.allowAttachSelf=true\" to your JVM arguments.");
		}
		
		try {
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(path);
		} catch (AttachNotSupportedException e) {
			LOGGER.error("Self-attachment has failed! Try adding \"-Djdk.attach.allowAttachSelf=true\" to your JVM arguments.");
			throw new RuntimeException(e);
		} catch (AgentLoadException | AgentInitializationException e) {
			LOGGER.error("The agent has failed to load.");
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	static {
		boolean doPwn = Boolean.parseBoolean(System.getProperty("yqh.relaunch", "false"));
		doPwn = doPwn || !RELAUNCH; // FIXME: do this until we get relaunching working
		if (doPwn) {
			final ClassLoader appLoader = Knot.class.getClassLoader();
			final ClassLoader knotLoader = YummyQuiltHacks.class.getClassLoader();
			
			byte[] jarBytes;
			try (InputStream is = Objects.requireNonNull(YummyQuiltHacks.class.getClassLoader().getResource("yummy_agent.jar")).openStream()) {
				jarBytes = is.readAllBytes();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			final String vmPid = String.valueOf(ManagementFactory.getRuntimeMXBean().getPid());
			
			// make a temp file of the agent jar, so we can attach it
			final File tempJar;
			try {
				tempJar = File.createTempFile("tmp_", null);
				FileUtils.writeByteArrayToFile(tempJar, jarBytes);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			ByteBuddyAgent.attach(FileUtils.getFile(tempJar.getAbsolutePath()), vmPid);
			
			try {
				UnsafeUtil.initializeClass(appLoader.loadClass("org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader"));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			
			UNSAFE_LOADER = knotLoader;
			
			LOGGER.fatal("Quilt has been successfully pwned >:3");
			
			try (Stream<ModContainer> stream = QuiltLoader.getAllMods().stream()) {
				stream
						.map(mod -> Pair.of(mod, mod.getPath("yqh.mod.json")))
						.forEach(pair -> {
							ModContainer mod = pair.left();
							Path path = pair.right();
							if (Files.isRegularFile(path)) {
								byte[] manifestBytes;
								try {
									manifestBytes = Files.readAllBytes(path);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
								
								YummyManifestSchema manifestSchema = JsonIterator.deserialize(manifestBytes, YummyManifestSchema.class);
								if (manifestSchema.schemaVersion != 1) {
									throw new UnsupportedOperationException("the schema version " + manifestSchema.schemaVersion + " in the yqh.mod.json of " + mod.metadata().name() + " (" + mod.metadata().id() + ") is unsupported.");
								}
								
								YummyManifest manifest = JsonIterator.deserialize(manifestBytes, YummyManifest.class);
								MANIFESTS.put(mod.metadata().id(), manifest);
								
								// call entrypoint
								String entryClassName = manifest.entrypoints.get("pre_mixin");
								try {
									Class<? extends PreMixin> entryClass = knotLoader.loadClass(entryClassName).asSubclass(PreMixin.class);
									PreMixin entry = entryClass.getDeclaredConstructor().newInstance();
									entry.onPreMixin();
								} catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
										 IllegalAccessException | NoSuchMethodException e) {
									throw new RuntimeException(e);
								}
							}
						});
			}
		} else {
			UNSAFE_LOADER = null;
		}
	}
}
