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
`antBridge` extension (`antBridge.classpath`, `antBridge.operations`, `antBridge.properties { }`,
`antBridge.query { }`, `antBridge.importBuild(...)`).

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
version = legacy.getting("app.version").get()
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

Inside a recorded body, an `ant.` prefix (`ant.copy(...)`, or `ant.withGroovyBuilder { }` in Kotlin) is accepted and
records the same: `ant` there is the recorder, never Gradle's `project.ant`.

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
        // or, for a legacy builder body: ant.record(cp) { withGroovyBuilder { "touch"("file" to marker) } }
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
`'antlib:net.sf.antcontrib:var'(name: 'x', value: '1')` (an `ant.` prefix is also accepted).

### `ant.importBuild`

```kotlin
// before: ant.importBuild("build.xml")
antBridge.importBuild(file("build.xml"))
```

The imported targets stay Gradle tasks of the same names, and can be handed over to Gradle one at a time. See
[Replacing `ant.importBuild`](#replacing-antimportbuild).

### The Kotlin DSL and its typed helpers

`AntOperations.execute(classpath) { }`, `AntWork.ant { }`, `AntTrees.build { }` and a typed task's `elements()` take an
`AntScope`: `task(name, attributes..., namespace = null) { }` appends an Ant task, and the nested scope
(`AntElementScope`) has `element(...)`, `text(...)` and helpers for common Ant types.

| Helper | Ant element |
|---|---|
| `fileset(dir, includes, excludes) { }` | `<fileset dir>` with `include`/`exclude` children |
| `include(name)`, `exclude(name)` | `<include>`, `<exclude>` |
| `path(files)` | `<path path="...">`; `files` may be a `FileCollection`, kept lazy |
| `mapper(type, from, to)` | `<mapper>` (absent attributes omitted) |
| `filterset(beginToken, endToken, tokens)` | `<filterset>` with one `<filter>` per token |
| `filterchain { }` | `<filterchain>` |

Attribute and text values can be `String`, `Number`, `Boolean`, `File`, `Path`, a `FileSystemLocation`, a
`FileCollection` or a `Provider<*>` of these; providers stay lazy until the task runs. A `null` value leaves the
attribute out; an absent provider fails when the task runs.

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
| `environment(prefix)` | `<property environment="..."/>`; the whole environment is the value's input, so any changed variable invalidates the configuration cache, and every variable (tokens included) is stored, encrypted, in its fingerprint |
| `seed(key, value)` | a property set before loading (overrides the sources) |

### Parameters (when using `providers.of` directly)

Direct users build the `AntPropertySource` values themselves and must set `baseDirectory`.

| Parameter | Type | Description |
|---|---|---|
| `sources` | `ListProperty<AntPropertySource>` | `File`, `Resource`, `Url` or `Environment`, loaded in order |
| `seeds` | `MapProperty<String, String>` | Properties set before any source |
| `baseDirectory` | `DirectoryProperty` | Required: Ant's `basedir`, used by `${basedir}` references; the spec sets the project directory |
| `antClasspath` | `ConfigurableFileCollection` | The Ant classpath |

### Example

```kotlin
// before: ant.property(file: "build.properties"); version = ant.properties["app.version"]
val legacy = antBridge.properties { file("build.properties") }
version = legacy.getting("app.version").get()

// before: ant.property(environment: "env"); ant.properties["env.HOME"]
val home = antBridge.properties { environment("env") }.getting("env.HOME")

// before: ant.property(name: "base", value: "/srv"); ant.property(file: "build.properties")
val overridden = antBridge.properties {
    seed("base", "/srv")
    file("build.properties")
}

// before: ant.property(file: "build.properties", prefix: "cfg"); ant.properties["cfg.dir"]
val dir = antBridge.properties { file("build.properties") { prefix.set("cfg") } }.getting("cfg.dir")
```

`getting(key)` (from `gradle-extensions`, `import com.kelvsyc.gradle.providers.getting`) returns a `Provider<String>`
that is absent when the key is not defined. In the Groovy DSL, use `.map { it['key'] }`.

## Typed tasks (`com.kelvsyc.gradle.ant.tasks`)

Each extends `AbstractAntTask` (`antClasspath`, `isolation`; both wired by the plugin, isolation defaults to `CLASSLOADER`), declares its inputs and outputs, and never modifies an input in place.

Give each task an output directory of its own. The tasks clear (or, for `AntReplaceRegexp`, sync into) their output
directory before Ant runs, so that outputs of removed inputs do not survive; a task whose output directory holds any of
its inputs fails before anything is deleted. `AntChecksum` and `AntSignJar` write their outputs by file name, so they
fail when two inputs share a name.

| Task | Ant task | Inputs | Output | Cached |
|------|----------|--------|--------|--------|
| `AntReplaceRegexp` | `replaceregexp` | `sourceDirectory`, `includes`, `excludes`, `match`, `replace`, `flags`, `byLine`, `encoding` | `outputDirectory` (a filtered copy, then replaced in place there) | yes |
| `AntXslt` | `xslt` | `sourceDirectory`, `includes`, `excludes`, `stylesheet`, `parameters`, `extension` | `outputDirectory` | yes |
| `AntChecksum` | `checksum` | `files`, `algorithm` (`SHA-256`), `fileExtension` (`.sha256`) | `outputDirectory` | yes |
| `AntSignJar` | `signjar` | `jars`, `keystore`, `alias`, `storeType`, `sigAlg`, `digestAlg`, `tsaUrl` | `outputDirectory` | no |
| `AntJUnitReport` | `junitreport` | `resultsDirectory`, `includes` (`TEST-*.xml`), `format` (`noframes`) | `outputDirectory` | yes |

`AntXslt`, `AntChecksum` and `AntSignJar` clear `outputDirectory` before Ant runs and tell Ant to skip its own
up-to-date checks, so a rerun never leaves stale output (including the output of a removed input). `AntSignJar` takes
`storePassword` and `keyPassword` as `Property<CredentialReference>`; they are resolved at execution and are not task inputs. `AntJUnitReport` needs `org.apache.ant:ant-junit`, which the plugin adds by default.

```kotlin
tasks.register<AntChecksum>("checksums") {
    files.from(tasks.jar)
    outputDirectory.set(layout.buildDirectory.dir("checksums"))
}
```

### Defining a typed task

Extend `AbstractAntTask` to wrap an Ant task in a reusable task type:

1. Declare the task's inputs and outputs as annotated properties, as for any Gradle task.
2. Override `AntScope.elements()` and describe the Ant work with the same Kotlin DSL as `AntWork.ant { }`
   ([The Kotlin DSL and its typed helpers](#the-kotlin-dsl-and-its-typed-helpers)): nesting, `fileset`,
   `filterchain` and the other helpers all work. It runs at execution, so pass properties directly (they stay lazy)
   and read optional ones with `orNull`: a `null` attribute value is left out, and a `null` check skips an optional
   nested element.
3. Override `prepare()` if outputs need setting up first.

`AbstractAntTask` takes no constructor parameters; it injects `objects` and `fileSystemOperations`, which subclasses
may use.

```kotlin
@CacheableTask
abstract class Concat : AbstractAntTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:Input
    abstract val includes: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val header: Property<String>

    @get:OutputFile
    abstract val destination: RegularFileProperty

    override fun AntScope.elements() {
        task("concat", "destfile" to destination, "fixlastline" to true) {
            header.orNull?.let { element("header", "trimleading" to true) { text(it) } }
            fileset(sourceDirectory, includes = includes.get())
            filterchain { element("striplinecomments") { element("comment", "value" to "#") } }
        }
    }
}
```

Clear any output directory in `prepare()` (`clearDirectory(outputDirectory)`) when the Ant task skips work it
considers up to date, as `xslt`, `signjar` and `checksum` do, and set Ant's force attribute: otherwise Gradle reruns
the task but Ant leaves stale output in place. `clearDirectory` refuses a directory that holds any of the task's
inputs; call `requireNoInputsIn(directory)` before replacing a directory's contents any other way, and
`requireDistinctNames(files, what)` when outputs are written by input file name.

### Reusing Ant code

Reuse Ant at the Gradle boundary, not inside the Ant tree, so that the eventual Ant-free rewrite does not touch the
call sites:

- **A typed task** (above): callers see typed inputs and outputs. Going Ant-free replaces `elements()` with
  Gradle-native code; every caller stays unchanged, and up-to-date checks and caching come for free.
- **A Gradle-level helper** that registers or configures a task, such as `fun TaskContainer.registerConcat(...)`:
  its signature survives a switch to a native task.
- **Not an extension on `AntScope`** such as `fun AntScope.touch(...)`. Its callers live inside `ant { }` blocks,
  so going Ant-free rewrites every one of them, and a growing set of such helpers turns Ant into a permanent part of
  the build's vocabulary.

Helpers that only translate Gradle values into Ant data, like the built-in `path(files)` and `filterset(tokens)`,
are fine anywhere: they carry no behaviour of their own.

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

`AntWork` is meant to be configured, not extended; to define a reusable Ant task type, extend `AbstractAntTask`
(see [Defining a typed task](#defining-a-typed-task)).

`AntWork` has `elements`, `antClasspath` (defaults to `antBridgeClasspath`), `isolation` and, to hand Ant-computed
values to other tasks, `exportProperties`, `exportedPropertiesFile` and `exportedProperties` (see
[Values Ant computes](#values-ant-computes)). A task without declared
outputs is never up to date. The Ant tree is an input through `treeFingerprint`, the SHA-256 of the resolved tree, so
editing it reruns the task; only the hash is recorded, keeping resolved values such as secrets out of the history.
The hash is unsalted, so a short secret written directly into the tree could be guessed from it: pass secrets to Ant
through a typed task (`AntSignJar` resolves its `CredentialReference` passwords at execution) or an environment
variable instead. It also includes absolute paths, so if you enable caching on an `AntWork` task, its cache entries are
not shared between checkouts in different directories.

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

## Values Ant computes

`getting` comes from `gradle-extensions`: `import com.kelvsyc.gradle.providers.getting`.

Ant passes values through properties. The bridge turns them into a `Provider<Map<String, String>>`, read with
`getting(key)` (as for `antBridge.properties { }`). Later you replace that provider with a
non-Ant one from the [read-back table](#what-cannot-be-migrated) and the code that reads it does not change. Results are
strings; a name the Ant tree never set is absent from the map (`getting` gives an absent provider), never an empty
string.

### Script body: `antBridge.query { }`

```kotlin
// before: ant.available(file: 'VERSION', property: 'hasVersion'); ant.loadfile(srcFile: 'VERSION', property: 'v')
//         if (ant.properties['hasVersion']) version = ant.properties['v']
val q = antBridge.query {
    ant {
        task("available", "file" to layout.projectDirectory.file("VERSION"), "property" to "hasVersion")
        task("loadfile", "srcFile" to layout.projectDirectory.file("VERSION"), "property" to "v", "failonerror" to false)
    }
    result("hasVersion", "v")
}
if (q.getting("hasVersion").isPresent) {
    version = q.getting("v").get()
}
```

`record { }` takes the legacy closures unchanged. An Ant failure fails the build at configuration with
`AntExecutionException`.

The query is a `ValueSource`, so the configuration cache treats it according to where it is read:

- **Read during configuration** (the script body, as above), the query is a configuration-cache input: it re-runs on
  every cache check, and an edit to what it reads invalidates the entry. This is why the allowlist below exists.
- **Read only inside task actions** (capture the provider in a local inside `tasks.register`, read it in `doLast`), it
  is obtained when the task runs and is not an input: the entry survives a change to what it reads.

**Allowlist.** Because a query may re-run on every build, only read-only tasks are accepted, checked when the query is
declared: `available`, `condition`, `loadfile`, `loadproperties`, `property`, `basename`, `dirname`, `pathconvert`,
`length`, and `checksum` only with a `property` attribute (without it, `checksum` writes files). The check applies to
top-level elements (nested elements are the task's own configuration) and matches the task name, the `property`
attribute and the namespace exactly: `Property=` or an explicit empty namespace is rejected, as is every namespaced
element (antlib tasks). `tstamp` is excluded on purpose: its value changes on every build, which would invalidate the
configuration cache every time. Any other task fails with a message naming it.

`allowSideEffects()` inside the query lifts the check. Its cost: the Ant tasks then run on every build, even on a
configuration cache hit, so anything with side effects repeats. Prefer an `AntWork` task for such work.

### Task `doLast` handoff: `AntWork.exportProperties`

```kotlin
// before: task a { doLast { ant.exec(executable: 'hostname', outputproperty: 'rev') } }
//         task b { dependsOn a; doLast { println ant.properties.rev } }
val a = tasks.register<AntWork>("a") {
    exportProperties("rev")
    ant { task("exec", "executable" to "hostname", "outputproperty" to "rev") }
}
tasks.register("b") {
    val exported = a.flatMap { it.exportedProperties }
    inputs.property("exported", exported) // declaring the input carries the dependency on a
    doLast { println(exported.get().getValue("rev")) }
}
```

`exportProperties(...)` is the only way to set the names; `exportedPropertyNames` is read-only and an input, so adding a
name reruns the task. The values go to `exportedPropertiesFile` (default `build/ant-bridge/<task>/exported.properties`)
and `exportedProperties` is derived from it, so a consumer that declares it as an input (`inputs.property(...)`, as
above) depends on the producer without `dependsOn`; a provider captured only inside `doLast` registers nothing. If
the producer did not run (for example, `onlyIf` skipped it), the file is missing and reading `exportedProperties`
fails. A consumer still written in Ant passes the file to `property file=` as a declared input
(`inputs.file(a.flatMap { it.exportedPropertiesFile })`).

The file is Java properties format, and `$` is escaped as `$$`: Ant's `property file=` and `exportedProperties` read
values back exactly, while a plain `java.util.Properties` reader sees `$$`.

**Up to date.** Exporting alone never makes an `AntWork` up to date: an exported value usually depends on something the
task does not declare (a command's output, the clock), so a task whose only output is the export file reruns every build. It is up
to date only when it has declared outputs other than the export file and nothing changed.

### Custom task class: `AntOperations` results

```kotlin
// before: ant.loadfile(...); outputFile.writeText(ant.properties['rev'])
@TaskAction
fun go() {
    val results = ops.execute(cp, AntIsolation.CLASSLOADER, setOf("rev")) {
        task("property", "name" to "rev", "value" to "abc")
    }
    out.get().asFile.writeText(results.getValue("rev")) // write it to a declared output
}
```

`execute` and `record` have overloads taking a `Collection<String>` of names (a Groovy list literal works), with or
without an `AntIsolation` (default `CLASSLOADER`), and returning the `Map<String, String>`; the other overloads still
return nothing. Declare the value as the task's own output, and other tasks read it from there.
To submit the work yourself, `AntWorkAction` takes optional `resultProperties` (names) and `resultFile` (where the
values are written).

## Submitting Ant work to your own work queue: `AntWorkAction`

`AntOperations` waits for all work the current task has submitted. To run Ant next to a task's own work items and wait
once, submit `com.kelvsyc.gradle.ant.actions.AntWorkAction` to your own queue. Build the tree with `AntTrees`
(`AntTrees.build { }` for the Kotlin DSL; `AntTrees.record { }` for legacy calls, taking a Groovy closure or a
`withGroovyBuilder` body) and resolve it at execution time:

```kotlin
abstract class Mixed @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    @get:Classpath
    abstract val antClasspath: ConfigurableFileCollection // set from antBridge.classpath

    @get:OutputDirectory
    abstract val out: DirectoryProperty

    @TaskAction
    fun go() {
        val queue = workers.noIsolation()
        val tree = AntTrees.build { task("mkdir", "dir" to out.dir("from-ant")) }
        queue.submit(AntWorkAction::class.java) {
            nodes.set(tree.map { it.resolve() })
            antClasspath.from(this@Mixed.antClasspath)
            baseDirectory.set(out)
        }
        queue.submit(MyOwnAction::class.java) { /* ... */ }
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `nodes` | `ListProperty<AntNode>` | The Ant tasks to run, in order, resolved (`AntElement.resolve()`) |
| `antClasspath` | `ConfigurableFileCollection` | Ant, its antlibs and anything they need; usually `antBridge.classpath` |
| `baseDirectory` | `DirectoryProperty` | The Ant project's base directory (`basedir`) |
| `resultProperties` | `SetProperty<String>` | Optional: names of Ant properties to report after the tree runs |
| `resultFile` | `RegularFileProperty` | Optional: where the requested properties are written (Java properties format) |

The action isolates Ant itself, in a class loader of its own, so any queue works: `noIsolation()` and
`classLoaderIsolation()` run it in the daemon, `processIsolation()` in a worker process. The queue's classpath needs
nothing. A failing Ant task fails the work item with `AntExecutionException`.

## Replacing `ant.importBuild`

`antBridge.importBuild` replaces `ant.importBuild`: it registers the same tasks, each running one target of the
`build.xml` with Ant's own parser and target execution (`<import>`, `macrodef`, `presetdef`, antlibs, `if`/`unless`,
`antcall` and extension points all work as in Ant). From there, targets can be handed over to Gradle one at a time,
or all at once, until the `build.xml` is gone.

```kotlin
// before
ant.properties["env"] = "ci"
ant.importBuild("build.xml") { target -> "ant-$target" }
ant.importBuild("tools/build.xml", "tools")

// after
val imported = antBridge.importBuild(file("build.xml")) {
    taskName { "ant-$it" }
    properties(mapOf("env" to "ci"))
}
antBridge.importBuild(file("tools/build.xml"), file("tools")) {}
```

- **Tasks.** Each target gets an `AntImportedTarget` task (group `ant`) with the target's description, depending on
  the tasks named by its `depends`; a name that is not a target of the imported build is a Gradle task name, as with
  legacy. The call registers the tasks, so `tasks.named(...)` works right after it. Dependencies are resolved after
  every call, so a later file may provide a target an earlier file depends on.
- **`taskName { }`** is the legacy renaming closure. A task name that is already taken fails the import, naming the
  task and the target; rename with `taskName`. In a root project this includes Gradle's built-in tasks, so a target named `init`,
  `wrapper`, `help` or `tasks` needs renaming.
- **Base directory.** A file's top-level statements and targets use its `<project basedir>`, else its directory, else
  the `baseDirectory` argument of the overload; files pulled in with `<import>` use their importer's.
- **Several calls in one project** form one Ant build, as with legacy: targets of one file may depend on another's,
  and top-level properties are shared. Each call re-reads every file imported so far, with that call's Ant classpath
  and seeds. Calling `importBuild` after the build has been read (by a task, or by querying `propertiesFor`) fails.
- **Seeds.** `properties(map)` and `properties(provider)` replace `ant.properties[...] = ...` and `ant.property(...)`
  before the import. They are resolved during configuration, when the build is read, and set as Ant user properties
  (as `-D` would), so they override the files' own definitions; the first definition of a name wins, across calls too.
- **Isolation and classpath** are per import: `isolation(AntIsolation.PROCESS)` and `antClasspath(files)` (default:
  `CLASSLOADER` and the `antBridge` classpath). The classpath also reads the build.
- **Imported targets always run.** They declare no outputs other than their property exports (below), so they are
  never up to date and not cached, as with legacy.
- **Configuration cache.** Reading the build runs the files' top-level statements during configuration, not on a
  configuration-cache hit. The cache inputs are every loaded build file (including `<import>`ed ones) and the files
  read by top-level `property file=` and `loadproperties srcFile=`; editing one of them invalidates the entry. Other
  parse-time inputs (environment variables, URLs, resources, `exec`) are not tracked: editing them does not.

### How properties flow between targets

Legacy targets shared one Ant project, and so one property bag. Here, each task runs only its own target, in a fresh
Ant project: it sets the seeds, parses every file of the project's Ant build (running their top-level statements),
loads the properties of its upstream targets, then runs the target. The properties the target newly defines are written
to its export file (`build/ant-bridge/import/<task>.properties`), a declared output.

The upstream targets are the target's transitive `depends` in the `build.xml`, in the order Ant would run them; their
export files are declared inputs. The first definition of a name wins, so the target sees what a single Ant run of it
would have computed, and `if`/`unless` see upstream values too. Targets that merely ran earlier contribute nothing (see
[Deviations](#deviations-from-gradles-ant-integration)).

### Handing a target over to Gradle

Take over the `compile` target of this `build.xml`:

```xml
<project name="app">
    <target name="setup"><property name="src.dir" value="src"/></target>
    <target name="compile" depends="setup"><property name="build.dir" value="ant-build"/></target>
    <target name="jar" depends="compile"><echo message="${build.dir} ${src.dir}"/></target>
</project>
```

1. **Exclude it:** `exclude("compile")` registers no task for it. Its task name (after `taskName`) is left free, and
   imported targets that depend on it depend on the Gradle task of that name. Excluding a target the file does not
   add fails.
2. **Write the replacement** and register it under that task name.
3. **Values it needs:** `imported.propertiesFor("compile")` is exactly what the target would have seen: the seeds, the
   top-level properties and the exports of its upstream targets. It carries the dependencies on those tasks, so
   declaring it as an input runs `setup` first.
4. **Values it provides:** `exclude("compile") { properties(provider) }` passes the properties the target used to
   define to the targets downstream of it. Derive the provider from the replacement, so that it carries the
   dependency. The import writes them with a task named `<task name>AntProperties` (here `compileAntProperties`).
5. **The warning.** An excluded target with imported targets downstream and no `properties(...)` logs a warning: the
   properties it used to define are undefined downstream unless supplied (Ant leaves `${build.dir}` unexpanded). The
   check runs in `afterEvaluate`; imports made later are not checked.

```kotlin
import com.kelvsyc.gradle.ant.AntImport
import com.kelvsyc.gradle.providers.getting

lateinit var imported: AntImport

val compile = tasks.register("compile") {
    val src = imported.propertiesFor("compile").getting("src.dir") // configured after the import
    inputs.property("src", src)
    doLast { println("compiling " + src.get()) }
}

imported = antBridge.importBuild(file("build.xml")) {
    exclude("compile") { properties(compile.map { mapOf("build.dir" to "native-build") }) }
}
```

`gradle jar` runs `setup`, the replacement `compile`, then `jar`, which sees `build.dir=native-build` and
`src.dir=src`. `propertiesFor` works for imported targets as well, for example to read a value Ant computes from a
Gradle task.

Once every target is taken over, no imported task remains: remove the `importBuild` call, replace the `propertiesFor`
reads with Gradle values, and delete the `build.xml`.

### Names and duplicates

- A target defined in two top-level imported files fails the import, naming both files, including one a file defines
  and another brings in with `<import>` (Ant would silently keep the first). Extension-point names brought in by
  `<import>` are not checked for collisions.
- A file `<import>`ed by two top-level files is parsed, and its top-level statements run, once per importer; its
  targets belong to the first importer.
- While a later top-level file is parsed, the earlier files' targets are hidden, so a top-level `<antcall>` in the
  later file cannot reach them.
- A dependency cycle anywhere in the build fails the import.
- The prefixed aliases `<import>` creates (`project.target`) get tasks of their own, as with legacy. Excluding the
  unprefixed name leaves the alias registered.

## What cannot be migrated

- **Read-backs inside a recorded block.** Ant runs after the block ends, so reads of `properties`, `references` or
  `project`, and uses of values returned by recorded calls, fail loudly (`AntReservedPropertyException`,
  `AntReadBackException`). Load properties with `antBridge.properties { }` instead, and read values Ant computes with
  `antBridge.query { }` or `AntWork.exportProperties` (see [Values Ant computes](#values-ant-computes)).
- **Unqualified method calls inside a recorded block are recorded as Ant elements**, including `file('x')` and
  `println`. Compute values outside the block and use them inside.
- **No task dependencies from values in `AntWork`'s tree.** A `Provider` or `FileCollection` there carries no task
  dependency or input registration: declare inputs, outputs and `dependsOn` yourself.
- **`AntOperations` waits for all worker items submitted by the current task**, not only its own. Submit
  `AntWorkAction` to your own queue to mix Ant with other work items (see above).
- **`AntWork` without declared outputs is never up to date.**
- **Reading a value Ant computed.** Ant tasks return no values; query tasks set a property, which legacy builds read
  back with `ant.properties[...]`. Within one Ant tree, later tasks can use it (`${name}`), but getting it into
  Gradle is a read-back. To keep Ant computing it for now, see [Values Ant computes](#values-ant-computes); to leave
  Ant, use a Gradle-native provider instead:

  | Legacy pattern | Gradle-native replacement |
  |---|---|
  | `ant.available(file: f, property: 'p')` | `providers.provider { f.exists() }`, or `layout.projectDirectory.file(...)` checked in the task that needs it |
  | `ant.condition(property: 'p') { os(family: 'unix') }` | `providers.systemProperty("os.name").map { ... }` |
  | `ant.loadfile(srcFile: f, property: 'p')` | `providers.fileContents(layout.projectDirectory.file(...)).asText` |
  | `ant.exec(executable: ..., outputproperty: 'p')` | `providers.exec { commandLine(...) }.standardOutput.asText` |
  | `ant.checksum(file: f, property: 'p')` | `providers.of(ChecksumValueSource::class) { parameters { inputFile.set(f); algorithm.set("MD5") } }` (`gradle-extensions`, `com.kelvsyc.gradle.providers`) |
  | `ant.property(file: f)` and reads | `antBridge.properties { file(f) }` |
- **`junitreport` with the `frames` format** may need Xalan on `antBridge`; `noframes` works with the default
  classpath.

### Why there is no shared `ant` service

The temptation: inject a service named `ant`, keep the `ant.properties` reads, and be on Gradle 10. The bridge refuses
to offer it, because it is the same global, mutable bag under a new name:

- Gradle cannot see the dependency between the task that writes and the one that reads, so ordering depends on
  `dependsOn` rather than on data.
- Up-to-date checks and the build cache ignore the value, so a changed value does not rerun or invalidate anything.
- Parallel execution and the configuration cache make it worse: writers race, and nothing stored in the cache carries
  the value.
- The build is stuck on the bridge, instead of one provider swap away from leaving it.

Use the provider forms in [Values Ant computes](#values-ant-computes) instead, then replace them with the Gradle-native
providers in the table above.

## Deviations from Gradle's Ant integration

Every known difference from Gradle's built-in Ant integration, intentional or not. Check a build against this list
before migrating it.

### `antBridge.importBuild`

- **Properties pass only along `depends`.** A target sees the properties of the targets it depends on, transitively,
  never those of a target that merely ran earlier in the same build (with legacy, that order-dependent leak worked).
  Fix: add the missing `depends`.
- **Only properties pass between targets.** Ant references (`id`) set by one target are not visible to another. Fix:
  define them at top level, or in the target that uses them.
- **Only newly defined properties pass.** A target's export is the names it added to the property table; changing an
  existing property (ant-contrib's `var`, `unset`) does not reach the targets downstream.
- **Every execution re-parses the build.** Each imported task re-parses every imported file of the project, so
  top-level side effects (an `echo`, a `mkdir`) run during configuration and again once per executed target, but not
  on a configuration-cache hit.
- **Untracked parse inputs.** Only the build files and the files read by top-level `property file=` and
  `loadproperties srcFile=` are configuration-cache inputs; a changed environment variable, URL, resource or command
  output read at top level does not invalidate the entry.
- **The build script's `ant` object no longer feeds or reads the import.** Set values before the import with
  `properties(...)` seeds, which must be known during configuration; read Ant's values with `propertiesFor`.
- **Build listeners, loggers and other per-project Ant state** configured through the legacy `ant` object do not apply.
- **Duplicate target names across imported files fail**, where Ant silently kept the first definition (see
  [Names and duplicates](#names-and-duplicates) for the other naming rules).
- **No shared Ant project between targets**, so a top-level `<antcall>` in a later file cannot reach an earlier file's
  targets.

### The bridge as a whole

- **A fresh Ant project for every execution:** nothing carries over between Ant calls, tasks or builds except through
  declared files and providers ([Values Ant computes](#values-ant-computes)).
- **No shared `ant` service** ([Why there is no shared `ant` service](#why-there-is-no-shared-ant-service)).
- **No read-backs and no unqualified calls inside a recorded block**, no task dependencies from values in `AntWork`'s
  tree, `AntOperations` waiting for all of the task's work items, `AntWork` without outputs never up to date, values
  Ant computes reaching Gradle only through providers, and `junitreport frames` needing Xalan: see
  [What cannot be migrated](#what-cannot-be-migrated).
- **Ant runs in its own class loader** (or process) from the `antBridge` configuration, not on the build script
  classpath, and its version is the one `antBridge` resolves, not the one Gradle bundles.

Heavily nonstandard Ant usage is unsupported: code relying on Ant internals, on JVM-global state carried between
targets, or on custom project helpers may not work with the bridge.

## Contributing

How the bridge loads and runs Ant (the isolated class loader, the Java shim and its boundary contract) is described in
[INTERNALS.md](INTERNALS.md).
