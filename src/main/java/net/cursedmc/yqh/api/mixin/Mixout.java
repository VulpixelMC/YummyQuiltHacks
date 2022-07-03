package net.cursedmc.yqh.api.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.qsl.base.api.event.Event;

public class Mixout {
	public static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Mixout");
	
	@FunctionalInterface
	public interface TransformEvent {
		Event<TransformEvent> PRE_MIXIN = Event.create(TransformEvent.class, callbacks -> (name, node) -> {
			for (TransformEvent cb : callbacks) {
				cb.transform(name, node);
			}
		});
		Event<TransformEvent> POST_MIXIN = Event.create(TransformEvent.class, callbacks -> (name, node) -> {
			for (TransformEvent cb : callbacks) {
				cb.transform(name, node);
			}
		});
		
		void transform(String name, ClassNode node);
	}
	
	static {
		LOGGER.info("mixin, mixout. mixin, mixout. /lyr");
		LOGGER.info("we have truly achieved a sad state of realization, one that encompasses the entire jdk, the minecraft classes.. everything. we have untold power, and with so may come untold consequences. tread lightly, explorer.");
	}
}
