group = "ltd.mbor"
version = "0.5.0-SNAPSHOT"

val ktorVersion = "3.0.0-wasm2"
val bignumVersion = "0.3.9"

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.3.0")
  }
}

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.kotlinx.kover") version "0.6.1"
  id("org.jetbrains.kotlin.android") apply false
  id("com.android.library") version "7.4.0"
  id("maven-publish")
}

repositories {
  google()
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }
  js(IR) {
    browser {
      commonWebpackConfig {
        cssSupport {
          enabled.set(true)
        }
      }
    }
  }
  wasmJs()
//  val hostOs = System.getProperty("os.name")
//  val isMingwX64 = hostOs.startsWith("Windows")
//  val nativeTarget = when {
//    hostOs == "Mac OS X" -> macosX64("native")
//    hostOs == "Linux" -> linuxX64("native")
//    isMingwX64 -> mingwX64("native")
//    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//  }
  
  androidTarget {
    publishLibraryVariants("release", "debug")
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
        api("com.ionspin.kotlin:bignum:$bignumVersion")
        api("com.ionspin.kotlin:bignum-serialization-kotlinx:$bignumVersion")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
        implementation("io.ktor:ktor-client-core:$ktorVersion")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
      }
    }
    val jvmTest by getting
    val jsMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-js:$ktorVersion")
      }
    }
    val jsTest by getting
//    val nativeMain by getting{
//      dependencies {
//        implementation("io.ktor:ktor-client-curl:$ktorVersion")
//      }
//    }
//    val nativeTest by getting
    val androidMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
      }
    }
    val androidUnitTest by getting {
      dependencies {
        implementation("junit:junit:4.13.2")
      }
    }
  }
}

android {
  compileSdk = 33
  namespace = "ltd.mbor.minimak"
  defaultConfig {
    minSdk = 21
    targetSdk = 33
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/mihbor/MinimaK")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}
