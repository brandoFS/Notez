plugins {
    id("notez.android.library")
    id("notez.compose")
    id("notez.koin")
    id("notez.kotlin.serialization")
}

dependencies {
    "implementation"(library("androidx-lifecycle-runtime-ktx"))
    "implementation"(library("androidx-lifecycle-runtime-compose"))
    "implementation"(library("androidx-lifecycle-viewmodel-compose"))
    "implementation"(library("androidx-navigation-compose"))
    "implementation"(library("kotlinx-coroutines-core"))
    "implementation"(library("kotlinx-coroutines-android"))

    "testImplementation"(library("junit-jupiter-api"))
    "testRuntimeOnly"(library("junit-jupiter-engine"))
    // Gradle 9 no longer puts the launcher on the test runtime classpath implicitly.
    "testRuntimeOnly"(library("junit-platform-launcher"))
    "testImplementation"(library("junit-jupiter-params"))
    "testImplementation"(library("turbine"))
    "testImplementation"(library("assertk"))
    "testImplementation"(library("kotlinx-coroutines-test"))
}
