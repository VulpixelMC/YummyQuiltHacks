package net.cursedmc.yqh.test;

import net.cursedmc.yqh.api.entrypoints.PreLoader;
import net.cursedmc.yqh.api.mixin.Mixout;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;

public class YummyQuiltHacksPreLoader implements PreLoader {
	@Override
	public void onPreLoader() {
		System.out.println("Pre-loader entrypoint initialized!");
		
		Mixout.RawTransformEvent.registerPreLoader((name, bytes) -> {
			if (name.equals("org.quiltmc.loader.impl.game.minecraft.Hooks")) {
				System.out.println("Found Hooks class");
				ClassReader cr = new ClassReader(bytes);
				ClassNode cn = new ClassNode();
				cr.accept(cn, 0);
				
				cn.fields.forEach(field -> {
					if (field.name.equals("QUILT")) {
						field.value = "cursed";
						System.out.println("Found QUILT brand field");
					}
				});
				
				cn.methods.forEach(method -> {
					if (method.name.equals("<clinit>")) {
						InsnList insns = new InsnList();
						insns.add(new LdcInsnNode("cursed loader"));
						insns.add(new FieldInsnNode(Opcodes.PUTSTATIC, "org/quiltmc/loader/impl/game/minecraft/Hooks", "QUILT", "Ljava/lang/String;"));
						method.instructions.insertBefore(method.instructions.getLast(), insns);
						System.out.println("Found class initializer");
					}
				});
				
				ClassWriter cw = new ClassWriter(0);
				cn.accept(cw);
				
				return cw.toByteArray();
			}
			
			return bytes;
		});
	}
}
