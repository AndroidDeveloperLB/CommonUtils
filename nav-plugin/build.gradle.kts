plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
}

group = "com.lb.navplugin"
version = "1.0.0"

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
