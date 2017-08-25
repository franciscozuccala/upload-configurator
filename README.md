# upload-configurator

[![](https://jitpack.io/v/franciscozuccala/upload-configurator.svg)](https://jitpack.io/#franciscozuccala/upload-configurator)

El plugin upload-variants-configurator es un plugin diseñado para subir a nexus una libreria de Android
generando artifacts por cada build variant
El plugin upload-configurator es un plugin que solamente se encarga de subir el artifact de una librería a nexus
con sus sources

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

Luego aplicar alguno de los dos plugins:

```
apply plugin: 'upload-configurator'

apply plugin: 'upload-variants-configurator'
```

Por ultimo la configuración del plugin:
```
uploadConfigurator {
    pomName 'Nombre del modulo'
    pomArtifactId 'artifact Id'
    pomVersion 'version'
    pomDescription 'Descripcion del modulo'
    pomUrl 'Url de github comunmente'
    developer 'id', 'name', 'dev@mail.com'

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
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.franciscozuccala:upload-configurator:0.0.1'
    }
}

apply plugin: 'upload-variants-configurator'

uploadConfigurator {
    pomName 'My Module'
    pomArtifactId 'artifact Id'
    pomVersion 'version'
    pomDescription 'This is my module'
    pomUrl 'https://github.com/user/my-module'
    developer 'me', 'me', 'me@mail.com'
    developer 'other', 'other', 'other@mail.com'

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
