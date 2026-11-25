plugins {
    id("net.fabricmc.fabric-loom")
}

fun prop(key: String): String = sc.properties[key]

val minecraftVersion = sc.current.version
version = prop("mod.version")

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // Compile-only: exact SkyHanni types for the optional price-patch mixin.
    // Not shipped, not a runtime dep (see shpricefix.mixins.json, required=false).
    // A local jar (libs/, not in git) wins when a version names one and it is present.
    val localSkyhanni = sc.properties.getOrNull<String>("deps.skyhanni_jar")
        ?.let { rootProject.file("libs/$it") }
        ?.takeIf { it.isFile }
    if (localSkyhanni != null) {
        logger.lifecycle("$minecraftVersion: compiling against ${localSkyhanni.name}")
        compileOnly(files(localSkyhanni))
    } else {
        compileOnly("maven.modrinth:skyhanni:${prop("deps.skyhanni")}") {
            isTransitive = false
        }
    }
}

loom {
    runConfigs.named("client") {
        generateRunConfig = true
        // Shared between versions
        runDir = "../../run"
        jvmArguments.addAll(
            "-Dmixin.debug.export=true",
            "-XX:+IgnoreUnrecognizedVMOptions",
            "-XX:+AllowEnhancedClassRedefinition"
        )
        jvmArguments.add(provider {
            "-javaagent:${configurations.compileClasspath.get().find { it.name.contains("sponge-mixin") }}"
        })
    }

    runConfigs.named("server") {
        generateRunConfig = false
    }
}

tasks {
    processResources {
        val props = mapOf(
            "mod_id" to prop("mod.id"),
            "mod_name" to prop("mod.name"),
            "mod_description" to prop("mod.description"),
            "mod_version" to prop("mod.version"),
            "minecraft_version_range" to prop("mod.mc_compat"),
            "loader_version" to prop("deps.fabric_loader"),
            "fabric_api_version" to prop("deps.fabric_api"),
        )
        inputs.properties(props)
        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }

    // Collects every version's jar into the root build/libs
    register<Copy>("collectJar") {
        group = "build"
        from(jar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs"))
    }

    build {
        finalizedBy("collectJar")
    }
}

base {
    archivesName.set(prop("mod.id"))
}

tasks.withType<AbstractArchiveTask>().configureEach {
    archiveVersion.set("${project.version}-$minecraftVersion")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}
