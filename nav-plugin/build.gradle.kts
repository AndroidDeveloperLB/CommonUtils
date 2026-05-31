plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("navProguardPlugin") {
            id = "com.lb.navplugin.NavProguardPlugin"
            implementationClass = "com.lb.navplugin.NavProguardPlugin"
        }
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:9.2.1")
}
