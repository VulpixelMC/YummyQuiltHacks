package net.cursedmc.yqh.instrumentation;

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

import static net.cursedmc.yqh.instrumentation.MusicAgent.INST;

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
		
		Music.retransformClass(Music.class.getClassLoader().loadClass("org.spongepowered.asm.mixin.transformer.MixinTransformer"), (name, cn) -> {
			for (MethodNode m : cn.methods) {
				if ("(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;[B)[B".equals(m.desc)) {
					LOGGER.info("Target method found");
					m.instructions.clear();
					m.instructions.add(ASMFormatParser.parseInstructions("""
                            A:
                            LINE A 1
                            ALOAD 0
                            ALOAD 1
                            ALOAD 2
                            ALOAD 3
                            INVOKESTATIC org/spongepowered/asm/mixin/transformer/HackedMixinTransformer.transformClass(Lorg/spongepowered/asm/mixin/transformer/MixinTransformer;Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;[B)[B
                            ARETURN
                            B:
                            """, m));
					m.instructions.forEach(node -> {
						LOGGER.info(node.getClass().getName());
						LOGGER.info(node.getOpcode());
					});
				}
			}
		});
	}
}
