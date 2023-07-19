import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.util.Properties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun releaseTime(): String {
    val dateFormat = SimpleDateFormat("yy.MMddHH")
    dateFormat.timeZone = TimeZone.getTimeZone("GMT+8")
    return dateFormat.format(Date())
}

// 秒时间戳
fun buildTime(): Long {
    return Date().time / 1000
}

fun executeCommand(command: String): String {
    val process = Runtime.getRuntime().exec(command)
    process.waitFor()
    val output = process.inputStream.bufferedReader().use { it.readText() }
    return output.trim()
}

val name = "ImageProcessor"
val version = "1.${releaseTime()}"
val gitCommits: Int = executeCommand("git rev-list HEAD --count").trim().toInt()

android {
    namespace = "com.github.jing332.image_processor"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.github.jing332.image_processor"
        minSdk = 24
        targetSdk = 33
        versionCode = gitCommits
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // 写入构建 秒时间戳
        buildConfigField("long", "BUILD_TIME", "${buildTime()}")
    }


    signingConfigs {
        val pro = Properties()
        val input = FileInputStream(project.rootProject.file("local.properties"))
        pro.load(input)

        create("release") {
            storeFile = file(pro.getProperty("KEY_PATH"))
            storePassword = pro.getProperty("KEY_PASSWORD")
            keyAlias = pro.getProperty("ALIAS_NAME")
            keyPassword = pro.getProperty("ALIAS_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "_debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    android.applicationVariants.configureEach {
        outputs.configureEach {
            if (this is ApkVariantOutputImpl) outputFileName = "${name}-v${versionName}.apk"
        }
    }
}

dependencies {
    val accompanistVersion = "0.31.3-beta"
    implementation("com.google.accompanist:accompanist-systemuicontroller:${accompanistVersion}")
    implementation("com.google.accompanist:accompanist-navigation-animation:${accompanistVersion}")

    implementation("androidx.savedstate:savedstate:1.2.1")

    // 图片加载
    implementation("io.coil-kt:coil-compose:2.4.0")

    // 图片预览
    implementation("com.github.jvziyaoyao:ImageViewer:1.0.2-alpha.4")

    // 数据持久化
    implementation("com.github.FunnySaltyFish.ComposeDataSaver:data-saver:v1.1.5")


    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.1")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.navigation:navigation-compose:2.6.0")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}