pluginManagement {
    repositories {
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
        google()
        mavenCentral()
        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Do not change the username below.
                // This should always be `mapbox` (not your username).
                username = "mapbox"
                // Use the secret token you stored in gradle.properties as the password
            password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").get()
                //password = "pk.eyJ1IjoiZC1nZW5lemlzLWIiLCJhIjoiY2xkbmY3ajg0MGFjMDNuczRiMHFoOXcyciJ9.QnOg16RA9xGp060-lIGOZA";
            }
        }

    }
}

rootProject.name = "My Application"
include(":app")
 