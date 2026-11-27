#!/usr/bin/env bash
# Tests for release-plan.sh, with gh replaced by a PATH shim serving canned JSON. Portable to bash 3.2.
set -euo pipefail

script="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/release-plan.sh"
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
lacks() {
  case "$out" in *"$1"*) echo "FAIL $2: '$1' in: $out"; failures=$((failures + 1)) ;; *) echo "ok   $2" ;; esac
}

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

# The gh shim: `gh api [flags] repos/<repo>/commits/<sha>/pulls` prints $tmp/pulls/<sha>.json, else [].
mkdir -p "$tmp/bin" "$tmp/pulls"
cat > "$tmp/bin/gh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
[ "${1:-}" = api ] || { echo "gh shim: unsupported: $*" >&2; exit 1; }
shift
[ -z "${GH_SHIM_FAIL:-}" ] || { echo "gh shim: HTTP 500" >&2; exit 1; }
path=""
for arg in "$@"; do
  case "$arg" in repos/*) path="$arg" ;; *) ;; esac
done
case "$path" in
  "repos/$GITHUB_REPOSITORY/commits/"*/pulls) ;;
  *) echo "gh shim: unexpected path '$path'" >&2; exit 1 ;;
esac
sha="${path#repos/"$GITHUB_REPOSITORY"/commits/}"
sha="${sha%/pulls}"
if [ -f "$GH_SHIM_DIR/$sha.json" ]; then cat "$GH_SHIM_DIR/$sha.json"; else echo '[]'; fi
EOF
chmod +x "$tmp/bin/gh"
export PATH="$tmp/bin:$PATH" GH_SHIM_DIR="$tmp/pulls" GITHUB_REPOSITORY=owner/repo

# pr <sha> <number> [label...]: the commit is the merge of PR <number> with the labels.
pr() {
  local sha="$1" number="$2" labels="" label
  shift 2
  for label in "$@"; do labels="$labels${labels:+,}{\"name\":\"$label\"}"; done
  printf '[{"number":%s,"merged_at":"2026-11-26T00:00:00Z","labels":[%s]}]\n' "$number" "$labels" > "$tmp/pulls/$sha.json"
}

mkdir "$tmp/repo"
cd "$tmp/repo"
git init -q .
g() { git -c user.name=t -c user.email=t@example.invalid "$@"; }
commit() { g add -A; g commit -q --allow-empty -m "$1"; git rev-parse HEAD; }
mkdir -p cores/a/api
printf 'one\ntwo\n' > cores/a/api/a.api
root="$(commit root)"

# No release tag: the API change counts as none, the first version is v0.1.0.
run 0 "no tag at all" "$root" auto
contains "version=v0.1.0" "no tag gives v0.1.0"
contains "latest=" "no tag prints an empty latest"

g tag -a v0.1.1 -m v0.1.1 "$root"
run 1 "no commits since the latest tag" "$root" auto
contains "No commits" "no commits reason"

# Unlabelled PR with an additive API change on 0.x: patch.
printf 'one\ntwo\nthree\n' > cores/a/api/a.api
additive="$(commit additive)"
pr "$additive" 11
run 0 "unlabelled additive on 0.x" "$additive" auto
contains "version=v0.1.2" "additive on 0.x gives v0.1.2"
contains "bump=patch" "additive on 0.x is patch"
contains "latest=v0.1.1" "latest is printed"
contains "pr=#11" "the PR is listed"
lacks "direct=" "no direct commits"

# A release:minor PR: minor.
echo x > x.txt
minor="$(commit minor)"
pr "$minor" 12 release:minor bug
run 0 "release:minor PR" "$minor" auto
contains "version=v0.2.0" "release:minor gives v0.2.0"
contains "pr=#12 release:minor,bug" "the PR's labels are listed"
contains "pr=#11" "earlier PRs are listed"
run 1 "input patch below a release:minor label" "$minor" patch
contains "below" "input below the requirement is explained"
run 0 "input major at 0.x" "$minor" major
contains "version=v1.0.0" "major input leaves 0.x"
contains "bump=major" "major input is major"
run 0 "input minor equal to the requirement" "$minor" minor
contains "version=v0.2.0" "minor input gives v0.2.0"

# release:major counts as minor while 0.x.
git reset -q --hard "$additive"
echo y > y.txt
major_label="$(commit majorlabel)"
pr "$major_label" 13 release:major
run 0 "release:major label at 0.x" "$major_label" auto
contains "version=v0.2.0" "release:major at 0.x gives v0.2.0"

# A breaking API change without labels: raised to minor, and the raise is reported.
git reset -q --hard "$additive"
printf 'one\nTWO\nthree\n' > cores/a/api/a.api
breaking="$(commit breaking)"
pr "$breaking" 14
run 0 "breaking API without labels" "$breaking" auto
contains "version=v0.2.0" "breaking on 0.x gives v0.2.0"
contains "api=breaking" "the API change is reported"
contains "raised=patch->minor" "the raise is reported"
run 1 "input patch below a breaking API change" "$breaking" patch

# A commit without a PR is listed as direct.
git reset -q --hard "$additive"
echo z > z.txt
direct="$(commit direct)"
run 0 "direct commit" "$direct" auto
contains "direct=$direct" "the direct commit is listed"
contains "pr=#11" "the PR before it is still listed"
contains "version=v0.1.2" "direct commit alone is patch"

# Only first-parent commits count: a merged side branch's own commits are not looked up.
git reset -q --hard "$additive"
git checkout -q -b side
echo s > s.txt
side="$(commit side)"
git checkout -q -
g merge -q --no-ff -m merge side
merge="$(git rev-parse HEAD)"
pr "$merge" 15
run 0 "merge commit" "$merge" auto
contains "pr=#15" "the merge's PR is listed"
lacks "$side" "side-branch commit is not walked"
git branch -q -D side

# Several commits of one PR are listed once.
git reset -q --hard "$additive"
echo p > p.txt
first="$(commit first)"
echo q > q.txt
second="$(commit second)"
pr "$first" 16
pr "$second" 16
run 0 "one PR over two commits" "$second" auto
assert_eq 1 "$(printf '%s\n' "$out" | grep -c '^pr=#16')" "the PR is listed once"

# A failing GitHub API call fails the plan rather than counting the commit as direct.
GH_SHIM_FAIL=1 run 1 "gh failure" "$additive" auto
lacks "version=" "no version on gh failure"

# Bad input.
run 2 "invalid bump input" "$additive" huge
run 2 "missing args"
run 2 "bad sha" nonexistent auto

# The latest tag must be an ancestor of the released commit.
git reset -q --hard "$root"
echo o > o.txt
other="$(commit other)"
git reset -q --hard "$additive"
g tag -a v0.1.5 -m v0.1.5 "$other"
run 1 "latest tag not an ancestor" "$additive" auto
contains "not an ancestor" "non-ancestor tag reason"

if [ "$failures" -ne 0 ]; then echo "$failures failure(s)"; exit 1; fi
echo "all passed"
