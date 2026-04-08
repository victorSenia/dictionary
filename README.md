# dictionary

Java dictionary project with a reusable common module and a Windows desktop application.

## Modules

- `common`  
  Shared domain classes and services. Intended to be reusable from other projects.

- `windows`  
  JavaFX desktop application for Windows. Uses `common`, Dagger, SQLite JDBC, and JavaFX.

## What It Does

The project stores and works with words, translations, topics, sentences, and playback-related settings.  
The Windows application loads configuration from `app.properties`, uses a SQLite database, and starts a JavaFX UI from `windows/src/main/resources/org/leo/dictionary/view/dictionary_view.fxml`.

## Build

Requirements:

- JDK 21 for the full Maven build
- Maven

Notes:

- `common` is compiled for Java 8 compatibility
- `windows` follows the parent Java 21 build and uses JavaFX 23

Build the whole project:

```bash
mvn package
```

Build only `common`:

```bash
mvn -pl common -am package
```

## Configuration

- `app.properties` contains runtime settings used by the application
- `parseWordsConfig.properties` contains word parsing configuration
- `dictionary.db` is the local SQLite database file

## Release

GitHub Actions creates releases from tags in the form `vX.Y.Z`.

Release rules:

- the tag must point to the current head of the default branch
- the tag version must match the current root `revision` without `-SNAPSHOT`

If the checks pass, the workflow:

1. builds the project
2. creates a GitHub release
3. uploads:
   - `common-X.Y.Z.jar`
   - `windows-X.Y.Z.jar`
4. updates the root version to the next patch snapshot

Example:

- current version: `1.0.0-SNAPSHOT`
- tag: `v1.0.0`
- next version after release: `1.0.1-SNAPSHOT`
