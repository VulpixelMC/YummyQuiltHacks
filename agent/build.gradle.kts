plugins {
	java
}

base.archivesBaseName = "agent"
group = "net.cursedmc"
version = "0.1"

repositories {
	mavenLocal()
	mavenCentral()

	maven {
		name = "auoeke Maven"
		url = uri("https://maven.auoeke.net")
	}
}

dependencies {
	annotationProcessor("net.auoeke:uncheck:latest.release")
}

tasks.jar {
	manifest {
		attributes(
			"Agent-Class" to "net.cursedmc.yqh.impl.instrumentation.MusicAgent",
			"Can-Redefine-Classes" to "true",
			"Can-Retransform-Classes" to "true",
		)
	}
}
