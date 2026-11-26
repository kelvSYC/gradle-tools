package com.kelvsyc.gradle.ant.shim;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;

/**
 * Parses build files with Ant's own project helper, in a fresh project, and reports what the parse defined, without
 * running any target.
 * <p>
 * Request keys: {@code "files"} (a list of maps with {@code "file"}, an absolute path, and optionally
 * {@code "baseDirectory"}, an absolute path; see {@link ShimSupport#parseBuildFiles}), {@code "seeds"} (a map of
 * properties set as user properties before the parse) and {@code "logger"}. The result has {@code "antVersion"},
 * {@code "targets"} (a list of target maps, one per name Ant registered, with {@code "name"}, {@code "file"} (the
 * absolute path of the file defining it, or {@code null}), {@code "description"} (or {@code null}),
 * {@code "depends"} (the direct dependencies as Ant resolved them, extension-point wiring included) and
 * {@code "upstream"} (every transitive dependency in Ant's execution order, without the target itself; names that are
 * not targets are left out)), {@code "properties"} (the seeds and every property the parse set or tried to set, with
 * Ant's effective value; {@code basedir}, other Ant built-ins and system properties the parse did not name are left
 * out), {@code "loadedFiles"} (the absolute paths of the top-level files and every file they imported or included)
 * and {@code "readFiles"} (the absolute paths of the files the parse's {@code property file=},
 * {@code loadproperties} and {@code import} tasks read or looked for, missing ones included; other inputs of the
 * parse are not tracked).
 * Failures are reported as by {@link TreeExecutor}. Never throws.
 */
public final class BuildReader implements Function<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> apply(Map<String, Object> request) {
        return ShimSupport.runInProject(request, project -> read(request, project));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> read(Map<String, Object> request, Project project) {
        Map<String, String> seeds = (Map<String, String>) request.get("seeds");
        ShimSupport.applySeeds(project, seeds);
        Set<String> keys = new LinkedHashSet<>(seeds.keySet());
        PropertyHelper.getPropertyHelper(project).add(new RecordingSetter(keys));
        ShimSupport.ParsedBuild parsed = ShimSupport.parseBuildFiles(project, request);
        List<String> loadedFiles = new ArrayList<>();
        for (File file : parsed.loadedFiles) {
            loadedFiles.add(file.getPath());
        }
        List<String> readFiles = new ArrayList<>();
        for (File file : parsed.readFiles) {
            readFiles.add(file.getPath());
        }
        keys.remove(MagicNames.PROJECT_BASEDIR);
        Map<String, String> properties = new LinkedHashMap<>();
        for (String key : keys) {
            String value = project.getProperty(key);
            if (value != null) {
                properties.put(key, value);
            }
        }
        Map<String, Object> result = ShimSupport.success();
        result.put("targets", targets(project));
        result.put("properties", properties);
        result.put("loadedFiles", loadedFiles);
        result.put("readFiles", readFiles);
        return result;
    }

    private static List<Map<String, Object>> targets(Project project) {
        Map<String, Target> named = ShimSupport.namedTargets(project);
        List<String> names = new ArrayList<>(named.keySet());
        Collections.sort(names);
        List<Map<String, Object>> targets = new ArrayList<>();
        for (String name : names) {
            Target target = named.get(name);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("file", target.getLocation() == null ? null : target.getLocation().getFileName());
            map.put("description", target.getDescription());
            map.put("depends", Collections.list(target.getDependencies()));
            map.put("upstream", upstream(project, name, named));
            targets.add(map);
        }
        return targets;
    }

    /**
     * Returns the targets Ant runs before {@code name}, in Ant's order ({@link Project#topoSort}). A dependency that
     * names no target (a Gradle task, for instance) stands in as a target without dependencies and is left out.
     */
    private static List<String> upstream(Project project, String name, Map<String, Target> named) {
        Hashtable<String, Target> table = new Hashtable<>(named);
        for (Target target : named.values()) {
            for (Enumeration<String> dependencies = target.getDependencies(); dependencies.hasMoreElements();) {
                String dependency = dependencies.nextElement();
                if (!table.containsKey(dependency)) {
                    Target placeholder = new Target();
                    placeholder.setName(dependency);
                    table.put(dependency, placeholder);
                }
            }
        }
        List<String> upstream = new ArrayList<>();
        for (Target target : project.topoSort(name, table, false)) {
            String targetName = target.getName();
            if (!targetName.equals(name) && named.containsKey(targetName)) {
                upstream.add(targetName);
            }
        }
        return upstream;
    }

    /**
     * Records the name of every property set or newly set through the property helper, whether or not Ant keeps
     * the value, and leaves the setting to Ant.
     */
    private static final class RecordingSetter implements PropertyHelper.PropertySetter {
        private final Set<String> keys;

        RecordingSetter(Set<String> keys) {
            this.keys = keys;
        }

        @Override
        public boolean setNew(String property, Object value, PropertyHelper propertyHelper) {
            keys.add(property);
            return false;
        }

        @Override
        public boolean set(String property, Object value, PropertyHelper propertyHelper) {
            keys.add(property);
            return false;
        }
    }
}
