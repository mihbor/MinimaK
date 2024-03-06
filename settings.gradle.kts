pluginManagement {
  val kotlinVersion = extra["kotlin.version"] as String
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.namespace == "com.android" || requested.id.name == "kotlin-android-extensions") {
        useModule("com.android.tools.build:gradle:7.0.4")
      }
    }
  }
  plugins {
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.kotlin.android") version kotlinVersion
  }
}
rootProject.name = "minimak"
