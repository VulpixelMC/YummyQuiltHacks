package net.cursedmc.yqh.api.entrypoints;

/**
 * This entrypoint is run before the loader is run.<br>
 * Do your loader transformations here.
 */
public interface PreLoader {
	/**
	 * Runs before the loader.
	 * @see PreLoader
	 */
	void onPreLoader();
}
