@AGENTS.md

## Claude Code Specifics

`.claude/settings.json` registers a `PostToolUse` hook that, after every file write, appends a missing trailing newline to `.kt` files and reports likely detekt violations (wildcard imports, forbidden comments, generic exception types). Follow the detekt rules in `AGENTS.md` anyway; the hook is a safety net, not a substitute.
