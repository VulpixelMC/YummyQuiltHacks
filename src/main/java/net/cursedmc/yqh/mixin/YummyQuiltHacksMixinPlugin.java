package net.cursedmc.yqh.mixin;

import net.auoeke.reflect.Accessor;
import net.cursedmc.yqh.api.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.HackedMixinProcessor;

import java.util.List;
import java.util.Set;

public class YummyQuiltHacksMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(final String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {}

	@Override
	public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {}

	static {
		final Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
		final Object processor = Accessor.getReference(transformer, "processor");
		UnsafeUtil.unsafeCast(processor, HackedMixinProcessor.class);

		EntrypointUtils.invoke("yqh:pre_pre_launch", PrePreLaunch.class, PrePreLaunch::onPrePreLaunch);
	}
}
