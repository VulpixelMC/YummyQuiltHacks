package org.spongepowered.asm.mixin.transformer;

import ca.rttv.ASMFormatParser;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Methods;
import net.cursedmc.yqh.api.mixin.Mixout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.transformers.TreeTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HackedMixinTransformer {
	private static final Method readClass;
	private static final Method writeClass;
	private static final Field processorField;
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/HackedMixinTransformer");

	public static byte[] transformClass(MixinTransformer self, MixinEnvironment environment, String name, byte[] classBytes) {
		LOGGER.info( "Hello from hacked" );
		return classBytes;
	}

	public static ClassNode readClass(TreeTransformer self, String className, byte[] basicClass) {
		return (ClassNode) readClass.invoke(self, className, basicClass);
	}

	public static byte[] writeClass(TreeTransformer self, ClassNode classNode) {
		return (byte[]) writeClass.invoke(self, classNode);
	}

	public static MixinProcessor getProcessor(MixinTransformer self) {
		return (MixinProcessor) processorField.get(self);
	}

	static {
		readClass = Methods.of(TreeTransformer.class, "readClass", String.class, byte[].class);
		readClass.setAccessible(true);
		writeClass = Methods.of(TreeTransformer.class, "writeClass", ClassNode.class);
		writeClass.setAccessible(true);
		processorField = Fields.of(MixinTransformer.class, "processor");
		processorField.setAccessible(true);
	}
}
