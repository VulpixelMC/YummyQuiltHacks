package net.cursedmc.yqh.api.util;

import org.quiltmc.loader.api.QuiltLoader;

public class MapUtils {
	/**
	 * Allows you to easily get the mapped class name in any environment.
	 *
	 * @param name the class name
	 * @return the corresponding mapping
	 */
	public static String mappedClass(String name) {
		return QuiltLoader.getMappingResolver().mapClassName("named", name);
	}
	
	/**
	 * Allows you to easily get the mapped method name in any environment.
	 * This method also only returns the method's name.
	 *
	 * @param owner      the class the method is in
	 * @param name       the method name
	 * @param descriptor the method descriptor
	 * @return the corresponding mapping
	 */
	public static String mappedMethod(String owner, String name, String descriptor) {
		return QuiltLoader.getMappingResolver().mapMethodName("named", owner, name, descriptor);
	}
	
	/**
	 * Allows you to easily get the mapped field name in any environment.
	 *
	 * @param owner      the class the field is in
	 * @param name       the field name
	 * @param descriptor the field descriptor
	 * @return the corresponding mapping
	 */
	public static String mappedField(String owner, String name, String descriptor) {
		return QuiltLoader.getMappingResolver().mapFieldName("named", owner, name, descriptor);
	}
	
	/**
	 * Allows you to easily get the mapped field name in any environment.
	 * This method also only returns the field's name.
	 *
	 * @param owner      the class the field is in
	 * @param name       the field name
	 * @param descriptor the field descriptor
	 * @return the corresponding mapping
	 * @see MapUtils#mappedField(String owner, String name, String descriptor)
	 */
	public static String mappedFieldName(String owner, String name, String descriptor) {
		final String[] split = mappedField(owner, name, descriptor).split("\\.");
		return split[split.length - 1];
	}
}
