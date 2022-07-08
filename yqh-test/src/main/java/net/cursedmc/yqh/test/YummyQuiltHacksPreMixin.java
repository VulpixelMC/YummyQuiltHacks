package net.cursedmc.yqh.test;

import ca.rttv.ASMFormatParser;
import net.cursedmc.yqh.api.entrypoints.PreMixin;
import net.cursedmc.yqh.api.mixin.Mixout;
import net.cursedmc.yqh.api.util.MapUtils;
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
			String screenClass;
			String textClass;
			String screenTitleField;
			String textOfMethod;
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
				for (FieldNode f : cn.fields) {
					if (screenTitleField.equals(f.name)) {
						LOGGER.info("target field found");
						f.access = f.access ^ Opcodes.ACC_FINAL;
					}
				}
				for (MethodNode m : cn.methods) {
					if ("<init>".equals(m.name)) {
						LOGGER.info("target method found");
						m.instructions.insertBefore(m.instructions.get(m.instructions.size() - 2), ASMFormatParser.parseInstructions("""
                        ALOAD 0
                        LDC "experience the A S M (T M)."
                        """ + "INVOKESTATIC_itf " + textClass + '.' + textOfMethod + "(Ljava/lang/String;)L" + textClass + ';' +
                        "\nPUTFIELD " + screenClass + '.' + screenTitleField + " L" + textClass + ';', m, false));
						m.instructions.forEach(insn -> {
							if (insn instanceof VarInsnNode) {
								LOGGER.info("var_insn " + ((VarInsnNode) insn).var);
							} else if (insn instanceof LdcInsnNode) {
								LOGGER.info("ldc " + ((LdcInsnNode) insn).cst);
							} else if (insn instanceof MethodInsnNode) {
								LOGGER.info("method_insn " + ((MethodInsnNode) insn).owner + '.' + ((MethodInsnNode) insn).name + ((MethodInsnNode) insn).desc);
							} else if (insn instanceof FieldInsnNode) {
								LOGGER.info("field_insn " + ((FieldInsnNode) insn).owner + '.' + ((FieldInsnNode) insn).name + ' ' + ((FieldInsnNode) insn).desc);
							} else {
								LOGGER.info(insn);
							}
						});
						LOGGER.info("applied");
					}
				}
			}
		});
	}
}
