plugins {
    id("notez.android.application")
    id("notez.compose")
    id("notez.koin")
}

android {
    namespace = "com.example.notezapp"

    defaultConfig {
        applicationId = "com.example.notezapp"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core:design-system"))
    implementation(project(":core:presentation"))
    implementation(project(":feature:notes:data"))
    implementation(project(":feature:notes:presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
