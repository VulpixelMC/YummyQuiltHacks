package com.example.example_mod.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@SuppressWarnings("SpellCheckingInspection")
	@Shadow @Final @Mutable private boolean isMinceraft;
	
	@Inject(method = "<init>(Z)V", at = @At("TAIL"))
	public void init(boolean bl, CallbackInfo ci) {
		isMinceraft = true;
	}
}
