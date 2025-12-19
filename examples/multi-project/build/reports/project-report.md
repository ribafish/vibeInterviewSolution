# project-report-example

## Metadata
- Name: project-report-example
- Group: com.example.demo
- Description: Aggregated report across all subprojects.

## Dependencies
### compileClasspath
- :app - app-1.0.0.jar
- :library - main
- :utils - main

### runtimeClasspath
- :app - app-1.0.0.jar
- :library - library-1.0.0.jar
- :utils - utils-1.0.0.jar
- org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar
- org.apache.commons:commons-text:1.10.0 - commons-text-1.10.0.jar

### testCompileClasspath
- :app - app-1.0.0.jar
- :library - main
- :utils - main

### testRuntimeClasspath
- :app - app-1.0.0.jar
- :library - library-1.0.0.jar
- :utils - utils-1.0.0.jar
- org.apache.commons:commons-lang3:3.12.0 - commons-lang3-3.12.0.jar
- org.apache.commons:commons-text:1.10.0 - commons-text-1.10.0.jar

