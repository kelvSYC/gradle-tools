#!/usr/bin/env bash
# Usage: release-plan.sh <sha> <auto|patch|minor|major>
# Plans the release of <sha>: the bump is the largest of the labels of the PRs merged since the latest vX.Y.Z tag and
# of what the public-API change over that range requires, or the given bump, which may not be lower. Needs `gh` with
# GH_TOKEN and GITHUB_REPOSITORY (owner/repo). Prints version=, bump=, latest=, labels=, api=, raised= (only when the
# API change raised the labels' bump), then one pr=#<number> <labels> line per PR and one direct=<sha> line per commit
# with no PR. Exits 1 when there is nothing to release or the given bump is too low, 2 on bad input.
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo "usage: $0 <sha> <auto|patch|minor|major>" >&2
  exit 2
fi

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=release-lib.sh
. "$here/release-lib.sh"

input="$2"
case "$input" in
  auto | patch | minor | major) ;;
  *) echo "Invalid bump '$input': expected auto, patch, minor or major." >&2; exit 2 ;;
esac
sha="$(git rev-parse --verify --quiet "$1^{commit}")" || { echo "Not a commit: $1" >&2; exit 2; }
repo="${GITHUB_REPOSITORY:?GITHUB_REPOSITORY (owner/repo) is required}"

latest="$(latest_release_tag)"
major=0
if [ -n "$latest" ]; then
  if ! git merge-base --is-ancestor "$latest" "$sha"; then
    echo "The latest release tag $latest is not an ancestor of $sha." >&2
    exit 1
  fi
  major="${latest#v}"
  major="${major%%.*}"
  commits="$(git rev-list --first-parent "$latest..$sha")"
  change="$("$here/api-change.sh" "$latest" "$sha")" || exit 2
else
  # No release yet: there is no base to diff the API against.
  commits="$(git rev-list --first-parent "$sha")"
  change=none
fi
if [ -z "$commits" ]; then
  echo "No commits since ${latest:-the start of history}; nothing to release." >&2
  exit 1
fi

labels=()
prs=""
seen=""
direct=""
for commit in $commits; do
  pulls="$(gh api --paginate "repos/$repo/commits/$commit/pulls")"
  found="$(jq -r '.[] | select(.merged_at != null) | "\(.number)\t\([.labels[].name] | join(","))"' <<<"$pulls")"
  if [ -z "$found" ]; then
    direct="$direct$commit
"
    continue
  fi
  while IFS="$(printf '\t')" read -r number names; do
    # A PR merged as several first-parent commits is listed once.
    case " $seen " in *" $number "*) continue ;; esac
    seen="$seen $number"
    prs="${prs}pr=#$number${names:+ $names}
"
  done <<<"$found"
  while IFS= read -r name; do
    labels+=("$name")
  done < <(jq -r '.[] | select(.merged_at != null) | .labels[].name' <<<"$pulls")
done

label_bump="$(labels_to_bump "$major" ${labels[@]+"${labels[@]}"})"
api_bump="$(required_bump "$major" "$change")"
auto_bump="$(max_bump "$label_bump" "$api_bump")"

if [ "$input" = auto ]; then
  bump="$auto_bump"
elif [ "$(bump_rank "$input")" -lt "$(bump_rank "$auto_bump")" ]; then
  echo "The bump $input is below the $auto_bump that the labels ($label_bump) and the API change ($change) require." >&2
  exit 1
else
  bump="$input"
fi

echo "version=$(next_version "$latest" "$bump")"
echo "bump=$bump"
echo "latest=$latest"
echo "labels=$label_bump"
echo "api=$change"
if [ "$(bump_rank "$api_bump")" -gt "$(bump_rank "$label_bump")" ]; then
  echo "raised=$label_bump->$api_bump"
fi
printf '%s' "$prs"
printf '%s' "$direct" | sed 's/^/direct=/'
