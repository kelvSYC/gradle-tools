#!/usr/bin/env bash
# Usage: release-label-check.sh <base-ref> <head-ref> [label...]
# Fails when the PR's release labels do not cover the public-API change between the refs.
set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "usage: $0 <base-ref> <head-ref> [label...]" >&2
  exit 2
fi

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=release-lib.sh
. "$here/release-lib.sh"

base="$1" head="$2"
shift 2

# Measure only the PR's own change: a PR behind its base must not see the base's newer changes reversed.
fork="$(git merge-base "$base" "$head")" || exit 2
change="$("$here/api-change.sh" "$fork" "$head")" || exit 2
latest="$(latest_release_tag)"
major=0
if [ -n "$latest" ]; then
  major="${latest#v}"
  major="${major%%.*}"
fi

have="$(labels_to_bump "$major" "$@")"
need="$(required_bump "$major" "$change")"

if [ "$(bump_rank "$have")" -lt "$(bump_rank "$need")" ]; then
  files="$(git diff --name-only --no-renames "$fork" "$head" -- ':(glob)cores/*/api/*.api')"
  echo "::error::Public API change is $change (latest release: ${latest:-none}), which needs label $(required_label "$major" "$change"), but the PR resolves to a $have bump."
  echo "Changed API dumps:"
  echo "$files" | sed 's/^/  /'
  exit 1
fi

echo "API change: $change; release bump: $have (required at least: $need)"
