package ca.rttv;

import net.cursedmc.yqh.api.util.MapUtils;
import org.objectweb.asm.tree.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.*;

//https://gist.github.com/RealRTTV/6d4576998f3780c9766a63caa5ab9ae1
public class ASMFormatParser {
	
	private static final String PLACEHOLDER = "!YQH!";
	
	/**
	 * Finds any unmapped {@code net.minecraft.*} classes/methods/fields and maps them.
	 * @param unmapped the unmapped string
	 * @return the mapped string
	 */
	public static String mapString(String unmapped) {
		// map methods
		Pattern methodPattern = Pattern.compile("(net/minecraft/.+)\\.(.+)(\\(.+\\)\\S+)");
		Matcher methodMatcher = methodPattern.matcher(unmapped);
		while (methodMatcher.find()) {
			unmapped = (unmapped.replace(methodMatcher.group(), MapUtils.mappedClass(methodMatcher.group(1).replaceAll("/", ".")).replaceAll("\\.", "/") + '.' + MapUtils.mappedMethod(methodMatcher.group(1).replaceAll("/", "."), methodMatcher.group(2), methodMatcher.group(3)) + methodMatcher.group(3))).replaceAll("net/minecraft", "net/" + PLACEHOLDER);
		}
		
		// map fields
		Pattern fieldPattern = Pattern.compile("(net/minecraft/.+\\.([^(\\s]+)) (\\[?(?:[BCDFIJSZ]|(?:L.+;)))");
		Matcher fieldMatcher = fieldPattern.matcher(unmapped);
		while (fieldMatcher.find()) {
			unmapped = unmapped.replace(fieldMatcher.group(), MapUtils.mappedField(fieldMatcher.group(1).replaceAll("/", "."), fieldMatcher.group(2), fieldMatcher.group(3)).replaceAll("\\.(?![A-Za-z]+[( ])", "/").replaceAll("net/minecraft", "net/" + PLACEHOLDER));
		}
		
		// map classes
		Pattern classPattern = Pattern.compile("net/minecraft/[^;.\\s]+");
		Matcher classMatcher = classPattern.matcher(unmapped);
		while (classMatcher.find()) {
			unmapped = unmapped.replace(classMatcher.group(), MapUtils.mappedClass(classMatcher.group().replaceAll("/", "."))).replaceAll("\\.(?![A-Za-z]+[( ])", "/");
		}
		
		return unmapped.replaceAll("net/" + PLACEHOLDER, "net/minecraft");
	}
	
	public static InsnList parseInstructions(String unmapped, MethodNode method) {
		return parseInstructions(unmapped, method, true);
	}
	
	public static InsnList parseInstructions(String unmapped, MethodNode method, boolean map) {
		String mapped = map ? mapString(unmapped) : unmapped; // the mapped string
		System.out.println(mapped);
		
		InsnList list = new InsnList();
		Map<String, LabelNode> labels = new HashMap<>();
		Map<String, LabelNode> vanillaLabels = new HashMap<>();
		
		String[] lines = mapped.split("\n");
		
		for (String line : lines) {
			if (line.matches("[A-Za-z]+:")) {
				labels.put(line.substring(0, line.length() - 1), new LabelNode());
			}
		}
		
		int count = 0;
		for (AbstractInsnNode insn : method.instructions) {
			if (insn instanceof LineNumberNode lineNumberNode) {
				vanillaLabels.put(getLabelName(count++), lineNumberNode.start);
			}
		}
		
		for (String line : lines) {
			parseInstruction(line, list, labels, vanillaLabels);
		}
		
		return list;
	}
	
	private static String getLabelName(int num) {
		final int len = 16;
		byte[] characters = new byte[len];
		int i;
		num++;
		for (i = len - 1; num != 0; i--) {
			characters[i] = (byte) ((--num % 26) + 'A');
			num /= 26;
		}
		return new String(characters, ++i, len - i, StandardCharsets.US_ASCII);
		// tks Geolykt ♥
	}
	
	private static LabelNode getLabel(String str, Map<String, LabelNode> labels, Map<String, LabelNode> vanillaLabels) {
		if (str.endsWith(":")) {
			str = str.substring(0, str.length() - 1);
		}
		final String otherStr = str;
		return Optional.ofNullable(labels.get(str)).orElseGet(() -> Optional.ofNullable(vanillaLabels.get(otherStr)).orElseThrow(NodeNotFoundException::new));
	}
	
	private static void parseInstruction(String line, InsnList list, Map<String, LabelNode> labels, Map<String, LabelNode> vanillaLabels) {
		String[] words = new String[10]; // seems good enough
		
		boolean quote = false;
		int index = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < line.length(); i++) {
			char cur = line.charAt(i);
			if (cur == '"') {
				quote = !quote;
			}
			
			if (cur == ' ' && !quote) {
				words[index++] = sb.toString();
				sb = new StringBuilder();
				continue;
			}
			
			sb.append(cur);
		}
		
		words[index] = sb.toString();
		
		switch (words[0]) {
			case "NOP", "DEFINE" -> {}
			case "ACONST_NULL" -> list.add(new InsnNode(ACONST_NULL));
			case "ICONST_M1" -> list.add(new InsnNode(ICONST_M1));
			case "ICONST_0" -> list.add(new InsnNode(ICONST_0));
			case "ICONST_1" -> list.add(new InsnNode(ICONST_1));
			case "ICONST_2" -> list.add(new InsnNode(ICONST_2));
			case "ICONST_3" -> list.add(new InsnNode(ICONST_3));
			case "ICONST_4" -> list.add(new InsnNode(ICONST_4));
			case "ICONST_5" -> list.add(new InsnNode(ICONST_5));
			case "LCONST_0" -> list.add(new InsnNode(LCONST_0));
			case "LCONST_1" -> list.add(new InsnNode(LCONST_1));
			case "FCONST_0" -> list.add(new InsnNode(FCONST_0));
			case "FCONST_1" -> list.add(new InsnNode(FCONST_1));
			case "FCONST_2" -> list.add(new InsnNode(FCONST_2));
			case "DCONST_0" -> list.add(new InsnNode(DCONST_0));
			case "DCONST_1" -> list.add(new InsnNode(DCONST_1));
			case "BIPUSH" -> list.add(new IntInsnNode(BIPUSH, Integer.parseInt(words[1])));
			case "SIPUSH" -> list.add(new IntInsnNode(SIPUSH, Integer.parseInt(words[1])));
			case "LDC" -> list.add(new LdcInsnNode(getLdc(words[1])));
			case "ILOAD" -> list.add(new VarInsnNode(ILOAD, Integer.parseInt(words[1])));
			case "LLOAD" -> list.add(new VarInsnNode(LLOAD, Integer.parseInt(words[1])));
			case "FLOAD" -> list.add(new VarInsnNode(FLOAD, Integer.parseInt(words[1])));
			case "DLOAD" -> list.add(new VarInsnNode(DLOAD, Integer.parseInt(words[1])));
			case "ALOAD" -> list.add(new VarInsnNode(ALOAD, Integer.parseInt(words[1])));
			case "ALOAD_0" -> list.add(new InsnNode(42));
			case "ALOAD_1" -> list.add(new InsnNode(43));
			case "ALOAD_2" -> list.add(new InsnNode(44));
			case "ALOAD_3" -> list.add(new InsnNode(45));
			case "IALOAD" -> list.add(new InsnNode(IALOAD));
			case "LALOAD" -> list.add(new InsnNode(LALOAD));
			case "FALOAD" -> list.add(new InsnNode(FALOAD));
			case "DALOAD" -> list.add(new InsnNode(DALOAD));
			case "AALOAD" -> list.add(new InsnNode(AALOAD));
			case "BALOAD" -> list.add(new InsnNode(BALOAD));
			case "CALOAD" -> list.add(new InsnNode(CALOAD));
			case "SALOAD" -> list.add(new InsnNode(SALOAD));
			case "ISTORE" -> list.add(new VarInsnNode(ISTORE, Integer.parseInt(words[1])));
			case "LSTORE" -> list.add(new VarInsnNode(LSTORE, Integer.parseInt(words[1])));
			case "FSTORE" -> list.add(new VarInsnNode(FSTORE, Integer.parseInt(words[1])));
			case "DSTORE" -> list.add(new VarInsnNode(DSTORE, Integer.parseInt(words[1])));
			case "ASTORE" -> list.add(new VarInsnNode(ASTORE, Integer.parseInt(words[1])));
			case "IASTORE" -> list.add(new InsnNode(IASTORE));
			case "LASTORE" -> list.add(new InsnNode(LASTORE));
			case "FASTORE" -> list.add(new InsnNode(FASTORE));
			case "DASTORE" -> list.add(new InsnNode(DASTORE));
			case "AASTORE" -> list.add(new InsnNode(AASTORE));
			case "BASTORE" -> list.add(new InsnNode(BASTORE));
			case "CASTORE" -> list.add(new InsnNode(CASTORE));
			case "SASTORE" -> list.add(new InsnNode(SASTORE));
			case "POP" -> list.add(new InsnNode(POP));
			case "POP2" -> list.add(new InsnNode(POP2));
			case "DUP" -> list.add(new InsnNode(DUP));
			case "DUP_X1" -> list.add(new InsnNode(DUP_X1));
			case "DUP_X2" -> list.add(new InsnNode(DUP_X2));
			case "DUP2" -> list.add(new InsnNode(DUP2));
			case "DUP2_X1" -> list.add(new InsnNode(DUP2_X1));
			case "DUP2_X2" -> list.add(new InsnNode(DUP2_X2));
			case "SWAP" -> list.add(new InsnNode(SWAP));
			case "IADD" -> list.add(new InsnNode(IADD));
			case "LADD" -> list.add(new InsnNode(LADD));
			case "FADD" -> list.add(new InsnNode(FADD));
			case "DADD" -> list.add(new InsnNode(DADD));
			case "ISUB" -> list.add(new InsnNode(ISUB));
			case "LSUB" -> list.add(new InsnNode(LSUB));
			case "FSUB" -> list.add(new InsnNode(FSUB));
			case "DSUB" -> list.add(new InsnNode(DSUB));
			case "IMUL" -> list.add(new InsnNode(IMUL));
			case "LMUL" -> list.add(new InsnNode(LMUL));
			case "FMUL" -> list.add(new InsnNode(FMUL));
			case "DMUL" -> list.add(new InsnNode(DMUL));
			case "IDIV" -> list.add(new InsnNode(IDIV));
			case "LDIV" -> list.add(new InsnNode(LDIV));
			case "FDIV" -> list.add(new InsnNode(FDIV));
			case "DDIV" -> list.add(new InsnNode(DDIV));
			case "IREM" -> list.add(new InsnNode(IREM));
			case "LREM" -> list.add(new InsnNode(LREM));
			case "FREM" -> list.add(new InsnNode(FREM));
			case "DREM" -> list.add(new InsnNode(DREM));
			case "INEG" -> list.add(new InsnNode(INEG));
			case "LNEG" -> list.add(new InsnNode(LNEG));
			case "FNEG" -> list.add(new InsnNode(FNEG));
			case "DNEG" -> list.add(new InsnNode(DNEG));
			case "ISHL" -> list.add(new InsnNode(ISHL));
			case "LSHL" -> list.add(new InsnNode(LSHL));
			case "ISHR" -> list.add(new InsnNode(ISHR));
			case "LSHR" -> list.add(new InsnNode(LSHR));
			case "IUSHR" -> list.add(new InsnNode(IUSHR));
			case "LUSHR" -> list.add(new InsnNode(LUSHR));
			case "IAND" -> list.add(new InsnNode(IAND));
			case "LAND" -> list.add(new InsnNode(LAND));
			case "IOR" -> list.add(new InsnNode(IOR));
			case "LOR" -> list.add(new InsnNode(LOR));
			case "IXOR" -> list.add(new InsnNode(IXOR));
			case "LXOR" -> list.add(new InsnNode(LXOR));
			case "IINC" -> list.add(new IincInsnNode(IINC, Integer.parseInt(words[1])));
			case "I2L" -> list.add(new InsnNode(I2L));
			case "I2F" -> list.add(new InsnNode(I2F));
			case "I2D" -> list.add(new InsnNode(I2D));
			case "L2I" -> list.add(new InsnNode(L2I));
			case "L2F" -> list.add(new InsnNode(L2F));
			case "L2D" -> list.add(new InsnNode(L2D));
			case "F2I" -> list.add(new InsnNode(F2I));
			case "F2L" -> list.add(new InsnNode(F2L));
			case "F2D" -> list.add(new InsnNode(F2D));
			case "D2I" -> list.add(new InsnNode(D2I));
			case "D2L" -> list.add(new InsnNode(D2L));
			case "D2F" -> list.add(new InsnNode(D2F));
			case "I2B" -> list.add(new InsnNode(I2B));
			case "I2C" -> list.add(new InsnNode(I2C));
			case "I2S" -> list.add(new InsnNode(I2S));
			case "LCMP" -> list.add(new InsnNode(LCMP));
			case "FCMPL" -> list.add(new InsnNode(FCMPL));
			case "FCMPG" -> list.add(new InsnNode(FCMPG));
			case "DCMPL" -> list.add(new InsnNode(DCMPL));
			case "DCMPG" -> list.add(new InsnNode(DCMPG));
			case "IFEQ" -> list.add(new JumpInsnNode(IFEQ, getLabel(words[1], labels, vanillaLabels)));
			case "IFNE" -> list.add(new JumpInsnNode(IFNE, getLabel(words[1], labels, vanillaLabels)));
			case "IFLT" -> list.add(new JumpInsnNode(IFLT, getLabel(words[1], labels, vanillaLabels)));
			case "IFGE" -> list.add(new JumpInsnNode(IFGE, getLabel(words[1], labels, vanillaLabels)));
			case "IFGT" -> list.add(new JumpInsnNode(IFGT, getLabel(words[1], labels, vanillaLabels)));
			case "IFLE" -> list.add(new JumpInsnNode(IFLE, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ICMPEQ" -> list.add(new JumpInsnNode(IF_ICMPEQ, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ICMPNE" -> list.add(new JumpInsnNode(IF_ICMPNE, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ICMPLT" -> list.add(new JumpInsnNode(IF_ICMPLT, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ICMPGE" -> list.add(new JumpInsnNode(IF_ICMPGE, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ICMPGT" -> list.add(new JumpInsnNode(IF_ICMPGT, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ICMPLE" -> list.add(new JumpInsnNode(IF_ICMPLE, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ACMPEQ" -> list.add(new JumpInsnNode(IF_ACMPEQ, getLabel(words[1], labels, vanillaLabels)));
			case "IF_ACMPNE" -> list.add(new JumpInsnNode(IF_ACMPNE, getLabel(words[1], labels, vanillaLabels)));
			case "GOTO" -> list.add(new JumpInsnNode(GOTO, getLabel(words[1], labels, vanillaLabels)));
			case "JSR" -> list.add(new JumpInsnNode(JSR, getLabel(words[1], labels, vanillaLabels)));
			case "RET" -> list.add(new VarInsnNode(RET, Integer.parseInt(words[1])));
			case "TABLESWITCH" -> list.add(new TableSwitchInsnNode(Integer.parseInt(words[1].replaceAll("range\\[|]", "").split(":")[0]), Integer.parseInt(words[1].replaceAll("range\\[|]", "").split(":")[1]), getLabel(words[3].replaceAll("default\\[", "").replaceAll("]", ""), labels, vanillaLabels), getLabels(words[2], labels, vanillaLabels)));
			case "LOOKUPSWITCH" -> list.add(new LookupSwitchInsnNode(getLabel(words[2].replaceAll("default\\[", "").replaceAll("]", ""), labels, vanillaLabels), getLookupKeys(words[1]), getLabelKeys(words[1], labels, vanillaLabels)));
			case "IRETURN" -> list.add(new InsnNode(IRETURN));
			case "LRETURN" -> list.add(new InsnNode(LRETURN));
			case "FRETURN" -> list.add(new InsnNode(FRETURN));
			case "DRETURN" -> list.add(new InsnNode(DRETURN));
			case "ARETURN" -> list.add(new InsnNode(ARETURN));
			case "RETURN" -> list.add(new InsnNode(RETURN));
			case "GETSTATIC" -> list.add(new FieldInsnNode(GETSTATIC, words[1].split("\\.")[0], words[1].split("\\.")[1], words[2]));
			case "PUTSTATIC" -> list.add(new FieldInsnNode(PUTSTATIC, words[1].split("\\.")[0], words[1].split("\\.")[1], words[2]));
			case "GETFIELD" -> list.add(new FieldInsnNode(GETFIELD, words[1].split("\\.")[0], words[1].split("\\.")[1], words[2]));
			case "PUTFIELD" -> list.add(new FieldInsnNode(PUTFIELD, words[1].split("\\.")[0], words[1].split("\\.")[1], words[2]));
			case "INVOKEVIRTUAL" -> list.add(new MethodInsnNode(INVOKEVIRTUAL, words[1].split("[.(]")[0], words[1].split("[.(]")[1], getDescriptor(words[1])));
			case "INVOKESPECIAL" -> list.add(new MethodInsnNode(INVOKESPECIAL, words[1].split("[.(]")[0], words[1].split("[.(]")[1], getDescriptor(words[1])));
			case "INVOKESTATIC" -> list.add(new MethodInsnNode(INVOKESTATIC, words[1].split("[.(]")[0], words[1].split("[.(]")[1], getDescriptor(words[1])));
			case "INVOKESTATIC_itf" -> list.add(new MethodInsnNode(INVOKESTATIC, words[1].split("[.(]")[0], words[1].split("[.(]")[1], getDescriptor(words[1]), true));
			case "INVOKEINTERFACE" -> list.add(new MethodInsnNode(INVOKEINTERFACE, words[1].split("[.(]")[0], words[1].split("[.(]")[1], getDescriptor(words[1])));
			case "INVOKEDYNAMIC" -> list.add(new MethodInsnNode(INVOKEDYNAMIC, words[1].split("[.(]")[0], words[1].split("[.(]")[1], getDescriptor(words[1])));
			case "NEW" -> list.add(new TypeInsnNode(NEW, words[1]));
			case "NEWARRAY" -> list.add(new IntInsnNode(NEWARRAY, getArrayType(words[1])));
			case "ANEWARRAY" -> list.add(new TypeInsnNode(ANEWARRAY, words[1]));
			case "ARRAYLENGTH" -> list.add(new InsnNode(ARRAYLENGTH));
			case "ATHROW" -> list.add(new InsnNode(ATHROW));
			case "CHECKCAST" -> list.add(new TypeInsnNode(CHECKCAST, words[1]));
			case "INSTANCEOF" -> list.add(new TypeInsnNode(INSTANCEOF, words[1]));
			case "MONITORENTER" -> list.add(new InsnNode(MONITORENTER));
			case "MONITOREXIT" -> list.add(new InsnNode(MONITOREXIT));
			case "MULTIANEWARRAY" -> list.add(new MultiANewArrayInsnNode(words[1], Integer.parseInt(words[2])));
			case "IFNULL" -> list.add(new JumpInsnNode(IFNULL, getLabel(words[1], labels, vanillaLabels)));
			case "IFNONNULL" -> list.add(new JumpInsnNode(IFNONNULL, getLabel(words[1], labels, vanillaLabels)));
			case "LINE" -> list.add(new LineNumberNode(Integer.parseInt(words[2]), getLabel(words[1], labels, vanillaLabels)));
			default -> {
				if (words[0].matches("[A-Za-z]+:")) {
					list.add(labels.get(words[0].substring(0, words[0].length() - 1)));
				}
			}
		}
	}
	
	private static int getArrayType(String str) {
		return switch (str) {
			case "Z" -> T_BOOLEAN;
			case "C" -> T_CHAR;
			case "F" -> T_FLOAT;
			case "D" -> T_DOUBLE;
			case "B" -> T_BYTE;
			case "S" -> T_SHORT;
			case "I" -> T_INT;
			case "J" -> T_LONG;
			default -> throw new IllegalStateException("Unexpected value: " + str);
		};
	}
	
	private static String getDescriptor(String str) {
		return str.substring(str.indexOf('('));
	}
	
	private static LabelNode[] getLabels(String str, Map<String, LabelNode> labels, Map<String, LabelNode> vanillaLabels) {
		String[] strings = str.replaceAll("labels\\[|]", "").split(", ");
		LabelNode[] nodes = new LabelNode[strings.length];
		for (int i = 0; i < strings.length; i++) {
			nodes[i] = getLabel(strings[i], labels, vanillaLabels);
		}
		return nodes;
	}
	
	private static LabelNode[] getLabelKeys(String str, Map<String, LabelNode> labels, Map<String, LabelNode> vanillaLabels) {
		str = str.substring(8, str.length() - 1).replaceAll("[0-9]+=", "");
		String[] strings = str.split(", ");
		LabelNode[] keys = new LabelNode[strings.length];
		for (int i = 0; i < strings.length; i++) {
			keys[i] = getLabel(strings[i], labels, vanillaLabels);
		}
		return keys;
	}
	
	private static int[] getLookupKeys(String str) {
		str = str.substring(8, str.length() - 1).replaceAll("=[A-Za-z]+", "");
		String[] strings = str.split(", ");
		int[] keys = new int[strings.length];
		for (int i = 0; i < strings.length; i++) {
			keys[i] = Integer.parseInt(strings[i]);
		}
		return keys;
	}
	
	private static Object getLdc(String str) {
		if (str.equals("\"\"")) {
			return "";
		} else if (str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1).substring(0, str.length() - 2);
		} else if (str.endsWith("f")) {
			return Float.parseFloat(str.substring(0, str.length() - 1));
		} else if (str.endsWith("d") || str.indexOf('.') > -1) {
			return Double.parseDouble(str.substring(0, str.length() - 1));
		} else {
			return Integer.parseInt(str);
		}
	}
	
	private static class NodeNotFoundException extends IllegalStateException {
		public NodeNotFoundException() {
			super();
		}
		
		public NodeNotFoundException(String s) {
			super(s);
		}
		
		public NodeNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public NodeNotFoundException(Throwable cause) {
			super(cause);
		}
	}
}
