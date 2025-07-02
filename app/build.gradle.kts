import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
} else {
    throw GradleException("local.properties file not found!")
}

val azureKey = localProperties.getProperty("AZURE_KEY")
    ?: throw GradleException("AZURE_KEY not found in local.properties")
android {
    buildFeatures {
        buildConfig = true
    }
    namespace = "com.example.hci_test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.hci_test"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "AZURE_KEY", "\"$azureKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.okhttp)
    implementation(libs.glide)
    implementation (libs.material)
    implementation(libs.org.json)

}