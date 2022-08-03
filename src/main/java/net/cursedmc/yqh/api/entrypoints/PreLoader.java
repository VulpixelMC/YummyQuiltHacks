package net.cursedmc.yqh.api.entrypoints;

/**
 * This entrypoint is run before the loader is run.<br>
 * <b>Warning</b>: Do not use Log4j here! This will cause the log to lose its color.
 */
public interface PreLoader {
	/**
	 * Runs before the loader.
	 * @see PreLoader
	 */
	void onPreLoader();
}
