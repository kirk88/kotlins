plugins {
    id("com.github.ben-manes.versions")
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}