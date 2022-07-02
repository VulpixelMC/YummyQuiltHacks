package org.spongepowered.asm.mixin.transformer;

import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinInitialisationError;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtensionRegistry;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;
import org.spongepowered.asm.transformers.TreeTransformer;
import org.spongepowered.asm.util.asm.ASM;

import java.lang.reflect.Constructor;
import java.util.List;

public class HackedMixinTransformer extends TreeTransformer implements IMixinTransformer {
	public static final HackedMixinTransformer INSTANCE;
	
	private final SyntheticClassRegistry syntheticClassRegistry;
	private final Extensions extensions;
	private final IHotSwap hotSwapper;
	private final MixinCoprocessorNestHost nestHostCoprocessor;
	private final MixinProcessor processor;
	private final MixinClassGenerator generator;
	
	HackedMixinTransformer() {
		MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
		Object globalMixinTransformer = environment.getActiveTransformer();
		if (globalMixinTransformer instanceof IMixinTransformer) {
			throw new MixinException("Terminating MixinTransformer instance " + this);
		} else {
			environment.setActiveTransformer(this);
			this.syntheticClassRegistry = new SyntheticClassRegistry();
			this.extensions = new Extensions(this.syntheticClassRegistry);
			this.hotSwapper = this.initHotSwapper(environment);
			this.nestHostCoprocessor = new MixinCoprocessorNestHost();
			this.processor = new MixinProcessor(environment, this.extensions, this.hotSwapper, this.nestHostCoprocessor);
			this.generator = new MixinClassGenerator(environment, this.extensions);
			DefaultExtensions.create(environment, this.extensions, this.syntheticClassRegistry, this.nestHostCoprocessor);
		}
	}
	
	private IHotSwap initHotSwapper(MixinEnvironment environment) {
		if (!environment.getOption(MixinEnvironment.Option.HOT_SWAP)) {
			return null;
		} else {
			try {
				MixinProcessor.logger.info("Attempting to load Hot-Swap agent");
				Class<?> clazz = Class.forName("org.spongepowered.tools.agent.MixinAgent");
				Constructor<?> ctor = clazz.getDeclaredConstructor(IMixinTransformer.class);
				return (IHotSwap)ctor.newInstance(this);
			} catch (Throwable var4) {
				MixinProcessor.logger.info("Hot-swap agent could not be loaded, hot swapping of mixins won't work. {}: {}", var4.getClass().getSimpleName(), var4.getMessage());
				return null;
			}
		}
	}
	
	public IExtensionRegistry getExtensions() {
		return this.extensions;
	}
	
	public String getName() {
		return this.getClass().getName();
	}
	
	public boolean isDelegationExcluded() {
		return true;
	}
	
	public void audit(MixinEnvironment environment) {
		this.processor.audit(environment);
	}
	
	public List<String> reload(String mixinClass, ClassNode classNode) {
		return this.processor.reload(mixinClass, classNode);
	}
	
	public byte[] transformClassBytes(String name, String transformedName, byte[] basicClass) {
		if (transformedName == null) {
			return basicClass;
		} else {
			MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
			return basicClass == null ? this.generateClass(environment, transformedName) : this.transformClass(environment, transformedName, basicClass);
		}
	}
	
	public boolean computeFramesForClass(MixinEnvironment environment, String name, ClassNode classNode) {
		return false;
	}
	
	public byte[] transformClass(MixinEnvironment environment, String name, byte[] classBytes) {
		ClassNode classNode = this.readClass(name, classBytes);
		return this.processor.applyMixins(environment, name, classNode) ? this.writeClass(classNode) : classBytes;
	}
	
	public boolean transformClass(MixinEnvironment environment, String name, ClassNode classNode) {
		return this.processor.applyMixins(environment, name, classNode);
	}
	
	public byte[] generateClass(MixinEnvironment environment, String name) {
		ClassNode classNode = createEmptyClass(name);
		return this.generator.generateClass(environment, name, classNode) ? this.writeClass(classNode) : null;
	}
	
	public boolean generateClass(MixinEnvironment environment, String name, ClassNode classNode) {
		return this.generator.generateClass(environment, name, classNode);
	}
	
	private static ClassNode createEmptyClass(String name) {
		ClassNode classNode = new ClassNode(ASM.API_VERSION);
		classNode.name = name.replace('.', '/');
		classNode.version = MixinEnvironment.getCompatibilityLevel().getClassVersion();
		classNode.superName = "java/lang/Object";
		return classNode;
	}
	
	static class Factory implements IMixinTransformerFactory {
		Factory() {
		}
		
		public IMixinTransformer createTransformer() throws MixinInitialisationError {
			return new MixinTransformer();
		}
	}
	
	static {
		final Object mixinTransformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
		
		// change the klass pointer of the object so it points to us
		UnsafeUtil.unsafeCast(mixinTransformer, HackedMixinTransformer.class);
		
		INSTANCE = (HackedMixinTransformer) mixinTransformer;
	}
}
