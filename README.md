# upload-configurator

[![](https://jitpack.io/v/franciscozuccala/upload-configurator.svg)](https://jitpack.io/#franciscozuccala/upload-configurator)

El plugin upload-configurator es un plugin diseñado para subir a nexus una libreria de Android
generando artifacts por cada build variant

## Como usarlo

En el build.gradle del modulo que implemente el plugin agregar:

El buildscript:

```
buildscript{
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        ...
        classpath 'com.github.franciscozuccala:upload-configurator:X.Y.Z'
    }
}
```

Luego aplicar el plugin:

```
apply plugin: 'upload-configurator'
```

Por ultimo la configuración del plugin:
```
uploadConfigurator {
    pomName 'Nombre del modulo'
    pomDescription 'Descripcion del modulo'
    pomUrl 'Url de github comunmente'
    developer 'me', 'me', 'me@mail.com'
    developer 'other', 'other', 'other@mail.com'

    if (necesitaAutenticacion) {
        repoAuth = authentication 'userName', 'password'
        snapRepoAuth = authentication 'userName', 'password'
    }
    repository 'http://url-del-repositorio', repoAuth
    snapshotRepository 'http://url-del-repositorio', snapRepoAuth
}
```

Ejemplo:
```
buildscript{
    repositories {
        jcenter()
        maven { url "${System.env.HOME}/.m2/repository" }
    }
    dependencies {
        classpath 'com.github.franciscozuccala:upload-configurator:0.0.2-SNAPSHOT'
    }
}

apply plugin: 'upload-configurator'

uploadConfigurator {
    pomName 'My Module'
    pomDescription 'This is my module'
    pomUrl 'https://github.com/user/my-module'

    Boolean uploadLocal = project.hasProperty("UPLOAD_LOCAL") ? new Boolean(UPLOAD_LOCAL) : false
    def repoAuth = null
    def snapRepoAuth = null
    if (!uploadLocal) {
        repoAuth = authentication getRepositoryUsername(), getRepositoryPassword()
        snapRepoAuth = authentication getRepositoryUsername(), getRepositoryPassword()
    }
    repository getReleaseRepositoryUrl(), repoAuth
    snapshotRepository getSnapshotRepositoryUrl(), snapRepoAuth

}
```
