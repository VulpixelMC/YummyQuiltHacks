package org.spongepowered.asm.mixin.transformer;

import net.cursedmc.yqh.api.mixin.Mixout;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;

public class HackedMixinProcessor extends MixinProcessor {
	HackedMixinProcessor(final MixinEnvironment environment, final Extensions extensions, final IHotSwap hotSwapper, final MixinCoprocessorNestHost nestHostCoprocessor) {
		super(environment, extensions, hotSwapper, nestHostCoprocessor);
	}
	
	@Override
	synchronized boolean applyMixins(final MixinEnvironment environment, final String name, final ClassNode targetClassNode) {
		boolean shouldApply;
		
		shouldApply = Mixout.TransformEvent.preMixin(name, targetClassNode);
		shouldApply |= super.applyMixins(environment, name, targetClassNode);
		shouldApply |= Mixout.TransformEvent.postMixin(name, targetClassNode);
		
		return shouldApply;
	}
}
