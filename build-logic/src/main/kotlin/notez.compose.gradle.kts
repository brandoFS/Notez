import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

// Configured per concrete extension type rather than via CommonExtension, whose
// generic arity has shifted between AGP versions.
pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension> {
        buildFeatures { compose = true }
    }
}
pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension> {
        buildFeatures { compose = true }
    }
}

dependencies {
    val composeBom = platform(library("compose-bom"))
    "implementation"(composeBom)
    "androidTestImplementation"(composeBom)
    "implementation"(library("compose-ui"))
    "implementation"(library("compose-ui-graphics"))
    "implementation"(library("compose-ui-tooling-preview"))
    "implementation"(library("compose-material3"))
    "implementation"(library("compose-material-icons-core"))
    "implementation"(library("compose-foundation"))
    "debugImplementation"(library("compose-ui-tooling"))
    "debugImplementation"(library("compose-ui-test-manifest"))
    "androidTestImplementation"(library("compose-ui-test-junit4"))
    "androidTestImplementation"(library("androidx-junit"))
    "androidTestImplementation"(library("androidx-espresso-core"))
}
