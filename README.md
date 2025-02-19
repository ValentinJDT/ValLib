# ValLib
This repo contains all the tools I have developed for projects.

You can find :
- EventRegister (+ Event API)
- PluginLoader (+ Plugin API)
- Html builder
- YamlReader (Does not support lists at the moment)
- Test framework
- Socket connection : client and server (Kotlin + Java version)

And others functions in the package : `fr.valentin.lib.vallib.utils`


## Installation

Add repository :
```groovy
repositories {
    /* Others repositories */
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
}
```

Add implementation :
```groovy
dependencies {
    /* Others dependencies */
    implementation("com.github.ValentinJDT:ValLib:v0.2.0")
}
```

Compile lib in your project :
```groovy
jar {
    from {
        configurations
                .runtimeClasspath
                .collect {
                    if (it.name.contains("ValLib")) {
                        zipTree(it)
                    }
                }
    }
}
```
