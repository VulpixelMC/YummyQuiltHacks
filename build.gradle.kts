@file:Suppress("UnstableApiUsage")

plugins {
	java
	alias(libs.plugins.quilt.loom)
	`maven-publish`
	checkstyle
}

val modVersion: String by project
val mavenGroup: String by project
val modId: String by project

var hasCopied = false

version = modVersion
group = mavenGroup

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
	mavenCentral()

	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/")
	}

	maven {
		name = "Modrinth"
		url = uri("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}

	maven {
		name = "auoeke Maven"
		url = uri("https://maven.auoeke.net")
	}

	maven {
		name = "ENDERZOMBI102 Maven"
		url = uri("https://repsy.io/mvn/enderzombi102/mc")
	}
}


val modImplementationInclude by configurations.register("modImplementationInclude")

// All the dependencies are declared at gradle/libs.version.toml and referenced with "libs.<id>"
// See https://docs.gradle.org/current/userguide/platforms.html for information on how version catalogs work.
dependencies {
	minecraft(libs.minecraft)
	mappings(loom.layered {
		addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:${libs.versions.quilt.mappings.get()}:v2"))
		// officialMojangMappings() // Uncomment if you want to use Mojang mappings as your primary mappings, falling back on QM for parameters and Javadocs
	})
	modImplementation(libs.quilt.loader)

	@Suppress("UnstableApiUsage")
	modImplementationInclude(libs.qsl.base)

	modImplementationInclude("org.ow2.asm", "asm-commons", "9.3")
	modImplementationInclude("net.auoeke", "reflect", "5.+")
	modImplementationInclude("net.gudenau.lib", "unsafe", "latest.release")
	modImplementationInclude("com.enderzombi102", "EnderLib", "0.2.0")
	modImplementationInclude("net.bytebuddy", "byte-buddy-agent", "1.12.+")

	modRuntimeOnly("com.terraformersmc", "modmenu", "4.0.0")
	modRuntimeOnly("maven.modrinth", "wthit", "fabric-5.4.3")
	modRuntimeOnly("maven.modrinth", "badpackets", "fabric-0.1.2")
	modRuntimeOnly("maven.modrinth", "emi", "0.2.0+1.19")

	// QSL is not a complete API; You will need Quilted Fabric API to fill in the gaps.
	// Quilted Fabric API will automatically pull in the correct QSL version.
	modRuntimeOnly(libs.quilted.fabric.api)

	annotationProcessor("net.auoeke:uncheck:latest.release")

	add(sourceSets.main.get().getTaskName("mod", JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME), modImplementationInclude)
	add(net.fabricmc.loom.util.Constants.Configurations.INCLUDE, modImplementationInclude)
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("quilt.mod.json") {
		expand("group" to group, "id" to modId, "version" to version)
	}

	from("src/main/java") {
		include("**/LICENSE")
	}

	dependsOn("copyAgentJar")
}

tasks.register<Copy>("copyAgentJar") {
	this.destinationDir = tasks.processResources.get().destinationDir

	dependsOn(":agent:jar")

	from(project(":agent").tasks.jar.get().archiveFile) {
		rename { "yummy_agent.jar" }
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	// Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
	options.release.set(17)
}

java {
	// Still required by IDEs such as Eclipse and Visual Studio Code
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17

	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	// If this mod is going to be a library, then it should also generate Javadocs in order to aid with development.
	// Uncomment this line to generate them.
	// withJavadocJar()
}

// If you plan to use a different file for the license, don't forget to change the file name here!
tasks.withType<AbstractArchiveTask> {
	archiveBaseName.set(modId)
	from("LICENSE") {
		rename { "${it}_$modId" }
	}
	dependsOn("checkstyleMain")
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			this.artifactId = modId
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}

tasks.withType<Checkstyle>().configureEach {
	configFile = File("${rootDir}/checkstyle.xml")
}
