import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.github.takahirom.roborazzi")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val openRouterApiKey = localProperties.getProperty("OPENROUTER_API_KEY")
    ?: System.getenv("OPENROUTER_API_KEY")
    ?: ""
val openRouterModel = localProperties.getProperty("OPENROUTER_MODEL")
    ?: System.getenv("OPENROUTER_MODEL")
    ?: "openrouter/free"
val openRouterBaseUrl = localProperties.getProperty("OPENROUTER_BASE_URL")
    ?: "https://openrouter.ai/api/v1/"

android {
    namespace = "com.qoder.clawagent"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.qoder.clawagent"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.3"

        buildConfigField("String", "OPENROUTER_API_KEY", openRouterApiKey.asBuildConfigString())
        buildConfigField("String", "OPENROUTER_MODEL", openRouterModel.asBuildConfigString())
        buildConfigField("String", "OPENROUTER_BASE_URL", openRouterBaseUrl.asBuildConfigString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/INDEX.LIST",
            "/META-INF/DEPENDENCIES",
        )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware"
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.aallam.openai:openai-client:4.1.0")
    implementation("io.ktor:ktor-client-okhttp:3.0.0")

    implementation("com.google.adk:google-adk-kotlin-core:1.4.0")
    implementation("com.google.adk:google-adk-kotlin-integrations-openai:1.4.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.77.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.77.0")
    testImplementation("org.robolectric:robolectric:4.17")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.test:core-ktx:1.6.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
