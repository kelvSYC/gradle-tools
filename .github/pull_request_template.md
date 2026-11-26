## Release

Add the label `release:minor` (while on 0.x) if this PR:

- removes or changes public API (CI enforces this from the `cores/*/api/*.api` dumps)
- raises a core's minimum Gradle tier
- changes plugin or task defaults
- renames a task, extension or plugin ID
- changes only `gradle-extensions`' `src/gradle99` API

Otherwise, no label.
