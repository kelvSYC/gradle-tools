#!/usr/bin/env bash
# Tests for release-lib.sh. Portable to bash 3.2.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../release-lib.sh
source "$here/../release-lib.sh"

failures=0
assert_eq() {
  local expected="$1" actual="$2" message="$3"
  if [ "$expected" = "$actual" ]; then
    echo "ok   $message"
  else
    echo "FAIL $message: expected '$expected', got '$actual'"
    failures=$((failures + 1))
  fi
}

assert_eq minor "$(labels_to_bump 0 release:major)" "0.x release:major -> minor"
assert_eq major "$(labels_to_bump 1 release:major)" "1.x release:major -> major"
assert_eq minor "$(labels_to_bump 0 release:minor)" "0.x release:minor -> minor"
assert_eq patch "$(labels_to_bump 0)" "no labels -> patch"
assert_eq patch "$(labels_to_bump 1 bug dependencies)" "unrelated labels ignored"
assert_eq major "$(labels_to_bump 1 bug release:minor release:major)" "highest label wins"

assert_eq minor "$(required_bump 0 breaking)" "0.x breaking -> minor"
assert_eq patch "$(required_bump 0 additive)" "0.x additive -> patch"
assert_eq major "$(required_bump 1 breaking)" "1.x breaking -> major"
assert_eq minor "$(required_bump 1 additive)" "1.x additive -> minor"
assert_eq patch "$(required_bump 0 none)" "0.x none -> patch"
assert_eq patch "$(required_bump 1 none)" "1.x none -> patch"

assert_eq release:minor "$(required_label 0 breaking)" "0.x breaking label"
assert_eq "" "$(required_label 0 additive)" "0.x additive label"
assert_eq release:minor "$(required_label 1 additive)" "1.x additive label"
assert_eq release:major "$(required_label 1 breaking)" "1.x breaking label"
assert_eq "" "$(required_label 1 none)" "1.x none label"

assert_eq v0.2.0 "$(next_version v0.1.1 minor)" "next minor"
assert_eq v0.2.4 "$(next_version v0.2.3 patch)" "next patch"
assert_eq v1.0.0 "$(next_version v0.9.0 major)" "next major"
assert_eq v0.1.0 "$(next_version "" patch)" "empty latest"

assert_eq minor "$(max_bump patch minor patch)" "max_bump"
assert_eq patch "$(max_bump patch)" "max_bump single"
assert_eq 2 "$(bump_rank major)" "bump_rank major"

if (bump_rank bogus) >/dev/null 2>&1; then
  assert_eq fail ok "bump_rank rejects bad input"
else
  assert_eq ok ok "bump_rank rejects bad input"
fi

tmp="$(mktemp -d)"
tmp2="$(mktemp -d)"
trap 'rm -rf "$tmp" "$tmp2"' EXIT
origin="$PWD"
cd "$tmp"
git init -q .
git -c user.name=t -c user.email=t@example.invalid commit -q --allow-empty -m init
for t in v0.1.0 v0.10.0 v0.9.1 v0.11.0-rc1 foo; do git tag "$t"; done
assert_eq v0.10.0 "$(latest_release_tag)" "latest_release_tag by version order"
cd "$tmp2"
git init -q .
assert_eq "" "$(latest_release_tag)" "latest_release_tag with no tags"
cd "$origin"

[ "$failures" -eq 0 ] || { echo "$failures failure(s)"; exit 1; }
echo "all passed"
