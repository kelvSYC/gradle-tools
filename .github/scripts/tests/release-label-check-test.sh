#!/usr/bin/env bash
# Tests for release-label-check.sh. Portable to bash 3.2.
set -euo pipefail

script="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/release-label-check.sh"
failures=0
assert_eq() {
  if [ "$1" = "$2" ]; then echo "ok   $3"; else echo "FAIL $3: expected '$1', got '$2'"; failures=$((failures + 1)); fi
}
# run <expected exit code> <description> <args...>; sets $out
run() {
  local want="$1" desc="$2" rc=0
  shift 2
  out="$("$script" "$@" 2>&1)" || rc=$?
  assert_eq "$want" "$rc" "$desc (exit code)"
}
contains() {
  case "$out" in *"$1"*) echo "ok   $2" ;; *) echo "FAIL $2: '$1' not in: $out"; failures=$((failures + 1)) ;; esac
}

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
cd "$tmp"
git init -q .
g() { git -c user.name=t -c user.email=t@example.invalid "$@"; }
mkdir -p cores/a/api
printf 'one\ntwo\n' > cores/a/api/a.api
g add -A
g commit -q -m base
base="$(git rev-parse HEAD)"
commit_change() { g add -A; g commit -q -m "$1"; git rev-parse HEAD; }

printf 'one\ntwo\nthree\n' > cores/a/api/a.api
additive="$(commit_change add)"
git reset -q --hard "$base"
printf 'one\nTWO\n' > cores/a/api/a.api
breaking="$(commit_change change)"
git reset -q --hard "$base"

# The PR is behind its base: only the PR's own change counts, not what the base gained since.
git reset -q --hard "$base"
echo pr > pr.txt
pr_head="$(commit_change prwork)"
git reset -q --hard "$base"
base_moved="$(git rev-parse HEAD)"
printf 'one\ntwo\nthree\n' > cores/a/api/a.api
base_moved="$(commit_change basemoved)"
run 0 "base gained API after the PR branched" "$base_moved" "$pr_head"
contains "none" "behind-base PR reports none"
git reset -q --hard "$base"

# No release tag yet: major is 0.
run 0 "no change, no label" "$base" "$base"
contains "none" "reports change kind none"
run 0 "additive on 0.x needs no label" "$base" "$additive"
run 1 "breaking on 0.x without label fails" "$base" "$breaking"
contains "cores/a/api/a.api" "failure names the changed api file"
contains "release:minor" "failure names the required label"
run 0 "breaking on 0.x with release:minor" "$base" "$breaking" release:minor
contains "minor" "success prints the bump"
run 0 "breaking on 0.x with release:major (acts as minor)" "$base" "$breaking" release:major
run 0 "unrelated labels are ignored on no change" "$base" "$base" bug docs
run 1 "unrelated labels do not satisfy breaking" "$base" "$breaking" bug
run 2 "bad ref fails" nonexistent-ref "$breaking"
run 2 "missing args fails"

# After 1.0.0, additive needs minor and breaking needs major.
git tag v1.0.0 "$base"
run 1 "additive on 1.x without label fails" "$base" "$additive"
contains "release:minor" "additive on 1.x requires release:minor"
run 0 "additive on 1.x with release:minor" "$base" "$additive" release:minor
run 1 "breaking on 1.x with release:minor fails" "$base" "$breaking" release:minor
contains "release:major" "breaking on 1.x requires release:major"
run 0 "breaking on 1.x with release:major" "$base" "$breaking" release:major

if [ "$failures" -ne 0 ]; then echo "$failures failure(s)"; exit 1; fi
echo "all passed"
