plugins {
    id("notez.jvm.library")
}

dependencies {
    api(project(":core:domain"))
    api(libs.kotlinx.coroutines.core)
}
