package org.spongepowered.asm.mixin.transformer;

import net.cursedmc.yqh.api.mixin.Mixout;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;

public class HackedMixinProcessor extends MixinProcessor {
	HackedMixinProcessor(MixinEnvironment environment, Extensions extensions, IHotSwap hotSwapper, MixinCoprocessorNestHost nestHostCoprocessor) {
		super(environment, extensions, hotSwapper, nestHostCoprocessor);
	}
	
	@Override
	synchronized boolean applyMixins(MixinEnvironment environment, String name, ClassNode targetClassNode) {
		Mixout.TransformEvent.preMixin(name, targetClassNode);
		boolean applied = super.applyMixins(environment, name, targetClassNode);
		Mixout.TransformEvent.postMixin(name, targetClassNode);
		return applied;
	}
}
