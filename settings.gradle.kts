pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://repo.polyfrost.cc/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        fun register(loader: String, mcVersions: Iterable<String>) {
            for (mcVersion in mcVersions) {
                version("$mcVersion-$loader", mcVersion)
            }
        }

        register("fabric", listOf("1.21.1", "1.21.3", "1.21.4", "1.21.5"/*, "1.21.8", "1.21.10"*/))

        vcsVersion = "1.21.1-fabric"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "Lupin Loading Backgrounds (Stonecutter)"
