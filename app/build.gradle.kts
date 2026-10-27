plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rutalista.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rutalista.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- RutaLista: librerías que agregamos nosotros ---

    // Retrofit: permite llamar a la API de OpenRouteService desde Kotlin
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Convertidor Gson: transforma el JSON que responde la API en clases de Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Corrutinas: permiten hacer la llamada a internet sin congelar la pantalla
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    // ViewModel para Compose: guarda el estado de la pantalla (capa ViewModel de MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // Navegación entre pantallas (la usa el colaborador  Diego)
    implementation("androidx.navigation:navigation-compose:2.8.5")
}