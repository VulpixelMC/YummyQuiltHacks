package net.cursedmc.yqh.api.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mixout {
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Mixout");
	
	@FunctionalInterface
	public interface TransformEvent {
		List<TransformEvent> PRE_MIXIN = new ArrayList<>();
		List<TransformEvent> POST_MIXIN = new ArrayList<>();
		
		void transform(String name, ClassNode node);
		
		static void registerPreMixin(TransformEvent callback) {
			PRE_MIXIN.add(callback);
		}
		
		static void registerPostMixin(TransformEvent callback) {
			POST_MIXIN.add(callback);
		}
		
		static boolean preMixin(String name, ClassNode cn) {
			PRE_MIXIN.forEach(callback -> callback.transform(name, cn));
			return PRE_MIXIN.isEmpty();
		}
		
		static boolean postMixin(String name, ClassNode cn) {
			POST_MIXIN.forEach(callback -> callback.transform(name, cn));
			return POST_MIXIN.isEmpty();
		}
	}
	
	@FunctionalInterface
	public interface RawTransformEvent {
		List<RawTransformEvent> PRE_LOADER = new ArrayList<>();
		
		byte[] transform(String name, byte[] bytes);
		
		static void registerPreLoader(RawTransformEvent callback) {
			PRE_LOADER.add(callback);
		}
		
		static byte[] preLoader(String name, byte[] bytes) {
			if (name.equals(Mixout.class.getName())) {
				throw new UnsupportedOperationException("Attempted to modify Mixout! Check if Mixout is being class-loaded twice.");
			}
			var $ = new Object() {
				byte[] b = bytes;
			};
			PRE_LOADER.forEach(callback -> $.b = callback.transform(name, $.b));
			if (name.equals("org.quiltmc.loader.impl.game.minecraft.Hooks")) {
				System.out.println("modified Hooks: " + !Arrays.equals(bytes, $.b));
			}
			return $.b;
		}
	}
	
	static {
		LOGGER.info("mixin, mixout. mixin, mixout. /lyr");
		LOGGER.info("we have truly achieved a sad state of realization, one that encompasses the entire jdk, the minecraft classes.. everything. we have untold power, and with so may come untold consequences. tread lightly, explorer.");
	}
}
