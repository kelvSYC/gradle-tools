package com.kelvsyc.gradle.ant.shim;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.UnknownElement;

/**
 * Runs a tree of Ant elements in a fresh project, as Ant's own XML parser would build and run it.
 * <p>
 * Request keys: {@code "nodes"} (a list of node maps with keys {@code "name"}, {@code "namespace"},
 * {@code "attributes"}, {@code "text"} and {@code "children"}), {@code "baseDirectory"} (a path string) and
 * {@code "logger"} (a {@code BiConsumer<Integer, String>}). The result has {@code "antVersion"} and, on failure,
 * {@code "failure"} (Ant's message) and {@code "location"}. Never throws.
 */
public final class TreeExecutor implements Function<Map<String, Object>, Map<String, Object>> {
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> apply(Map<String, Object> request) {
        Project project = null;
        Throwable error = null;
        try {
            project = ShimSupport.newProject(request);
            Target target = new Target();
            target.setName("");
            target.setProject(project);
            for (Map<String, Object> node : (List<Map<String, Object>>) request.get("nodes")) {
                UnknownElement element = toElement(node, project, target);
                element.maybeConfigure();
                element.perform();
            }
            return ShimSupport.success();
        } catch (BuildException e) {
            error = e;
            return ShimSupport.failure(e);
        } catch (RuntimeException e) {
            error = e;
            return ShimSupport.failure(e);
        } finally {
            if (project != null) {
                project.fireBuildFinished(error);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static UnknownElement toElement(Map<String, Object> node, Project project, Target target) {
        String name = (String) node.get("name");
        String namespace = (String) node.get("namespace");
        String uri = namespace == null ? "" : namespace;
        UnknownElement element = new UnknownElement(name);
        element.setNamespace(uri);
        element.setQName(name);
        element.setTaskType(ProjectHelper.genComponentName(uri, name));
        element.setTaskName(name);
        element.setProject(project);
        element.setOwningTarget(target);
        RuntimeConfigurable wrapper = new RuntimeConfigurable(element, name);
        Map<String, String> attributes = (Map<String, String>) node.get("attributes");
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            wrapper.setAttribute(attribute.getKey(), attribute.getValue());
        }
        Object text = node.get("text");
        if (text != null) {
            wrapper.addText(text.toString());
        }
        for (Map<String, Object> child : (List<Map<String, Object>>) node.get("children")) {
            UnknownElement childElement = toElement(child, project, target);
            element.addChild(childElement);
            wrapper.addChild(childElement.getRuntimeConfigurableWrapper());
        }
        return element;
    }
}
