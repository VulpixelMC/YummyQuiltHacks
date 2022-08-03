package net.cursedmc.yqh.test;

import net.cursedmc.yqh.api.entrypoints.PreLoader;

public class YummyQuiltHacksPreLoader implements PreLoader {
	@Override
	public void onPreLoader() {
		System.out.println("Pre-loader entrypoint initialized!");
	}
}
