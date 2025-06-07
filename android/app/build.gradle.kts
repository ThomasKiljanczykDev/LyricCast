/*
 * Created by Tomasz Kiljanczyk on 03/01/2022, 23:17
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 03/01/2022, 23:13
 */

import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.navigationSafeArgs)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.google.googleServices)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.compose.compiler)
}

android {
    val major = 1
    val minor = 1
    val patch = 0
    // used for hotfix, alpha, beta, etc. versions
    val revision = 2

    defaultConfig {
        applicationId = "dev.thomas_kiljanczyk.lyriccast"
        minSdk = 27
        compileSdk = 35
        targetSdk = 35

        // Versioning
        // Max version code is 2,100,000,000
        // Version code is calculated as follows:
        // revision - up to 99
        // patch - up to 999
        // minor - up to 999
        // major - up to 21
        versionCode = major * 100_000_000 + minor * 100_000 + patch * 100 + revision
        versionName = "$major.$minor.$patch${if (revision > 0) ".$revision" else ""}"

        testInstrumentationRunner = "dev.thomas_kiljanczyk.lyriccast.HiltTestRunner"
        androidResources {
            localeFilters.addAll(listOf("en", "pl"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        animationsDisabled = true

        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
    namespace = "dev.thomas_kiljanczyk.lyriccast"
}

dependencies {
    // Submodules
    implementation(project(":common"))
    implementation(project(":data-transfer"))
    implementation(project(":data-model"))

    // App dependencies
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.services.nearby)

    // Architecture Components
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javaLite)
    implementation(libs.protobuf.kotlinLite)

    // AndroidX
    implementation(libs.android.material)
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.recyclerView)
    implementation(libs.androidx.recyclerViewSelection)
    implementation(libs.androidx.navigationFragmentKtx)
    implementation(libs.androidx.navigationUiKtx)

    // Chromecast
    implementation(libs.google.castFramework)
    implementation(libs.androidx.mediaRouter)

    // Hilt
    implementation(libs.hilt)
    ksp(libs.hiltCompiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlyticsKtx)
    implementation(libs.firebase.analyticsKtx)

    // AndroidX Test - Instrumented testing
    androidTestImplementation(libs.androidx.test.coreKtx)
    androidTestImplementation(libs.androidx.test.extJunit)
    androidTestImplementation(libs.androidx.test.extJunitKtx)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.espressoContrib) {
        // TODO: nice to have - workaround for protobuf-lite test issues, try to remove it in the future
        // Source: https://stackoverflow.com/questions/66154727/java-lang-nosuchmethoderror-no-static-method-registerdefaultinstance-with-fireb
        exclude(module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.rules)

    // AndroidX Test - Hilt testing
    androidTestImplementation(libs.hiltTesting)
    kspAndroidTest(libs.hiltCompiler)

    // LeakCanary
//    debugImplementation(libs.squareup.leakCanary)

    // Compose dependencies
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.compose.hilt)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Other dependencies
    implementation(libs.apache.commonsLang)
    implementation(libs.zip4j)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

// A fix for protobuf ksp issues
// Source: https://github.com/google/ksp/issues/1590
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val capName = variant.name.replaceFirstChar { it.uppercase() }
            tasks.getByName<KotlinCompile>("ksp${capName}Kotlin") {
                setSource(tasks.getByName("generate${capName}Proto").outputs)
            }
        }
    }
}