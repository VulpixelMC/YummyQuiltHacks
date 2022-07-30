package net.cursedmc.yqh.api.util;

import org.quiltmc.loader.api.QuiltLoader;

public class MapUtils {
	/**
	 * Allows you to easily get the mapped class name in any environment.
	 * @param name the class name
	 * @return the corresponding mapping
	 */
	public static String mappedClass(final String name) {
		return QuiltLoader.getMappingResolver().mapClassName("named", name);
	}

	/**
	 * Allows you to easily get the mapped method name in any environment.
	 * This method also only returns the method's name.
	 * @param owner the class the method is in
	 * @param name the method name
	 * @param descriptor the method descriptor
	 * @return the corresponding mapping
	 */
	public static String mappedMethod(final String owner, final String name, final String descriptor) {
		return QuiltLoader.getMappingResolver().mapMethodName("named", owner, name, descriptor);
	}

	/**
	 * Allows you to easily get the mapped field name in any environment.
	 * @param owner the class the field is in
	 * @param name the field name
	 * @param descriptor the field descriptor
	 * @return the corresponding mapping
	 */
	public static String mappedField(final String owner, final String name, final String descriptor) {
		return QuiltLoader.getMappingResolver().mapFieldName("named", owner, name, descriptor);
	}

	/**
	 * Allows you to easily get the mapped field name in any environment.
	 * This method also only returns the field's name.
	 * @param owner the class the field is in
	 * @param name the field name
	 * @param descriptor the field descriptor
	 * @return the corresponding mapping
	 * @see MapUtils#mappedField(String owner, String name, String descriptor)
	 */
	public static String mappedFieldName(final String owner, final String name, final String descriptor) {
		final String[] split = mappedField(owner, name, descriptor).split("\\.");
		return split[split.length - 1];
	}
}
