/*
 * Created by Tomasz Kiljanczyk on 03/01/2022, 23:12
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 03/01/2022, 23:12
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        minSdk = 27
        compileSdk = 35
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    namespace = "dev.thomas_kiljanczyk.datatransfer"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    // Submodules
    implementation(project(":common"))
}