#!/usr/bin/env bash
# Release helper functions; source this file. Portable to bash 3.2 and GNU/BSD tools.

# Print the highest vX.Y.Z tag by numeric version order, or nothing.
latest_release_tag() {
  git tag --list 'v*' \
    | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' \
    | sed 's/^v//' \
    | sort -t . -k1,1n -k2,2n -k3,3n \
    | tail -n 1 \
    | sed 's/^\(.\)/v\1/' || true
}

# bump_rank <patch|minor|major> -> 0|1|2
bump_rank() {
  case "${1:-}" in
    patch) echo 0 ;;
    minor) echo 1 ;;
    major) echo 2 ;;
    *) echo "bump_rank: invalid bump '${1:-}'" >&2; return 1 ;;
  esac
}

# rank_bump <0|1|2> -> patch|minor|major
rank_bump() {
  case "${1:-}" in
    0) echo patch ;;
    1) echo minor ;;
    2) echo major ;;
    *) echo "rank_bump: invalid rank '${1:-}'" >&2; return 1 ;;
  esac
}

# max_bump <bump>... -> the largest bump (patch when none given)
max_bump() {
  local best=0 rank bump
  for bump in "$@"; do
    rank="$(bump_rank "$bump")" || return 1
    if [ "$rank" -gt "$best" ]; then best="$rank"; fi
  done
  rank_bump "$best"
}

# labels_to_bump <major> <label>... -> patch|minor|major (0.x: release:major gives minor)
labels_to_bump() {
  local major="${1:?labels_to_bump: major required}"
  shift
  local bump=patch label
  for label in "$@"; do
    case "$label" in
      release:major)
        if [ "$major" = 0 ]; then bump="$(max_bump "$bump" minor)"; else bump="$(max_bump "$bump" major)"; fi
        ;;
      release:minor) bump="$(max_bump "$bump" minor)" ;;
      *) ;;
    esac
  done
  echo "$bump"
}

# required_bump <major> <none|additive|breaking> -> patch|minor|major
required_bump() {
  local major="${1:?required_bump: major required}" change="${2:-}"
  case "$change" in
    none) echo patch ;;
    additive) if [ "$major" = 0 ]; then echo patch; else echo minor; fi ;;
    breaking) if [ "$major" = 0 ]; then echo minor; else echo major; fi ;;
    *) echo "required_bump: invalid change '$change'" >&2; return 1 ;;
  esac
}

# required_label <major> <none|additive|breaking> -> release:major|release:minor|empty
required_label() {
  local bump
  bump="$(required_bump "$@")" || return 1
  case "$bump" in
    patch) echo "" ;;
    minor) echo release:minor ;;
    major) echo release:major ;;
  esac
}

# next_version <vX.Y.Z|empty> <bump> -> vX.Y.Z (empty latest gives v0.1.0)
next_version() {
  local latest="${1:-}" bump="${2:-}"
  bump_rank "$bump" >/dev/null || return 1
  if [ -z "$latest" ]; then
    echo v0.1.0
    return 0
  fi
  local ver="${latest#v}" major minor patch
  IFS=. read -r major minor patch <<EOT
$ver
EOT
  case "$bump" in
    major) echo "v$((major + 1)).0.0" ;;
    minor) echo "v$major.$((minor + 1)).0" ;;
    patch) echo "v$major.$minor.$((patch + 1))" ;;
  esac
}
