plugins {
    id("androidx.room")
    id("com.google.devtools.ksp")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    "implementation"(library("androidx-room-runtime"))
    "implementation"(library("androidx-room-ktx"))
    "ksp"(library("androidx-room-compiler"))
    "testImplementation"(library("androidx-room-testing"))
    "androidTestImplementation"(library("androidx-room-testing"))
}
