plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.ksp)
}

android {
    namespace = "me.easynap"
    compileSdk = 37
    defaultConfig {
        applicationId = "me.easynap"
        minSdk = 26
        targetSdk = 37
        versionCode = 1005
        versionName = "1.5"
        testInstrumentationRunner = "me.easynap.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    testOptions {
      unitTests {
        isIncludeAndroidResources = true
      }
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.activity.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  debugImplementation(libs.androidx.compose.ui.tooling)

  // Hilt
  implementation(libs.hilt.android)
  ksp(libs.hilt.android.compiler)

  // Local tests
  testImplementation(libs.junit)
  testImplementation(libs.androidx.datastore.preferences.core)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.hilt.android.testing)
  kspTest(libs.hilt.android.compiler)

  // Instrumented tests
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.hilt.android.testing)
  kspAndroidTest(libs.hilt.android.compiler)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
