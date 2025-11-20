plugins {
    application
    alias(libs.plugins.shadow)
}

application {
    mainClass = "ru.cyberc3dr.project.Main"
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass))
    }
}

group = "ru.cyberc3dr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.annotations)
    implementation(libs.logback)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.withType<Jar> {
    destinationDirectory = file("$rootDir/build")
    archiveVersion = ""
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets.main {
    java.srcDir("src")
    resources.srcDir("resources")
}

sourceSets.test {
    java.srcDir("test")
}