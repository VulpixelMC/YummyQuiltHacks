plugins {
	java
}

base.archivesName.set("agent")
group = "gay.sylv.vulpixel"
version = "0.1"

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains:annotations:24.0.1")
	
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
