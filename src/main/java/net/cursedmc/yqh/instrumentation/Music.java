package net.cursedmc.yqh.instrumentation;

import net.auoeke.reflect.Reflect;
import net.cursedmc.yqh.YummyQuiltHacks;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.function.BiConsumer;

public class Music {
	private static final Instrumentation INST;
	
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
	
	private static ClassFileTransformer createTransformer(BiConsumer<String, ClassNode> consumer) {
		return new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
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
		try {
			INST = Reflect.instrument().value();
		} catch (Throwable throwable) {
			YummyQuiltHacks.LOGGER.error("An error has occurred retrieving the Instrumentation API");
			throw new RuntimeException(throwable);
		}
	}
}
