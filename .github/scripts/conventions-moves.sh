#!/usr/bin/env bash
# Finds the libraries a gradle-build-conventions release moves across a breaking version.
#
#   conventions-moves.sh versions <platform.pom> <catalog.toml>
#       Prints `group:artifact version`, sorted: the POM's dependencyManagement entries and the catalog's [libraries]
#       with a literal version (`version.ref` is resolved from [versions]; an empty version is managed by the platform).
#   conventions-moves.sh breaking <old-versions> <new-versions>
#       Prints `group:artifact old -> new` for each breaking move, always exiting 0. A move is breaking when the major
#       changes, or the major is 0 and the minor changes, or the old version is a pre-release (a `-` suffix) and the
#       major or minor changes. Added or removed modules are not moves.
#
# Portable (awk/sed/sort only): runs on ubuntu-latest and macOS bash 3.2.
set -euo pipefail

versions() {
    local pom=$1 catalog=$2
    {
        awk '
            /<dependency>/ { inDep = 1; g = ""; a = ""; v = ""; next }
            /<\/dependency>/ { if (inDep && g != "" && a != "" && v != "") print g ":" a " " v; inDep = 0; next }
            inDep && /<groupId>/ { g = $0; sub(/.*<groupId>/, "", g); sub(/<\/groupId>.*/, "", g) }
            inDep && /<artifactId>/ { a = $0; sub(/.*<artifactId>/, "", a); sub(/<\/artifactId>.*/, "", a) }
            inDep && /<version>/ { v = $0; sub(/.*<version>/, "", v); sub(/<\/version>.*/, "", v) }
        ' "$pom"
        awk '
            /^\[/ { table = $0; next }
            table == "[versions]" && /^[A-Za-z0-9_.-]+ *= *"/ {
                key = $1; split($0, q, "\""); ref[key] = q[2]
            }
            table == "[libraries]" && /group *= *"/ {
                line = $0
                g = line; sub(/.*group *= *"/, "", g); sub(/".*/, "", g)
                n = line; sub(/.*name *= *"/, "", n); sub(/".*/, "", n)
                v = ""
                if (line ~ /version\.ref *= *"/) {
                    r = line; sub(/.*version\.ref *= *"/, "", r); sub(/".*/, "", r); v = ref[r]
                } else if (line ~ /version *= *"/) {
                    v = line; sub(/.*version *= *"/, "", v); sub(/".*/, "", v)
                }
                if (v != "") print g ":" n " " v
            }
        ' "$catalog"
    } | LC_ALL=C sort -u
}

breaking() {
    awk '
        function parts(ver, out,    core, n, arr) {
            core = ver; sub(/-.*/, "", core)
            n = split(core, arr, ".")
            out[1] = arr[1] + 0; out[2] = (n > 1 ? arr[2] + 0 : 0)
        }
        NR == FNR { old[$1] = $2; next }
        ($1 in old) && old[$1] != $2 {
            parts(old[$1], o); parts($2, w)
            majorMoved = o[1] != w[1]; minorMoved = o[2] != w[2]
            pre = index(old[$1], "-") > 0
            if (majorMoved || (o[1] == 0 && minorMoved) || (pre && minorMoved))
                print $1 " " old[$1] " -> " $2
        }
    ' "$1" "$2"
}

case "${1:-}" in
    versions) [ $# -eq 3 ] || { echo "usage: $0 versions <platform.pom> <catalog.toml>" >&2; exit 2; }; versions "$2" "$3" ;;
    breaking) [ $# -eq 3 ] || { echo "usage: $0 breaking <old-versions> <new-versions>" >&2; exit 2; }; breaking "$2" "$3" ;;
    *) echo "usage: $0 versions|breaking ..." >&2; exit 2 ;;
esac
