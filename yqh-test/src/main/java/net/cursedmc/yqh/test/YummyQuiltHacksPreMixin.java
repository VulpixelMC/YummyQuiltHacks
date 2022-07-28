package net.cursedmc.yqh.test;

import ca.rttv.ASMFormatParser;
import net.cursedmc.yqh.api.entrypoints.PreMixin;
import net.cursedmc.yqh.api.mixin.Mixout;
import net.fabricmc.api.EnvType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

public class YummyQuiltHacksPreMixin implements PreMixin {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/PreMixin");
	
	@Override
	public void onPreMixin() {
		LOGGER.info("pre_mixin test");
		Mixout.TransformEvent.registerPreMixin((name, cn) -> {
			if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.SERVER) return;
			String splashTextClass;
			String getMethod;
			if (QuiltLoader.isDevelopmentEnvironment()) {
				splashTextClass = "net/minecraft/client/resource/SplashTextResourceSupplier";
				getMethod = "get";
			} else {
				splashTextClass = "net/minecraft/class_4008";
				getMethod = "method_18174";
			}
			if (splashTextClass.equals(cn.name)) {
				LOGGER.info("target class found");
				for (MethodNode m : cn.methods) {
					if (getMethod.equals(m.name)) {
						LOGGER.info("target method found");
						m.instructions.clear();
						m.instructions.add(ASMFormatParser.parseInstructions("""
								A:
								LINE A 70
								LDC "experience the asm™"
								ARETURN
								B:
								""", m, false));
						LOGGER.info("applied");
					}
				}
			}
		});
	}
}
