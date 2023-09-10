package net.cursedmc.yqh.impl.manifest;

import com.jsoniter.annotation.JsonProperty;

public class YummyManifestSchema {
	@JsonProperty(value = "schema_version", required = true)
	public int schemaVersion;
}
