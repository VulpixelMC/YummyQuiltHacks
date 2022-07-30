package net.cursedmc.yqh.api.instrumentation;

import ca.rttv.ASMFormatParser;
import net.cursedmc.yqh.YummyQuiltHacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.function.BiConsumer;

public class Music {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Music");
	public static final Instrumentation INST;

	private Music() {}

	public static void retransformClass(final Class<?> klass, final BiConsumer<String, ClassNode> consumer) {
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

	public static void retransformClass(final String name, final BiConsumer<String, ClassNode> consumer) {
		try {
			retransformClass(Class.forName(name, true, UnsafeKnotClassLoader.knotLoader), consumer);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static ClassFileTransformer createTransformer(final BiConsumer<String, ClassNode> consumer) {
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
		LOGGER.info("what have we done");
		LOGGER.info("how did we get here");
		LOGGER.info("achievement unlcoekd");
		LOGGER.info("advancement*");
		final ClassLoader appLoader = Knot.class.getClassLoader();

		final Class<?> musicAgent = Class.forName("net.cursedmc.yqh.impl.instrumentation.MusicAgent", true, appLoader);
		INST = (Instrumentation) musicAgent.getDeclaredField("INST").get(null);
	}
}
