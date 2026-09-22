plugins {
    id("notez.android.feature")
}

android {
    namespace = "com.example.notezapp.notes.presentation"
}

dependencies {
    api(project(":feature:notes:domain"))
    api(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(libs.kotlinx.datetime)
}
