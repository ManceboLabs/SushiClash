import com.android.build.api.dsl.ApplicationExtension
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

data class ReleaseSigningValues(
    val storeFile: File,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

fun loadReleaseSigningValues(rootDir: File): ReleaseSigningValues? {
    val envStoreFile = System.getenv("RELEASE_STORE_FILE")?.trim().orEmpty()
    val envStorePassword = System.getenv("RELEASE_STORE_PASSWORD")
    val envKeyAlias = System.getenv("RELEASE_KEY_ALIAS")?.trim().orEmpty()
    val envKeyPassword = System.getenv("RELEASE_KEY_PASSWORD")

    if (envStoreFile.isNotEmpty()) {
        val missing = buildList {
            if (envStorePassword.isNullOrEmpty()) add("RELEASE_STORE_PASSWORD")
            if (envKeyAlias.isEmpty()) add("RELEASE_KEY_ALIAS")
            if (envKeyPassword.isNullOrEmpty()) add("RELEASE_KEY_PASSWORD")
        }
        require(missing.isEmpty()) {
            "Release signing env vars are incomplete. Missing: ${missing.joinToString()}"
        }

        return ReleaseSigningValues(
            storeFile = resolveStoreFile(rootDir, envStoreFile),
            storePassword = envStorePassword,
            keyAlias = envKeyAlias,
            keyPassword = envKeyPassword,
        )
    }

    val propertiesFile = rootDir.resolve("keystore.properties")
    if (!propertiesFile.exists()) {
        return null
    }

    val properties = Properties().apply {
        propertiesFile.inputStream().use(::load)
    }

    fun requiredProperty(name: String): String {
        return properties.getProperty(name)?.trim().orEmpty().also { value ->
            require(value.isNotEmpty()) {
                "Release signing property '$name' is missing in ${propertiesFile.name}"
            }
        }
    }

    return ReleaseSigningValues(
        storeFile = resolveStoreFile(rootDir, requiredProperty("storeFile")),
        storePassword = requiredProperty("storePassword"),
        keyAlias = requiredProperty("keyAlias"),
        keyPassword = requiredProperty("keyPassword"),
    )
}

fun resolveStoreFile(rootDir: File, configuredPath: String): File {
    val storeFile = File(configuredPath)
    return if (storeFile.isAbsolute) storeFile else rootDir.resolve(storeFile)
}

android {
    namespace = "com.mancebolabs.sushiclash"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mancebolabs.sushiclash"
        minSdk = 24
        targetSdk = 37
        versionCode = 7
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

val releaseSigningValues = loadReleaseSigningValues(rootProject.projectDir)
if (releaseSigningValues != null) {
    require(releaseSigningValues.storeFile.exists()) {
        "Release keystore not found at ${releaseSigningValues.storeFile.absolutePath}. " +
            "Create it locally or point RELEASE_STORE_FILE / keystore.properties to the correct path."
    }

    extensions.configure<ApplicationExtension>("android") {
        signingConfigs {
            create("release") {
                storeFile = releaseSigningValues.storeFile
                storePassword = releaseSigningValues.storePassword
                keyAlias = releaseSigningValues.keyAlias
                keyPassword = releaseSigningValues.keyPassword
            }
        }
        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

tasks.matching { it.name == "bundleRelease" || it.name == "assembleRelease" }.configureEach {
    doFirst {
        require(releaseSigningValues != null) {
            """
            Release signing is not configured.
            Create an upload keystore, copy keystore.properties.example to keystore.properties,
            and fill in storeFile/storePassword/keyAlias/keyPassword before building release artifacts.
            """.trimIndent()
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.org.json)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestUtil(libs.androidx.test.orchestrator)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
