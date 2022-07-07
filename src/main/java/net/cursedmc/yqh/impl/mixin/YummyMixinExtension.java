package net.cursedmc.yqh.impl.mixin;

import net.cursedmc.yqh.api.mixin.Mixout;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public class YummyMixinExtension implements IExtension {
	@Override
	public boolean checkActive(MixinEnvironment environment) {
		return true;
	}
	
	@Override
	public void preApply(ITargetClassContext context) {
		System.out.println(context.getClassInfo().getClassName());
		Mixout.TransformEvent.preMixin(context.getClassInfo().getClassName(), context.getClassNode());
	}
	
	@Override
	public void postApply(ITargetClassContext context) {
		Mixout.TransformEvent.postMixin(context.getClassInfo().getClassName(), context.getClassNode());
	}
	
	@Override
	public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
	}
}
