package net.cursedmc.yqh.mixin;

import net.auoeke.reflect.Accessor;
import net.cursedmc.yqh.YummyQuiltHacks;
import net.cursedmc.yqh.api.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.launch.knot.UnsafeKnotClassLoader;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.HackedMixinProcessor;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class YummyQuiltHacksMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	
	@Override
	public List<String> getMixins() {
		return null;
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	
	static {
		YummyQuiltHacks.isMixinLoaded = true;
		
		Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
		Object processor = Accessor.getReference(transformer, "processor");
		UnsafeUtil.unsafeCast(processor, HackedMixinProcessor.class);
		
		EntrypointUtils.invoke("yqh:pre_pre_launch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);
	}
}
