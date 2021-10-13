plugins {
    id("version-catalog")
    id("maven-publish")
    id("com.github.ben-manes.versions")
}

buildscript {
    dependencies {
        classpath(pp.android)
//        classpath(pp.android)
//        classpath(deps.kotlinPlugin)
    }
}
