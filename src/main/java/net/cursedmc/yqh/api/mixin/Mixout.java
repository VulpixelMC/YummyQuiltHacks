package net.cursedmc.yqh.api.mixin;

import ca.rttv.ASMFormatParser;
import net.cursedmc.yqh.api.instrumentation.Music;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.quiltmc.qsl.base.api.event.Event;

import java.util.ArrayList;
import java.util.List;

public class Mixout {
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Mixout");
	
	@FunctionalInterface
	public interface TransformEvent {
		List<TransformEvent> PRE_MIXIN = new ArrayList<>();
		List<TransformEvent> POST_MIXIN = new ArrayList<>();
		
		void transform(String name, ClassNode node);
	}
	
	static {
		LOGGER.info("mixin, mixout. mixin, mixout. /lyr");
		LOGGER.info("we have truly achieved a sad state of realization, one that encompasses the entire jdk, the minecraft classes.. everything. we have untold power, and with so may come untold consequences. tread lightly, explorer.");
		
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
				}
			}
		});
	}
}
