package net.cursedmc.yqh.impl.manifest;

import com.jsoniter.annotation.JsonProperty;

import java.util.Map;

public class YummyManifest {
	@JsonProperty(value = "schema_version", required = true)
	public int schemaVersion;
	@JsonProperty(required = true)
	public Map<String, String> entrypoints;
}
