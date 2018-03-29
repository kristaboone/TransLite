# TransLite
Developed for CEG 7370 (Distributed Computing) course at WSU

## Developer Setup
To get the repository and the `volley` submodule:

```
> git clone https://github.com/kristavan/TransLite.git
> git submodule update --init
```

The `volley` gradle build is [currently broken](https://stackoverflow.com/questions/41237629/gradle-dsl-method-not-found-has), so you'll need to edit the gradle file to replace `has()` with `hasProperty()`. You can do this by opening Android Studio, loading the TransLite project, and:

- Go to `Edit > Find > Find in Path...`
- In the search bar, type `project.has`
- There should be two instances identified. Replace `project.has()` with `project.hasProperty()`

The project should build as intended now!
