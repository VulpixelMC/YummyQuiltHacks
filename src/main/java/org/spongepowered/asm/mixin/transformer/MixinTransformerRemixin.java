package org.spongepowered.asm.mixin.transformer;

import net.cursedmc.yqh.api.mixin.Mixout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.transformers.TreeTransformer;

@Mixin( targets =  "org.spongepowered.asm.mixin.transformer.MixinTransformer" )
public abstract class MixinTransformerRemixin extends TreeTransformer {
	@Shadow
	@Final
	private MixinProcessor processor;

	@Inject(
		method = "transformClass",
		at = @At("HEAD"),
		cancellable = true,
		remap = false
	)
	public void onTransformClass( MixinEnvironment environment, String name, byte[] classBytes, CallbackInfoReturnable<byte[]> cir ) {
		var classNode = this.readClass( name, classBytes );
		for ( var event : Mixout.TransformEvent.PRE_MIXIN )
			event.transform( name, classNode );

		if ( this.processor.applyMixins( environment, name, classNode ) ) {
			classBytes = this.writeClass(classNode);
			classNode = this.readClass( name, classBytes );
		}

		for ( var event : Mixout.TransformEvent.PRE_MIXIN )
			event.transform( name, classNode );

		cir.setReturnValue( classBytes );
		cir.cancel();
	}
}
