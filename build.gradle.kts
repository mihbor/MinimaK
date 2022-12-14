val kotlin_version: String by extra
val ktorVersion = "2.2.1"
val bignumVersion = "0.3.7"

buildscript {
  var kotlin_version: String by extra
  kotlin_version = "1.7.20"
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(kotlin("gradle-plugin", kotlin_version))
    classpath("com.android.tools.build:gradle:7.2.0")
  }
}

plugins {
  kotlin("multiplatform") version "1.7.20"
  kotlin("plugin.serialization") version "1.7.20"
  id("org.jetbrains.kotlin.android") version "1.7.20" apply false
  id("com.android.library") version "7.4.0"
  id("maven-publish")
}

group = "ltd.mbor"
version = "0.3-SNAPSHOT"

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
        implementation("io.ktor:ktor-client-core:$ktorVersion")
    
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    
        implementation("com.ionspin.kotlin:bignum:$bignumVersion")
        implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:$bignumVersion")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
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
        implementation("com.google.android.material:material:1.5.0")
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
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
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