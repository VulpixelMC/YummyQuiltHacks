package net.cursedmc.yqh.api.instrumentation;

import net.cursedmc.yqh.YummyQuiltHacks;
import net.cursedmc.yqh.api.classloader.UnsafeKnotClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.impl.launch.knot.Knot;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.function.BiConsumer;

public class Music {
	/**
	 * @deprecated
	 * @see Music#getInstrument()
	 */
	@Deprecated(
			since = "0.2.0",
			forRemoval = true
	)
	public static final Instrumentation INST;
	
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Music");
	
	private Music() {
	}
	
	public static Instrumentation getInstrument() {
		return INST;
	}
	
	public static void retransformClass(Class<?> klass, BiConsumer<String, ClassNode> consumer) {
		final ClassFileTransformer transformer = createTransformer(consumer);
		INST.addTransformer(transformer, true);
		try {
			INST.retransformClasses(klass);
		} catch (final UnmodifiableClassException e) {
			YummyQuiltHacks.LOGGER.error("An error has occurred retransforming class " + klass.getName());
			throw new RuntimeException(e);
		}
		INST.removeTransformer(transformer);
	}
	
	public static void retransformClass(String name, BiConsumer<String, ClassNode> consumer) {
		try {
			retransformClass(Class.forName(name, true, UnsafeKnotClassLoader.INSTANCE), consumer);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ClassFileTransformer createTransformer(BiConsumer<String, ClassNode> consumer) {
		return new ClassFileTransformer() {
			@Override
			public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
				final ClassReader cr = new ClassReader(classfileBuffer);
				final ClassNode cn = new ClassNode(Opcodes.ASM9);
				cr.accept(cn, 0);
				consumer.accept(className, cn);
				final ClassWriter cw = new ClassWriter(0);
				cn.accept(cw);
				return cw.toByteArray();
			}
		};
	}
	
	static {
		LOGGER.info("Music Loaded");
		final ClassLoader appLoader = Knot.class.getClassLoader();
		
		final Class<?> musicAgent;
		try {
			musicAgent = Class.forName("net.cursedmc.yqh.impl.instrumentation.MusicAgent", true, appLoader);
			INST = (Instrumentation) musicAgent.getDeclaredField("INST").get(null);
		} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
