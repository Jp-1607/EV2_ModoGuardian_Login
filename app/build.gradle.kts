plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.example.vicente_parrra"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vicente_parrra"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        val apiUrl = providers.gradleProperty("insertCodeApiUrl").orElse("http://10.0.2.2:8080/").get()
        require(apiUrl.endsWith("/") && (apiUrl.startsWith("https://") ||
            Regex("""http://(10\.0\.2\.2|127\.0\.0\.1):[0-9]+/""").matches(apiUrl)) &&
            apiUrl.none { it == '"' || it == '\\' || it.isWhitespace() })
        buildConfigField("String", "API_URL", "\"$apiUrl\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val agentePruebas by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}

dependencies {
    agentePruebas("net.bytebuddy:byte-buddy-agent:1.14.17")
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    useJUnitPlatform()
    jvmArgumentProviders.add(org.gradle.process.CommandLineArgumentProvider {
        listOf("-javaagent:${agentePruebas.singleFile.absolutePath}")
    })
}

