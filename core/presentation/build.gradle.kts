plugins {
    id("notez.android.library")
    id("notez.compose")
}

android {
    namespace = "com.example.notezapp.core.presentation"
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
}
