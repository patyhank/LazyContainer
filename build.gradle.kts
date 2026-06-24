plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    alias(origamiLibs.plugins.origami)
    alias(libs.plugins.run.paper)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenLocal()
    mavenCentral()
    maven("https://repo.konjactw.dev/releases/")
    maven("https://repo.xenondevs.xyz/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(origamiLibs.mixin)
    compileOnly(origamiLibs.mixinextras)
    implementation(libs.kotlin.stdlib)
    implementation("org.ow2.asm:asm:9.9.1")
    implementation("org.ow2.asm:asm-tree:9.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}

origami {
    paperDevBundle("${libs.versions.minecraft.get()}.build.+")
    pluginId = "lazy-container"
}

runPaper {
    disablePluginJarDetection()
}

kotlin {
    jvmToolchain(25)
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.version,
            "mcVersion" to libs.versions.minecraft.get(),
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
