import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev686"
    kotlin("plugin.serialization") version "1.6.21"
    id("com.android.library")
    id("kotlin-parcelize")

}

group = "me.rahim"
version = "1.0"

repositories {
    google()
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
}
val ktor_version = "1.6.3"

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.runtime)
                api(compose.materialIconsExtended)
                api(compose.ui)
                api("com.russhwolf:multiplatform-settings:0.8.1")
                implementation("com.russhwolf:multiplatform-settings-serialization:0.8.1")
                api("com.russhwolf:multiplatform-settings-no-arg:0.8")

                api("io.ktor:ktor-client-core:$ktor_version")
                api("io.ktor:ktor-client-serialization:$ktor_version")
                api("com.arkivanov.decompose:decompose:0.3.1")
                api("com.arkivanov.decompose:extensions-compose-jetbrains:0.3.1")

                implementation("io.arrow-kt:arrow-fx-coroutines:1.0.0")
                implementation("io.arrow-kt:arrow-fx-stm:1.0.0")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val commonJvmAndroid = create("commonJvmAndroid") {
            dependsOn(commonMain)
            dependencies {
                api("com.github.xdbfoundation:java-digitalbits-sdk:0.27.0")
//                api("com.moandjiezana.toml:toml4j:0.7.2")
                api("com.google.zxing:core:3.4.1")
                api("com.github.kittinunf.fuel:fuel:2.3.1")
//                api("org.ktorm:ktorm-core:3.4.1")
//                api("org.ktorm:ktorm-support-sqlite:3.4.1")
//                api("org.xerial:sqlite-jdbc:3.36.0")
            }

        }
        val androidMain by getting {
            dependsOn(commonJvmAndroid)
            dependencies {
//                api("com.google.zxing:android-integration:3.4.1")
                api("com.github.xdbfoundation:java-digitalbits-sdk:0.27.0")
                api("androidx.appcompat:appcompat:1.4.1")
                api("androidx.core:core-ktx:1.7.0")
                implementation("io.ktor:ktor-client-android:$ktor_version")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13")
            }
        }
        val desktopMain by getting {
            dependsOn(commonJvmAndroid)
            dependencies {
                implementation(compose.desktop.currentOs)
                api("com.google.zxing:javase:3.4.1")
                implementation("io.ktor:ktor-client-cio-jvm:$ktor_version")
                api("com.github.xdbfoundation:java-digitalbits-sdk:0.27.0")

            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation("androidx.compose:compose-runtime:0.1.0-dev14")
}
