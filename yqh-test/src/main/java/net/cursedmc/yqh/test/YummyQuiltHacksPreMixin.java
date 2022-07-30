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
			final String splashTextClass;
			final String getMethod;
			if (QuiltLoader.isDevelopmentEnvironment()) {
				splashTextClass = "net/minecraft/client/resource/SplashTextResourceSupplier";
				getMethod = "get";
			} else {
				splashTextClass = "net/minecraft/class_4008";
				getMethod = "method_18174";
			}
			if (splashTextClass.equals(cn.name)) {
				LOGGER.info("target class found");
				for (final MethodNode m : cn.methods) {
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

		Mixout.TransformEvent.registerPreMixin((name, cn) -> {
			if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.SERVER) return;
			final String screenClass;
			final String textClass;
			final String screenTitleField;
			final String textOfMethod;
			if (QuiltLoader.isDevelopmentEnvironment()) {
				screenClass = "net/minecraft/client/gui/screen/Screen";
				textClass = "net/minecraft/text/Text";
				screenTitleField = "title";
				textOfMethod = "of";
			} else {
				screenClass = "net/minecraft/class_437";
				textClass = "net/minecraft/class_2561";
				screenTitleField = "field_22785";
				textOfMethod = "method_30163";
			}
			if (screenClass.replace('/', '.').equals(name)) {
				LOGGER.info("screen class found");
				for (final FieldNode f : cn.fields) {
					if (screenTitleField.equals(f.name)) {
						LOGGER.info("target field found");
						f.access = f.access ^ Opcodes.ACC_FINAL;
					}
				}
				for (final MethodNode m : cn.methods) {
					if ("<init>".equals(m.name)) {
						LOGGER.info("target method found");
						m.instructions.insertBefore(m.instructions.get(m.instructions.size() - 2), ASMFormatParser.parseInstructions("""
                        A:
                        LINE A 1
                        ALOAD 0
                        LDC "experience the asm™"
                        """ + "INVOKESTATIC_itf " + textClass + '.' + textOfMethod + "(Ljava/lang/String;)L" + textClass + ';' +
								"\nPUTFIELD " + screenClass + '.' + screenTitleField + " L" + textClass + ';' + """

								B:
								""", m, false));
						LOGGER.info("applied");
					}
				}
			}
		});
	}
}
