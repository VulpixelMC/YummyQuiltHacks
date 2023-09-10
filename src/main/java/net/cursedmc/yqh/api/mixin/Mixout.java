package net.cursedmc.yqh.api.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
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
	
	static {
		LOGGER.info("Mixout initialized.");
	}
}
