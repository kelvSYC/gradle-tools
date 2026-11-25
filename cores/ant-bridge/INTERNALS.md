# Ant Bridge internals

A maintainer's tour of how the bridge runs Ant. Consumer documentation is in [README.md](README.md); this file explains
the plumbing behind it and why it is shaped this way. Keep it current when that plumbing changes.

## The constraint: Ant is never on a Gradle classpath

Ant is never on the consumer's build-script classpath or on the plugin's own classpath:

- Gradle 8.14 to 9.x bundle their own Ant and put it on the build-script classpath. A plugin compiled against Ant
  would get whichever Ant Gradle happens to ship, not the one the consumer asked for, and Gradle 10 removes it.
- Consumers choose the Ant version and add antlibs through the `antBridge` configuration. Only an isolated class
  loader can honour that choice next to Gradle's own Ant.

So no class in `src/main` references an `org.apache.tools.ant` type. `verifyGradleLinkage` enforces this: an Ant
reference in the plugin's classes would be unresolvable against the Gradle API it checks.

## The pieces

```
antBridge configuration ──resolved──▶ Ant jars ─┐
                                                 ├─▶ AntRuntimeClassLoader (parent: platform loader)
plugin jar: ant-bridge-shim/**.class.bin ───────┘            │ defines
                                                             ▼
AntWorkAction / AntProperties ──▶ AntShim ──JDK maps──▶ shim entry point (TreeExecutor, PropertyLoader,
                                                             │        BuildReader, TargetRunner)
                                                             │
                                                             ▼
                                                        Ant Project
```

| Piece | Where | Role |
|---|---|---|
| The shim | `src/antShim/java/.../ant/shim/` | The only Ant-aware code. `TreeExecutor` turns element trees into Ant tasks and runs them; `PropertyLoader` runs Ant's `Property` task per source (`RecordingProperty` records the keys each source names); `BuildReader` parses build files with Ant's own project helper and reports their targets, top-level properties and loaded files; `TargetRunner` re-parses them and runs one target without its dependencies; `ShimSupport` holds the shared project setup, build-file parsing, logging bridge and result maps. |
| `AntRuntimeClassLoader` | `src/main/.../ant/internal/` | A `URLClassLoader` over the resolved `antBridge` jars whose parent is the platform class loader. It defines the shim classes from the plugin's resources. |
| `AntShim` | `src/main/.../ant/internal/` | The plugin-side adapter: converts requests to JDK types, invokes a shim entry point by class name, and turns the result back into a value or an `AntExecutionException`. |
| Callers | `AntWorkAction` (`actions`, public), `AntProperties` | Open a loader, call through `AntShim`, close the loader. Everything else (`AntWork`, the typed tasks, `AntOperations`) reaches Ant through `AntWorkAction`, whose trees come from `AntTrees`. |

## Why the shim is plain Java

The shim runs in a loader whose parent is the platform class loader, so it sees the JDK and Ant and nothing else: no
Gradle API and no Kotlin stdlib. Kotlin code compiles to calls into the Kotlin stdlib (`Intrinsics` null checks,
collection helpers), which would fail with `NoClassDefFoundError` the first time they ran. Java compiled against Ant
needs only the JDK and Ant. That is also why detekt, which lints Kotlin, does not cover the shim.

## Why the shim ships as `.class.bin` resources

`processResources` copies the `antShim` source set's classes into the plugin jar under `ant-bridge-shim/`, renamed to
`<Name>.class.bin` (see `build.gradle.kts`). The renaming does two things:

- The plugin's own class loader never finds these classes, so they cannot be loaded next to Gradle's Ant by mistake.
- `verifyGradleLinkage` scans the jar's `.class` entries; it does not see the shim's Ant references.

`AntRuntimeClassLoader.findClass` reads those resources through the plugin's class loader and defines each class in
the isolated loader, next to the consumer's Ant. The `antShim` source set compiles against `local.ant` and
`local.ant.junit` as `compileOnly`, so its Ant dependency never reaches the published POM.

## The boundary contract

The plugin and the shim are in different class loaders, so a class they both name is a different class on each side.
The only types they share are the JDK's. Across the boundary:

- **Entry points** are classes implementing `java.util.function.Function<Map<String, Object>, Map<String, Object>>`,
  instantiated by name (`AntShim.TREE_EXECUTOR`, `AntShim.PROPERTY_LOADER`, `AntShim.BUILD_READER`,
  `AntShim.TARGET_RUNNER`).
- **Requests** are maps of strings, lists and maps. `AntNode.toShimMap()` and `AntPropertySource.toShimMap()` build
  them; the request keys of each entry point are documented in its class comment (`TreeExecutor`, `PropertyLoader`,
  `BuildReader`, `TargetRunner`). Every request carries `"logger"`. `TreeExecutor` and `PropertyLoader` take
  `"baseDirectory"`; `BuildReader` and `TargetRunner` take `"files"` instead, each with its own base directory. `TreeExecutor` also accepts `"results"`, a list of property
  names to report after the tree runs.
- **Logging** is a `java.util.function.BiConsumer<Integer, String>` of Ant priority and message. `AntShim` maps Ant's
  priorities to Gradle's logger: error, warn, info, and debug for verbose and debug.
- **Results** are maps. Success has `"antVersion"` and `"properties"` (`PropertyLoader`, `BuildReader` and
  `TargetRunner` always; `TreeExecutor` always, empty when no `"results"` were requested). `BuildReader` adds
  `"targets"` and `"loadedFiles"`. For `TreeExecutor`, `"properties"` maps each requested name the Ant
  project defines to its value (names the tree did not set are left out). Failure has
  `"failure"` (Ant's message), `"location"` and `"trace"` (the stack trace as a string).

**Failures are values, never exceptions.** An exception thrown from the shim carries Ant classes in its cause chain,
which the plugin side cannot catch by type and which can fail to render or to serialize back from a worker process.
`ShimSupport.runInProject` therefore catches `BuildException`, `RuntimeException` and `LinkageError` (an antlib missing
a dependency) and returns a failure map; `AntShim` turns that into an `AntExecutionException` and logs the trace at
debug. Keep that rule when adding an entry point: nothing may escape `apply`.

**The thread context class loader** is set to the isolated loader for the duration of a call (`AntShim.apply`). JAXP
(used by `xslt` and `junitreport`) and Ant's project-helper lookup load classes through it.

**Every call gets a fresh Ant `Project`**, so properties and references never leak between executions, including
parallel ones. `fireBuildFinished` runs at the end, which closes the class loaders Ant creates for `taskdef
classpath=…`.

## Result properties and the result file

Work actions cannot return values, and the Worker API may run them in another process, so `AntWorkAction` writes the
requested properties (`resultProperties`) to `resultFile` and the caller reads that file back. The format is
deterministic: Java properties, keys in name order, `$` escaped as `$$` (so Ant's `property file=` reads the values
exactly), names the tree did not set absent. `AntWork`'s export file and `AntOperations`' result overloads use the same
mechanism: `AntWork` points `resultFile` at its declared `exportedPropertiesFile`, while `AntOperations` uses a
temporary file in the system temp directory (owner-only on POSIX), deleted after reading, also on failure. The
configuration-time `AntQuery` has no work action (a `ValueSource` cannot use the Worker API) and reads `"properties"`
from `TreeExecutor` directly.

## Parsing several top-level build files into one project

`ShimSupport.parseBuildFiles` (used by `BuildReader` and `TargetRunner`) parses each file with a new `ProjectHelper`
from `ProjectHelperRepository`, as `ProjectHelper.configureProject` does, but Ant expects one top-level file per
project, so before each file it:

- removes Ant's parsing context (the `ant.parsing.context` reference of `ProjectHelper2`) and the unnamed implicit
  target. A context left over from an earlier file would parse the next one as an import of it (its `<project>`
  attributes ignored, its top-level tasks appended to the earlier implicit target and run again), and the implicit
  target's name would be a duplicate;
- sets the base directory itself, because `ProjectHelper2` keeps a `basedir` property that is already set. It reads the
  file's own `<project basedir>` with a JDK SAX pass that resolves no entity or DTD (a file that pass cannot read is
  left to Ant's parse to report), and also lists the targets the file defines directly. Ant silently ignores a target
  whose name is already registered, so a name an earlier top-level file registered (itself or through its imports)
  fails instead, naming both files.

The loaded files are each top-level helper's import stack after its parse, which holds the file and everything it
imported or included (resources without a file, such as imports from a jar, are left out).

## Decision: no shared Ant state between tasks

A `BuildService` (or any build-scoped holder) carrying Ant properties from one task to another is rejected, and the
bridge must not grow one, even as a migration aid. It recreates the global `ant` property bag: Gradle cannot see the
producer-consumer dependency (ordering by `dependsOn`, not by data), up-to-date checks and the build cache ignore the
value, parallel runs and the configuration cache make it worse, and consumers stay on the bridge. Values reach other
tasks only as declared task outputs and providers derived from them (`exportedProperties`, a custom task's own
output), or at configuration time through `antBridge.query`, which a configuration-cache entry can validate. Each Ant
call keeps its fresh `Project` for the same reason.

## Who opens loaders, and why there is no cache

- `AntWorkAction` opens and closes a loader per work item. Build services are unavailable under process isolation, and
  Gradle already reuses the worker's own class loaders. Because the action isolates Ant itself, its isolation does not
  depend on the queue's isolation mode or classpath, which is why it is public API that consumers may submit to any
  work queue.
- `AntProperties.obtain()` opens and closes a loader per read. A cached loader in a `BuildService` was tried and
  removed: a configuration-time `ValueSource` is part of the configuration cache's fingerprint, and a
  `BuildServiceProvider` parameter cannot be serialized there.

A per-build cache is possible later if reloading Ant shows up in profiles; it must not live in a `ValueSource`
parameter.

## How tests get real Ant

Tests run real Ant, never mocks of it. The build resolves `antTestClasspath` (Ant and `ant-junit`) and
`antContribTestClasspath` (ant-contrib without its old Ant) and passes them to the test JVM as the
`antBridge.test.antClasspath` and `antBridge.test.contribClasspath` system properties; `TestAnt` reads them. Unit tests
build an `AntRuntimeClassLoader` over `TestAnt.classpath`, so they exercise the same loader and shim resources as
production. `antBridge.test.catalogAntVersion` lets `AntBridgePluginSpec` check that the plugin's default Ant version
matches the catalog.

The integration tests (`src/integrationTest`) run TestKit builds that resolve Ant from Maven Central, under the
configuration cache. The Gradle-version specs (`src/test/.../plugins/gradleversions`) repeat a smoke test on the oldest
supported Gradle and on the running one.
