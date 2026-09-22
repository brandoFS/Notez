plugins {
    id("notez.android.library")
    id("notez.room")
    id("notez.koin")
}

android {
    namespace = "com.example.notezapp.notes.data"
}

dependencies {
    api(project(":feature:notes:domain"))
    implementation(libs.kotlinx.coroutines.core)
}
