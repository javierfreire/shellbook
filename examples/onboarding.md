# SellBook :: Developer Guide

## Development environment {#requirements .choice}

### Prepare environment on Linux

#### Install Sdkman {.optional}

You can follow the next documentation: [Sdkman installation](https://sdkman.io/install)

```shell
$ curl -s "https://get.sdkman.io" | bash
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
$ sdk version
```

#### Install GraalVM {.optional}

* [GraalVM](https://www.graalvm.org/22.0/docs/getting-started/)
* [NativeImage](https://www.graalvm.org/22.0/reference-manual/native-image/)

```shell
$ sdk install java 22.0.0.2.r17-grl
$ sdk use java 22.0.0.2.r17-grl
$ gu install native-image
```

### Prepare environment on Mac

TODO ???

## Build

`make` is used as index to launch different targets.

### Tools

- [make](https://www.gnu.org/software/make/)
- [gradle](https://gradle.org/)
- [GraalVM Gradle Plugin](https://graalvm.github.io/native-build-tools/0.9.11/gradle-plugin.html)
- [Gradle Shadow Plugin](https://imperceptiblethoughts.com/shadow)

### Build artifacts {#build-artifacts .menu}

#### Build JAR

```shell {.play}
$ make jar
```

#### Build Native

```shell {.play}
$ make native
```

### Libraries used

* [intellij-markdown](https://github.com/JetBrains/markdown) : markdown processor
* [Clikt](https://ajalt.github.io/clikt/) : command line interface for kotlin
* [Mordant](https://github.com/ajalt/mordant) : colorful styling for command-line applications
* [kotlin-inquirer](https://github.com/kotlin-inquirer/kotlin-inquirer) : a collection of common interactive command line user interfaces 