#!/usr/bin/env bash
# Usage: api-change.sh <base-ref> <head-ref>
# Prints none|additive|breaking from the diff of cores/*/api/*.api.
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo "usage: $0 <base-ref> <head-ref>" >&2
  exit 2
fi

# Captured first so that a failing git diff (bad or unfetched ref) aborts under set -e.
# --no-renames makes a renamed dump a delete plus an add, hence breaking.
diff_output="$(git diff --unified=0 --no-renames "$1" "$2" -- ':(glob)cores/*/api/*.api')"

result=none
in_header=0
while IFS= read -r line; do
  case "$line" in
    'diff --git '*) in_header=1 ;;
    '@@'*) in_header=0 ;;
    *)
      if [ "$in_header" = 0 ]; then
        case "$line" in
          -*) result=breaking; break ;;
          +*) result=additive ;;
          *) ;;
        esac
      fi
      ;;
  esac
done <<EOT
$diff_output
EOT

echo "$result"
