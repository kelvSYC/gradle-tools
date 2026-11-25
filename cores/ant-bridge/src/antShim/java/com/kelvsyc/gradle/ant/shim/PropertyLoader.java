package com.kelvsyc.gradle.ant.shim;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.types.Path;

/**
 * Loads properties as Ant's {@code property} task does, one task per source in order, in a fresh project.
 * <p>
 * Request keys: {@code "seeds"} (a map of properties set before any source), {@code "sources"} (a list of source
 * maps: {@code "kind"} is {@code file}, {@code resource}, {@code url} or {@code environment}, with {@code "file"},
 * {@code "name"} and {@code "classpath"} (a list of paths), {@code "url"} and {@code "prefix"}, and
 * {@code "prefixValues"}), {@code "baseDirectory"} and {@code "logger"}. The result has {@code "properties"}: the
 * seeds and every key a source defined, first occurrence first, with Ant's effective value. Failures are reported as
 * by {@link TreeExecutor}. Never throws.
 */
public final class PropertyLoader implements Function<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> apply(Map<String, Object> request) {
        return ShimSupport.runInProject(request, project -> load(request, project));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> load(Map<String, Object> request, Project project) {
        Set<String> keys = new LinkedHashSet<>();
        PropertyHelper helper = PropertyHelper.getPropertyHelper(project);
        for (Map.Entry<String, String> seed : ((Map<String, String>) request.get("seeds")).entrySet()) {
            keys.add(seed.getKey());
            helper.setNewProperty(seed.getKey(), seed.getValue());
        }
        for (Map<String, Object> source : (List<Map<String, Object>>) request.get("sources")) {
            RecordingProperty task = configure(source, project, keys);
            if (task != null) {
                task.execute();
            }
        }
        Map<String, String> properties = new LinkedHashMap<>();
        for (String key : keys) {
            String value = project.getProperty(key);
            if (value != null) {
                properties.put(key, value);
            }
        }
        Map<String, Object> result = ShimSupport.success();
        result.put("properties", properties);
        return result;
    }

    /**
     * Returns the task loading {@code source}, or {@code null} for a file that does not exist, which is skipped.
     */
    @SuppressWarnings("unchecked")
    private static RecordingProperty configure(Map<String, Object> source, Project project, Set<String> keys) {
        RecordingProperty task = new RecordingProperty(keys);
        task.setProject(project);
        task.setTaskName("property");
        String kind = (String) source.get("kind");
        switch (kind) {
            case "file":
                File file = new File((String) source.get("file"));
                if (!file.exists()) {
                    project.log("Property file " + file.getAbsolutePath() + " not found; skipped", Project.MSG_INFO);
                    return null;
                }
                task.setFile(file);
                break;
            case "resource":
                task.setResource((String) source.get("name"));
                Path classpath = new Path(project);
                for (String entry : (List<String>) source.get("classpath")) {
                    classpath.setLocation(new File(entry));
                }
                task.setClasspath(classpath);
                break;
            case "url":
                task.setUrl(toUrl((String) source.get("url")));
                break;
            case "environment":
                task.setEnvironment((String) source.get("prefix"));
                return task;
            default:
                throw new BuildException("Unknown property source kind: " + kind);
        }
        Object prefix = source.get("prefix");
        if (prefix != null) {
            task.setPrefix(prefix.toString());
        }
        task.setPrefixValues(Boolean.TRUE.equals(source.get("prefixValues")));
        return task;
    }

    private static URL toUrl(String url) {
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException e) {
            throw new BuildException("Malformed property URL " + url, e);
        }
    }
}
