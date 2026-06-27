plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.sanin.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sanin.tv"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "3.2.3"

        // QR relay server host — override in local.properties as:
        //   QR_RELAY_HOST=https://your-relay-server.example.com
        val qrRelayHost = (project.findProperty("QR_RELAY_HOST") as? String)
            ?: "https://2241d94d-0db8-4990-a504-6cb80fc982a7-00-24kdo01pjzeh5.janeway.replit.dev"
        buildConfigField("String", "QR_RELAY_HOST", "\"$qrRelayHost\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        create("alpha") {
            initWith(getByName("release"))
            applicationIdSuffix = ".alpha"
        }
    }

    flavorDimensions += "store"
    productFlavors {
        create("google") {
            dimension = "store"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn",
        )
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Network
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.dnsoverhttps)
    implementation(libs.retrofit)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // ExoPlayer / Media3
    implementation(libs.exoplayer)
    implementation(libs.exoplayer.ui)
    implementation(libs.exoplayer.dash)
    implementation(libs.exoplayer.hls)
    implementation(libs.exoplayer.session)

    // WorkManager
    implementation(libs.androidx.work.runtime)

    // DataStore
    implementation(libs.androidx.datastore)

    // Injekt
    implementation(libs.injekt.core)

    // Jsoup
    implementation(libs.jsoup)

    // DataStore
    implementation(libs.androidx.datastore)

    // Animated Bottom Bar
    implementation("nl.joery.animatedbottombar:library:1.1.0")

    // Groupie (RecyclerView)
    implementation("com.github.lisawray.groupie:groupie:2.10.1")
    implementation("com.github.lisawray.groupie:groupie-viewbinding:2.10.1")

    // Glide (backup image loader)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    // Lottie
    implementation("com.airbnb.android:lottie:6.6.0")

    // Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Swipe to refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // RxJava (used by tachiyomi extension layer)
    implementation("io.reactivex:rxjava:1.3.8")
    implementation("io.reactivex:rxandroid:1.2.1")

    // QR code generation (ZXing core — no scanner, no Activity)
    implementation("com.google.zxing:core:3.5.3")
}
