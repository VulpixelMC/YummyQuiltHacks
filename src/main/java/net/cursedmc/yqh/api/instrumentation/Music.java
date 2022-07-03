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
import org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.function.BiConsumer;

import static net.cursedmc.yqh.impl.instrumentation.MusicAgent.INST;

public class Music {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Music");
	
	private Music() {}
	
	public static void retransformClass(Class<?> klass, BiConsumer<String, ClassNode> consumer) {
		ClassFileTransformer transformer = createTransformer(consumer);
		INST.addTransformer(transformer, true);
		try {
			INST.retransformClasses(klass);
		} catch (UnmodifiableClassException e) {
			YummyQuiltHacks.LOGGER.error("An error has occurred retransforming class " + klass.getName());
			throw new RuntimeException(e);
		}
		INST.removeTransformer(transformer);
	}
	
	public static void retransformClass(String name, BiConsumer<String, ClassNode> consumer) {
		try {
			retransformClass(Class.forName(name, true, UnsafeKnotClassLoader.knotLoader), consumer);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ClassFileTransformer createTransformer(BiConsumer<String, ClassNode> consumer) {
		return new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
				ClassReader cr = new ClassReader(classfileBuffer);
				ClassNode cn = new ClassNode(Opcodes.ASM9);
				cr.accept(cn, 0);
				consumer.accept(className, cn);
				ClassWriter cw = new ClassWriter(0);
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
	}
}
