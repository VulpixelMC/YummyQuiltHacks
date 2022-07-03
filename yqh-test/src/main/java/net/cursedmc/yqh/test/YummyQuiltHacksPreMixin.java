package net.cursedmc.yqh.test;

import ca.rttv.ASMFormatParser;
import net.cursedmc.yqh.api.entrypoints.PreMixin;
import net.cursedmc.yqh.api.mixin.Mixout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class YummyQuiltHacksPreMixin implements PreMixin {
	private static final Logger LOGGER = LogManager.getLogger("YummyQuiltHacks/PreMixin");
	
	@Override
	public void onPreMixin() {
		LOGGER.info("pre_mixin test");
		Mixout.TransformEvent.PRE_MIXIN.add((name, cn) -> {
			if ("net.minecraft.client.gui.screen.Screen".equals(name)) {
				LOGGER.info("screen class found");
				for (FieldNode f : cn.fields) {
					if ("title".equals(f.name)) {
						LOGGER.info("target field found");
						f.access = f.access ^ Opcodes.ACC_FINAL;
					}
				}
				for (MethodNode m : cn.methods) {
					if ("<init>".equals(m.name)) {
						LOGGER.info("target method found");
						m.instructions.insertBefore(m.instructions.get(m.instructions.size() - 2), ASMFormatParser.parseInstructions("""
                        ALOAD 0
                        LDC "experience the asm™"
                        INVOKESTATIC_itf net/minecraft/text/Text.of(Ljava/lang/String;)Lnet/minecraft/text/Text;
                        PUTFIELD net/minecraft/client/gui/screen/Screen.title Lnet/minecraft/text/Text;
                        """, m));
						m.instructions.forEach(LOGGER::info);
					}
				}
			}
		});
	}
}
