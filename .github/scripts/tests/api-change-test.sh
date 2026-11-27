#!/usr/bin/env bash
# Tests for api-change.sh. Portable to bash 3.2.
set -euo pipefail

script="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/api-change.sh"
failures=0
assert_eq() {
  if [ "$1" = "$2" ]; then echo "ok   $3"; else echo "FAIL $3: expected '$1', got '$2'"; failures=$((failures + 1)); fi
}

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
origin="$PWD"
cd "$tmp"
git init -q .
g() { git -c user.name=t -c user.email=t@example.invalid "$@"; }
mkdir -p cores/a/api cores/b/api cores/a/src
printf 'one\ntwo\n' > cores/a/api/a.api
printf 'x\n' > cores/b/api/b.api
echo src > cores/a/src/f.txt
g add -A
g commit -q -m base
base="$(git rev-parse HEAD)"

commit_change() { g add -A; g commit -q -m "$1"; git rev-parse HEAD; }

printf 'one\ntwo\nthree\n' > cores/a/api/a.api
head="$(commit_change add)"
assert_eq additive "$("$script" "$base" "$head")" "added lines only"

git reset -q --hard "$base"
printf 'one\nTWO\n' > cores/a/api/a.api
head="$(commit_change change)"
assert_eq breaking "$("$script" "$base" "$head")" "changed line"

git reset -q --hard "$base"
mkdir -p cores/new/api
printf 'n\n' > cores/new/api/new.api
head="$(commit_change new)"
assert_eq additive "$("$script" "$base" "$head")" "new api file"

git reset -q --hard "$base"
git rm -q cores/b/api/b.api
head="$(commit_change delete)"
assert_eq breaking "$("$script" "$base" "$head")" "deleted api file"

git reset -q --hard "$base"
echo more >> cores/a/src/f.txt
mkdir -p cores/a/build/api
echo gen > cores/a/build/api/a.api
head="$(commit_change outside)"
assert_eq none "$("$script" "$base" "$head")" "change outside api/ (incl. build/)"

git reset -q --hard "$base"
printf 'one\n--two\n' > cores/a/api/a.api
git add -A; g commit -q -m dashes
base2="$(git rev-parse HEAD)"
printf 'one\n' > cores/a/api/a.api
head="$(commit_change dashremoved)"
assert_eq breaking "$("$script" "$base2" "$head")" "removed line starting with --"

git reset -q --hard "$base"
git mv cores/a/api/a.api cores/a/api/renamed.api
head="$(commit_change rename)"
assert_eq breaking "$("$script" "$base" "$head")" "renamed api file"

if "$script" nonexistent-ref HEAD >/dev/null 2>&1; then
  assert_eq nonzero zero "bad base ref exits non-zero"
else
  assert_eq nonzero nonzero "bad base ref exits non-zero"
fi

cd "$origin"
[ "$failures" -eq 0 ] || { echo "$failures failure(s)"; exit 1; }
echo "all passed"
