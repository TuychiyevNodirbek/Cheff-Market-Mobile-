plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    // UNVERIFIED on this machine: Kotlin/Native's iOS backend only runs on macOS, so these
    // targets have never been compiled - see docs/ios-phase-plan.md. Written from the Gradle
    // DSL docs, not from a successful build.
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.multiplatform.settings)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.multiplatform.settings.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.yandex.mapkit)
            implementation(libs.ktor.client.okhttp)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlin.test)
            }
        }
        // iosMain exists via the default Kotlin hierarchy template only when the iOS
        // targets above actually register, which Kotlin/Native limits to macOS hosts -
        // guard the lookup so Windows/Linux configuration doesn't fail.
        findByName("iosMain")?.apply {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "uz.nodirbek.receiptdelivery.shared.resources"
}

android {
    namespace = "uz.nodirbek.receiptdelivery.shared"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}
