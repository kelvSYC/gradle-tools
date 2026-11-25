# ant-bridge

A migration aid for Gradle 10, which removes the built-in Ant integration (`project.ant`, `ant.importBuild` and
friends). The path off Ant is to replace what feeds each Gradle seam: Ant property loading becomes a `ValueSource`,
Ant task calls in a task action become Worker API work, and the Ant tasks you still need become typed Gradle tasks.
Ant itself runs in an isolated class loader (or process) from the `antBridge` configuration, never on the build
script classpath, and everything works under the configuration cache.

## Supported Gradle versions

Gradle 8.14 and later, running on JDK 21 or newer. Ant 1.10.x.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.ant-bridge")
}
```

The plugin creates the `antBridge` dependency scope, the `antBridgeClasspath` resolvable configuration and the
`antBridge` extension (`antBridge.classpath`, `antBridge.operations`, `antBridge.properties { }`).

### The `antBridge` configuration

By default it holds `org.apache.ant:ant` and `org.apache.ant:ant-junit` at a *preferred* version `1.10.18`. Add antlibs
as dependencies; pin a different Ant with `strictly`:

```kotlin
dependencies {
    antBridge("ant-contrib:ant-contrib:1.0b3") { exclude(group = "ant") }
    antBridge("org.apache.ant:ant") { version { strictly("1.10.14") } }
    antBridge("org.apache.ant:ant-junit") { version { strictly("1.10.14") } }
}
```

Repositories come from the project's own `repositories { }`.

## Migration recipes

Each recipe shows the legacy pattern and its replacement. The Kotlin DSL entry points are `AntOperations.execute`
and `AntWork.ant`; legacy Groovy closures and Kotlin `withGroovyBuilder` bodies move, unchanged, to
`AntOperations.record` and `AntWork.record`.

### `ant.property(file:)` and `ant.properties[...]` reads

```kotlin
// before: ant.property(file: "build.properties"); version = ant.properties["app.version"]
val legacy = antBridge.properties { file("build.properties") }
version = legacy.map { it.getValue("app.version") }.get()
```

See [Loading Ant properties](#loading-ant-properties-antproperties) for every source and the other property recipes.

### `ant.xxx` in a Groovy DSL `doLast`

```groovy
// before
tasks.register('legacy') {
    doLast {
        ant.copy(todir: 'out') {
            fileset(dir: 'src') { include(name: '**/*.txt') }
        }
    }
}

// after: capture the operations and classpath as locals; the closure body is unchanged
def ops = antBridge.operations
def cp = antBridge.classpath
tasks.register('legacy') {
    doLast {
        ops.record(cp) {
            copy(todir: 'out') {
                fileset(dir: 'src') { include(name: '**/*.txt') }
            }
        }
    }
}
```

### `ant.xxx` in a custom task class's `@TaskAction`

Create an `AntOperations` with the injected `ObjectFactory` and pass the Ant classpath as a task input.

```kotlin
// before: ant.touch(file: marker) in the @TaskAction (Groovy) or the legacy ant builder (Kotlin)
abstract class Legacy @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    private val ant = objects.newInstance(AntOperations::class.java)

    @get:Classpath
    abstract val cp: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val out: DirectoryProperty

    @TaskAction
    fun go() {
        val marker = out.get().file("marker")
        ant.execute(cp) { task("touch", "file" to marker) }
        // or, for a legacy closure body: ant.record(cp) { touch(file: marker) }
    }
}

tasks.register<Legacy>("legacy") {
    cp.from(antBridge.classpath)
    out.set(layout.buildDirectory.dir("legacy"))
}
```

### `ant.withGroovyBuilder` in the Kotlin DSL

`withGroovyBuilder` on the project's `ant` cannot run once the built-in Ant is gone. Move the body into `record`:

```kotlin
// before: ant.withGroovyBuilder { "copy"("todir" to "out") { "fileset"("dir" to "src") { "include"("name" to "**/*.txt") } } }
tasks.register<AntWork>("legacy") {
    record {
        withGroovyBuilder {
            "copy"("todir" to "out") {
                "fileset"("dir" to "src") {
                    "include"("name" to "**/*.txt")
                }
            }
        }
    }
}
```

In a Kotlin DSL `doLast`, use `AntOperations.record` the same way, or rewrite the body with `execute` (below). Capture
`antBridge.operations`, `antBridge.classpath` and `layout.projectDirectory` as locals inside `tasks.register { }`:
a `doLast` lambda that reads a script-level `val` captures the script object, which fails the configuration cache.

```kotlin
tasks.register("legacy") {
    val ops = antBridge.operations
    val cp = antBridge.classpath
    val root = layout.projectDirectory
    doLast {
        ops.execute(cp) { task("mkdir", "dir" to root.dir("out")) }
    }
}
```

### `taskdef` of an antlib

Add the antlib to `antBridge`, then either `taskdef` it or address its tasks by `antlib:` namespace. Both run in the
same Ant project as the tasks around them.

```kotlin
// before: ant.taskdef(resource: "net/sf/antcontrib/antlib.xml", uri: "antlib:net.sf.antcontrib")
dependencies {
    antBridge("ant-contrib:ant-contrib:1.0b3") { exclude(group = "ant") }
}

tasks.register<AntWork>("contrib") {
    ant {
        // taskdef resource=
        task(
            "taskdef",
            "resource" to "net/sf/antcontrib/antlib.xml",
            "uri" to "antlib:net.sf.antcontrib"
        )
        task("var", "name" to "n", "value" to "1", namespace = "antlib:net.sf.antcontrib")
        task("echo", "message" to "\${n}")
    }
}
```

The `antlib:` URI form needs no `taskdef`: `task("var", ..., namespace = "antlib:net.sf.antcontrib")` makes Ant load
the antlib named by the URI from the `antBridge` classpath. In recorded Groovy, the same is
`ant.'antlib:net.sf.antcontrib:var'(name: 'x', value: '1')`.

### The Kotlin DSL and its typed helpers

`AntOperations.execute(classpath) { }` and `AntWork.ant { }` take an `AntScope`: `task(name, attributes..., namespace
= null) { }` appends an Ant task, and the nested scope (`AntElementScope`) has `element(...)`, `text(...)` and helpers
for common Ant types.

| Helper | Ant element |
|---|---|
| `fileset(dir, includes, excludes) { }` | `<fileset dir>` with `include`/`exclude` children |
| `include(name)`, `exclude(name)` | `<include>`, `<exclude>` |
| `path(files)` | `<path path="...">`; `files` may be a `FileCollection`, kept lazy |
| `mapper(type, from, to)` | `<mapper>` (absent attributes omitted) |
| `filterset(beginToken, endToken, tokens)` | `<filterset>` with one `<filter>` per token |
| `filterchain { }` | `<filterchain>` |

Attribute and text values can be `String`, `Number`, `Boolean`, `File`, `Path`, a `FileSystemLocation`, a
`FileCollection` or a `Provider<*>` of these; providers stay lazy until the task runs.

```kotlin
tasks.register<AntWork>("copyText") {
    ant {
        task("copy", "todir" to layout.projectDirectory.dir("out")) {
            fileset(layout.projectDirectory.dir("src"), includes = listOf("**/*.txt"))
        }
    }
}
```

## Loading Ant properties: `AntProperties`

A `ValueSource<Map<String, String>, AntProperties.Parameters>` that loads properties as Ant's `property` task does, in
a fresh Ant project and an isolated class loader (Ant never goes on the build script classpath).

- Seeds are set first, then each source loads in declaration order. As in Ant, the first value set for a key wins, so
  seeds override the sources, and an earlier source wins over a later one. `${...}` references expand within and
  across sources.
- The result holds only the keys the seeds and sources name, each with Ant's effective value: a key already set as a
  system property reports the system property's value. Other system properties and Ant built-ins (`basedir`,
  `ant.*`) are excluded.
- `basedir` is the project directory, as with Gradle's built-in `ant`.
- A missing file contributes nothing and is logged at info. Files are read as ISO-8859-1 with `\uXXXX` escapes.
- Files, URLs and the environment are read only when the value is obtained, so changing them invalidates the
  configuration cache.

### Sources (`AntPropertiesSpec`)

| Function | Ant equivalent |
|---|---|
| `file(path)`, `file(path) { prefix.set("p"); prefixValues.set(true) }` | `<property file="..." prefix="..." prefixValues="..."/>`; `path` resolves against the project directory |
| `resource(name, classpath)`, `resource(name, classpath) { prefix.set("p") }` | `<property resource="..." classpath="..." prefix="..."/>` |
| `url(url)`, `url(url) { prefix.set("p") }` | `<property url="..." prefix="..."/>` |
| `environment(prefix)` | `<property environment="..."/>` |
| `seed(key, value)` | a property set before loading (overrides the sources) |

### Parameters (when using `providers.of` directly)

| Parameter | Type | Description |
|---|---|---|
| `sources` | `ListProperty<AntPropertySource>` | `File`, `Resource`, `Url` or `Environment`, loaded in order |
| `seeds` | `MapProperty<String, String>` | Properties set before any source |
| `baseDirectory` | `DirectoryProperty` | Ant's `basedir`, used by `${basedir}` references; the spec sets the project directory |
| `antClasspath` | `ConfigurableFileCollection` | The Ant classpath |

### Example

```kotlin
// before: ant.property(file: "build.properties"); version = ant.properties["app.version"]
val legacy = antBridge.properties { file("build.properties") }
version = legacy.map { it.getValue("app.version") }.get()

// before: ant.property(environment: "env"); ant.properties["env.HOME"]
val home = antBridge.properties { environment("env") }.antProperty("env.HOME")

// before: ant.property(name: "base", value: "/srv"); ant.property(file: "build.properties")
val overridden = antBridge.properties {
    seed("base", "/srv")
    file("build.properties")
}

// before: ant.property(file: "build.properties", prefix: "cfg"); ant.properties["cfg.dir"]
val dir = antBridge.properties { file("build.properties") { prefix.set("cfg") } }.antProperty("cfg.dir")
```

`antProperty(key)` returns a `Provider<String>` that is absent when the key is not defined.

## Typed tasks (`com.kelvsyc.gradle.ant.tasks`)

Each extends `AbstractAntTask` (`antClasspath`, `isolation`; both wired by the plugin, isolation defaults to `CLASSLOADER`), declares its inputs and outputs, and never modifies an input in place.

| Task | Ant task | Inputs | Output | Cached |
|------|----------|--------|--------|--------|
| `AntReplaceRegexp` | `replaceregexp` | `sourceDirectory`, `includes`, `excludes`, `match`, `replace`, `flags`, `byLine`, `encoding` | `outputDirectory` (a filtered copy, then replaced in place there) | yes |
| `AntXslt` | `xslt` | `sourceDirectory`, `includes`, `excludes`, `stylesheet`, `parameters`, `extension` | `outputDirectory` | yes |
| `AntChecksum` | `checksum` | `files`, `algorithm` (`SHA-256`), `fileExtension` (`.sha256`) | `outputDirectory` | yes |
| `AntSignJar` | `signjar` | `jars`, `keystore`, `alias`, `storeType`, `sigAlg`, `digestAlg`, `tsaUrl` | `outputDirectory` | no |
| `AntJUnitReport` | `junitreport` | `resultsDirectory`, `includes` (`TEST-*.xml`), `format` (`noframes`) | `outputDirectory` | yes |

`AntSignJar` takes `storePassword` and `keyPassword` as `Property<CredentialReference>`; they are resolved at execution and are not task inputs. `AntJUnitReport` needs `org.apache.ant:ant-junit`, which the plugin adds by default.

```kotlin
tasks.register<AntChecksum>("checksums") {
    files.from(tasks.jar)
    outputDirectory.set(layout.buildDirectory.dir("checksums"))
}
```

## Generic fallback: `AntWork`

Any Ant task without a typed replacement runs through `AntWork` (Ant tasks in order; `ant { }` or `record { }`). It
has no typed inputs: declare them yourself with `inputs`/`outputs`. This includes `native2ascii`, which has no typed
task because JDK 9 and later read UTF-8 property bundles and the JDK's own `native2ascii` tool was removed:

```kotlin
tasks.register<AntWork>("native2ascii") {
    inputs.dir("src/messages")
    outputs.dir(layout.buildDirectory.dir("messages"))
    ant {
        task(
            "native2ascii",
            "encoding" to "UTF-8",
            "src" to layout.projectDirectory.dir("src/messages"),
            "dest" to layout.buildDirectory.dir("messages")
        )
    }
}
```

`AntWork` has `elements`, `antClasspath` (defaults to `antBridgeClasspath`) and `isolation`. A task without declared
outputs is never up to date.

## `AntOperations` and isolation modes

`AntOperations` is the `ExecOperations` of Ant: `execute(classpath) { }` (Kotlin DSL) and `record(classpath) { }`
(legacy closures) build a tree, resolve it, run it through the Worker API and wait. Get one from
`antBridge.operations`, or create one with `objects.newInstance(AntOperations::class.java)` in a custom task. The Ant
project's base directory is the project directory. A failing Ant task fails the call with a `WorkerExecutionException`
caused by `AntExecutionException`.

| `AntIsolation` | Ant runs |
|---|---|
| `CLASSLOADER` (default) | in the Gradle daemon, in an isolated class loader |
| `PROCESS` | in a separate worker process, for Ant tasks that leak static state or call `System.exit` |

Pass it as the second argument (`ops.execute(cp, AntIsolation.PROCESS) { ... }`) or set `isolation` on an `AntWork` or
typed task.

## What cannot be migrated

- **Read-backs inside a recorded block.** Ant runs after the block ends, so reads of `properties`, `references` or
  `project`, and uses of values returned by recorded calls, fail loudly (`AntReservedPropertyException`,
  `AntReadBackException`). Load properties with `antBridge.properties { }` instead.
- **Unqualified method calls inside a recorded block are recorded as Ant elements**, including `file('x')` and
  `println`. Compute values outside the block and use them inside.
- **No task dependencies from values in `AntWork`'s tree.** A `Provider` or `FileCollection` there carries no task
  dependency or input registration: declare inputs, outputs and `dependsOn` yourself.
- **`AntOperations` waits for all worker items submitted by the current task**, not only its own.
- **`AntWork` without declared outputs is never up to date.**
- **`junitreport` with the `frames` format** may need Xalan on `antBridge`; `noframes` works with the default
  classpath.
- **`ant.importBuild` is not covered.** Port the imported targets to Gradle tasks (typed tasks, `AntWork`).
