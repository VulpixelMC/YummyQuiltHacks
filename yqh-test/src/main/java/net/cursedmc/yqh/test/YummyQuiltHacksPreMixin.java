package net.cursedmc.yqh.test;

import ca.rttv.ASMFormatParser;
import net.cursedmc.yqh.api.entrypoints.PreMixin;
import net.cursedmc.yqh.api.mixin.Mixout;
import net.fabricmc.api.EnvType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.MethodNode;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

public class YummyQuiltHacksPreMixin implements PreMixin {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/Test/PreMixin");
	
	@Override
	public void onPreMixin() {
		LOGGER.info("pre_mixin test");
		
		Mixout.TransformEvent.registerPreMixin((name, cn) -> {
			if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.SERVER) return;
			final String splashTextClass;
			final String getMethod;
			final String splashTextRendererClass;
			if (QuiltLoader.isDevelopmentEnvironment()) {
				splashTextClass = "net/minecraft/client/resource/SplashTextResourceSupplier";
				getMethod = "get";
				splashTextRendererClass = "net/minecraft/client/gui/SplashTextRenderer";
			} else {
				splashTextClass = "net/minecraft/class_4008";
				getMethod = "method_18174";
				splashTextRendererClass = "net/minecraft/class_8519";
			}
			if (splashTextClass.equals(cn.name)) {
				LOGGER.info("target class found");
				for (final MethodNode m : cn.methods) {
					if (getMethod.equals(m.name)) {
						LOGGER.info("target method found");
						m.instructions.clear();
						m.instructions.add(ASMFormatParser.parseInstructions(String.format("""
								A:
								LINE A 70
								NEW %1$s
								DUP
								LDC "experience the asm™"
								INVOKESPECIAL %1$s.<init>(Ljava/lang/String;)V
								ARETURN
								B:
								""", splashTextRendererClass), m, false));
						LOGGER.info("applied");
					}
				}
			}
		});
	}
}
