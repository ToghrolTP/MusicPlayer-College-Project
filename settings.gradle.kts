pluginManagement {
    repositories {
        maven { url = uri("https://maven.myket.ir") }

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Add the mirror here first for libraries (Glide, etc.)
        maven { url = uri("https://maven.myket.ir") }

        google()
        mavenCentral()
    }
}

rootProject.name = "Music Player"
include(":app")