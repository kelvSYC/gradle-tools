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
| Callers | `AntWorkAction` (`actions`, public), `AntProperties`, `AntQuery`, and `AntImportRegistry` and `AntTargetAction` (`internal`) | Open a loader, call through `AntShim`, close the loader. Everything else (`AntWork`, the typed tasks, `AntOperations`) reaches Ant through `AntWorkAction`, whose trees come from `AntTrees`; `antBridge.importBuild` reads the build through `AntImportRegistry` and runs targets through `AntTargetAction` (see [How `importBuild` works](#how-importbuild-works)). |

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
  `"baseDirectory"`; `BuildReader` and `TargetRunner` take `"files"` instead, each with its own base directory.
  `TreeExecutor` also accepts `"results"`, a list of property names to report after the tree runs.
- **Logging** is a `java.util.function.BiConsumer<Integer, String>` of Ant priority and message. `AntShim` maps Ant's
  priorities to Gradle's logger: error, warn, info, and debug for verbose and debug.
- **Results** are maps. Success has `"antVersion"` and `"properties"` (`PropertyLoader`, `BuildReader` and
  `TargetRunner` always; `TreeExecutor` always, empty when no `"results"` were requested). `BuildReader` adds
  `"targets"`, `"loadedFiles"` and `"readFiles"`. For `TreeExecutor`, `"properties"` maps each requested name the Ant
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
- sets the base directory itself, to the file's `"baseDirectory"`, else the file's directory, as legacy `importBuild`
  did. `ProjectHelper2` keeps a `basedir` property that is already set, so the file's own `<project basedir>` is
  ignored, as it was with legacy;
- lists the targets and extension points the file defines directly (in no namespace or in
  `antlib:org.apache.tools.ant`, as Ant reads them) with a JDK SAX pass that resolves no entity or DTD (a file that
  pass cannot read is left to Ant's parse to report);
- hides the earlier files' targets (not their extension points, which the file may extend) during the parse. Ant
  silently ignores a target whose name is already registered, so without this a target that one file defines or
  imports would vanish behind an earlier file's. A name an earlier top-level file registered (itself or through its
  imports) fails instead, naming both files; within one top-level file, Ant's own rule stays (the importing file's
  target wins). A file imported by two top-level files is parsed, and its top-level statements run, once per
  importer; its targets are the same definitions, so they are not duplicates and belong to the first importer.

`TargetRunner` runs a target with the base directory and `ant.file` of the top-level file that registered it, as Ant
runs an imported target with its importer's. It applies the upstream exports after the parse, so the build's top-level
statements never see them, as in a single Ant run. `BuildReader` computes each target's upstream with Ant's
`topoSort`, so a dependency cycle anywhere in the build fails the whole read.

The loaded files are each top-level helper's import stack after its parse, which holds the file and everything it
imported or included (resources without a file, such as imports from a jar, are left out).

## How `importBuild` works

The two shim entry points behind it:

- `BuildReader` (`AntShim.readBuild`): request `"files"` (each `"file"` with an optional `"baseDirectory"`),
  `"seeds"`, `"logger"`. Result `"targets"` (per name Ant registered: `"name"`, `"file"`, `"description"`,
  `"depends"` as Ant resolved them, `"upstream"` in Ant's execution order from `topoSort`), `"properties"` (the seeds
  and what the parse set), `"loadedFiles"` and `"readFiles"` (the files `property file=`, `loadproperties` and
  `import`/`include` read or looked for during the parse, missing ones included, recorded by a build listener from
  each finished task's attribute).
- `TargetRunner` (`AntShim.runTarget`): request `"files"`, `"seeds"`, `"exports"` (property maps in load order),
  `"target"`, `"logger"`. Result `"properties"`: the target's newly defined properties.

### Configuration: reading the build

`AntBridgeExtension.importBuild` configures an `AntImportSpec` and hands it to the project's `AntImportRegistry` (one
per extension, so one per Gradle project). The registry:

- **Reads the whole build on every call**: the files so far plus the new one, in call order, with the call's Ant
  classpath (a loader opened and closed for the read) and the seeds accumulated so far, resolved here. This runs in
  ordinary configuration code, not in a `ValueSource`, so the parse (and its top-level side effects) is not repeated on
  a configuration-cache hit.
- **Validates before committing**: the exclusions must name targets the file adds, and no task name may clash (with an
  existing task, or two targets of the call). Only then does it replace its state (files, seeds, targets, task names,
  top-level properties), so a failing call changes nothing.
- **Registers cache inputs** by reading every loaded and read file through `providers.fileContents(...).asBytes.orNull`:
  a file content obtained during configuration is a configuration-cache input, and a missing file is fingerprinted by
  its absence, so creating it (a `local.properties`, an optional import) invalidates the entry. Only the existing files
  go into the tasks' `buildFiles` input. Inputs the shim does not report (environment, URLs, resources, commands) are
  not tracked.
- **Registers tasks eagerly and wires them lazily.** The call registers (`tasks.register`) an `AntImportedTarget` per
  target it adds, so `tasks.named` works right after it, but each task's `dependsOn` and `upstreamExports` are providers
  evaluated against the registry's final state, so later calls can still satisfy a `depends` or extend an extension
  point. The values every task shares (top-level files, base directories, seeds, top-level properties) are properties
  of the registry that `finalizeValueOnRead`; the provider behind each marks the registry frozen when it is first read,
  and `importBuild` fails once it is frozen, since an earlier task would otherwise have seen a different build. The
  build-file collection is a plain provider instead, because Gradle queries it while resolving task dependencies
  without finalizing it.
- **Keeps the `depends` order** with a `tasks.configureEach` hook giving every task of the project a lazy
  `mustRunAfter` on the task named before it in an imported target's `depends` (Gradle tasks and replacements
  included). Legacy used `shouldRunAfter`, which parallel execution, the configuration cache's default, ignores. The
  pairs are computed once per build state, in target name order, and a pair is dropped when its second name already
  runs before its first through Ant's `depends` or the pairs kept so far, so the ordering cannot form a cycle with the
  dependencies Ant knows of (dependencies only Gradle knows of are not checked).
- **Logs the exclusion warning** in `afterEvaluate` (registered by the plugin), after every import of a normal build
  script has run.

### Execution: one target per task

`AntImportedTarget` deletes its export file and submits `AntTargetAction` to a class-loader- or process-isolated
queue; the action opens its own loader from the Ant classpath, as `AntWorkAction` does. `TargetRunner` then, in a fresh
project:

1. sets the seeds as user properties;
2. re-parses every top-level file (see [Parsing several top-level build files into one
   project](#parsing-several-top-level-build-files-into-one-project)), so each execution rebuilds its starting state
   from the files instead of receiving it;
3. applies the upstream exports in order with `PropertyHelper.setNewProperty`, so the first definition wins;
4. enters the owning top-level file's base directory and `ant.file`, snapshots the property names, runs only the
   target (`Target.performTasks()`, so `if`/`unless` apply and its `depends` do not run), and reports the names added
   since the snapshot.

`AntTargetAction` writes them with `PropertiesFiles` (the `$$` escaping of the result-file format). Measuring by name
means a changed existing property is not exported.

### Exports, upstream order and exclusions

The registry maps each name in a target's `"upstream"` list to an export-file provider: an imported target's
`exportedPropertiesFile`, an excluded target's materialised export, or nothing (an excluded target without supplied
properties, or a name that is not a target). Because the providers come from the producing tasks' outputs,
`upstreamExports` is a declared input that carries the task dependencies; no task reads another's file otherwise.

An exclusion that supplies properties is materialised as an `AntExclusionExport` task (`<task name>AntProperties`)
writing the provider's values to a file in the same format. Downstream wiring then treats imported targets and
replacements alike (a list of file providers in Ant's order), the replacement's values are a task input with its
dependency, and the work action takes only files.

`AntImport.propertiesFor` merges the same pieces on the Gradle side: seeds, then top-level properties, then each
upstream export, zipped first-wins, so it carries the same dependencies as the target's task would.

## Decision: no adapter for Ant filter readers

Ant's filter readers (`ReplaceTokens` and friends in `Copy` filters) are not wrapped. They are plain
`java.io.FilterReader` classes that Gradle instantiates itself, without Ant's runtime, and Gradle's
[announcement][gradle-blog-keep-filters] gives a supported, faithful path: Ant as a build script
dependency from Gradle 10. An adapter running them through the isolated loader would need the Ant classpath threaded
through filter properties that Gradle does not track as inputs, and a loader per filtered file or a loader cache
shared across tasks, which edges towards the shared state the bridge avoids, for little gain over that one dependency
line. The README's "Ant filter readers in Copy tasks" section documents the options.

## Decision: no shared Ant state between tasks

A `BuildService` (or any build-scoped holder) carrying Ant properties from one task to another is rejected, and the
bridge must not grow one, even as a migration aid. It recreates the global `ant` property bag: Gradle cannot see the
producer-consumer dependency (ordering by `dependsOn`, not by data), up-to-date checks and the build cache ignore the
value, parallel runs and the configuration cache make it worse, and consumers stay on the bridge. Values reach other
tasks only as declared task outputs and providers derived from them (`exportedProperties`, a custom task's own
output), or at configuration time through `antBridge.query`, which a configuration-cache entry can validate. Each Ant
call keeps its fresh `Project` for the same reason.

## Decision: the import's Ant build is derived, not shared

Legacy `ant.importBuild` parsed into the project's one long-lived Ant project and every target ran in it. The
tempting port (parse once, keep the project or its property table in a `BuildService`, run each target in it) is the
shared bag of the previous decision, so the import is built from these guardrails instead:

- **Nothing holds Ant state between executions**: no `BuildService`, static, `object` or long-lived field with Ant
  projects, properties, references or parsed files. The project's Ant build is an immutable list of files (plus seeds
  and base directories) fixed at configuration, and every execution rebuilds its starting state from it by
  re-parsing. The cost is a parse per target and top-level side effects per execution, documented as deviations.
- **Values between targets are declared files.** A target's new properties are its declared output; downstream tasks
  declare the upstream export files as inputs. Gradle sees every edge, so parallel execution, the configuration cache
  and a later native replacement all keep working.
- **Visibility follows the `build.xml` graph**, never run order: a target loads only its upstream targets' exports.
  Recreating run-order visibility would need shared state and make results depend on scheduling.
- **Only properties pass.** References are live Ant objects that cannot cross a process or a file.

`AntImportRegistry` is filled during configuration and frozen when its values are first read. It holds what the calls
declared and what reading the build reported, and computes each task's wiring; the tasks receive plain values
(finalized when read) in their own properties, which is what the configuration cache stores. The registry holds no
runtime state: without the configuration cache, the lazy providers it hands out (task dependencies, ordering, upstream
exports, `propertiesFor`) may still read its frozen configuration during the execution phase, and the ordering
provider caches the pairs it computes when the task graph is built, but no work action reaches it, no Ant state or
task result is ever written to it, and with the configuration cache it is not part of the stored state at all. It
could not be serialized into the configuration cache, is unavailable in a worker process, and, if it carried values
at execution, would be the shared bag again.

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

[gradle-blog-keep-filters]: https://blog.gradle.org/side-quest-removing-ant#keep-ants-filters-by-adding-ant-yourself
