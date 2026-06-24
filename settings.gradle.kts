dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://repo.konjactw.dev/releases/")
        mavenCentral()
    }
    
    versionCatalogs {
        create("origamiLibs") {
            from("xyz.xenondevs.origami:origami-catalog:0.4.1-SNAPSHOT")
        }
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        maven("https://repo.konjactw.dev/releases/")
        mavenCentral()
        gradlePluginPortal()
    }
}


rootProject.name = "LazyContainer"