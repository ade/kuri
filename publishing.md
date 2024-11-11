Uses vanniktech publish plugin
https://vanniktech.github.io/gradle-maven-publish-plugin/central/

## Publishing
1. Set version in gradle.properties. Will publish snapshot if version ends with -SNAPSHOT
2. Publish with:
```
./gradlew kuriPublish
```
3. Set new snapshot version in gradle.properties and commit