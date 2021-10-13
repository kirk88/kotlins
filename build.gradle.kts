plugins {
    id("com.github.ben-manes.versions")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}


