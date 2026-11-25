package com.kelvsyc.gradle.ant.shim;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

/**
 * Runs one target of a build, without its dependencies, in a fresh project.
 * <p>
 * Request keys: {@code "files"} and {@code "seeds"} (as for {@link BuildReader}), {@code "exports"} (a list of
 * property maps, applied in order after the parse, as an upstream target's properties would be; the first definition
 * of a name wins, so the build's top-level definitions win too), {@code "target"} (the target's name) and
 * {@code "logger"}. Only the target's own tasks run, subject to its own {@code if} and {@code unless}, with the base
 * directory and {@code ant.file} of the top-level file that registered it. The result has {@code "antVersion"} and
 * {@code "properties"}: every property the run defined (the names in the project's property table after the run that
 * were not in it before), in name order.
 * Failures are reported as by {@link TreeExecutor}. Never throws.
 */
public final class TargetRunner implements Function<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> apply(Map<String, Object> request) {
        return ShimSupport.runInProject(request, project -> run(request, project));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> run(Map<String, Object> request, Project project) {
        ShimSupport.applySeeds(project, (Map<String, String>) request.get("seeds"));
        ShimSupport.ParsedBuild parsed = ShimSupport.parseBuildFiles(project, request);
        String name = (String) request.get("target");
        Target target = ShimSupport.namedTargets(project).get(name);
        if (target == null) {
            throw new BuildException("Target \"" + name + "\" does not exist in the project");
        }
        ShimSupport.applyExports(project, (List<Map<String, String>>) request.get("exports"));
        parsed.enterOwner(project, name);
        Set<String> before = new HashSet<>(project.getProperties().keySet());
        target.performTasks();
        Map<String, String> defined = new TreeMap<>();
        for (Map.Entry<String, Object> property : project.getProperties().entrySet()) {
            if (!before.contains(property.getKey()) && property.getValue() != null) {
                defined.put(property.getKey(), property.getValue().toString());
            }
        }
        Map<String, Object> result = ShimSupport.success();
        result.put("properties", defined);
        return result;
    }
}
