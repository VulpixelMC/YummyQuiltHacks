package net.cursedmc.yqh.mixin;

import net.cursedmc.yqh.YummyQuiltHacks;
import net.cursedmc.yqh.api.entrypoints.PrePreLaunch;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
		
		EntrypointUtils.invoke("yqh:pre_pre_launch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);
	}
}
