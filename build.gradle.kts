val ktorVersion = "2.2.2"
val bignumVersion = "0.3.7"

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

group = "ltd.mbor"
version = "0.3.3-SNAPSHOT"

repositories {
  google()
  mavenCentral()
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
        cssSupport { enabled = true }
      }
    }
  }
//  val hostOs = System.getProperty("os.name")
//  val isMingwX64 = hostOs.startsWith("Windows")
//  val nativeTarget = when {
//    hostOs == "Mac OS X" -> macosX64("native")
//    hostOs == "Linux" -> linuxX64("native")
//    isMingwX64 -> mingwX64("native")
//    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//  }
  
  android {
    publishLibraryVariants("release", "debug")
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        api("io.ktor:ktor-client-core:$ktorVersion")
    
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    
        api("com.ionspin.kotlin:bignum:$bignumVersion")
        api("com.ionspin.kotlin:bignum-serialization-kotlinx:$bignumVersion")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
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
    val androidTest by getting {
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
