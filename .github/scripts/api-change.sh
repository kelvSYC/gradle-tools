#!/usr/bin/env bash
# Usage: api-change.sh <base-ref> <head-ref>
# Prints none|additive|breaking from the diff of cores/*/api/*.api.
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo "usage: $0 <base-ref> <head-ref>" >&2
  exit 2
fi

result=none
while IFS= read -r line; do
  case "$line" in
    '---'*|'+++'*) ;;
    -*) result=breaking; break ;;
    +*) result=additive ;;
    *) ;;
  esac
done < <(git diff --unified=0 "$1" "$2" -- ':(glob)cores/*/api/*.api')

echo "$result"
