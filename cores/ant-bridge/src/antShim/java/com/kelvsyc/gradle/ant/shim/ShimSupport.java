package com.kelvsyc.gradle.ant.shim;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;

/**
 * Support code of the Ant shim, loaded in an isolated class loader next to Ant. Boundary types are JDK types only:
 * requests and results are maps, and log messages go to a {@code BiConsumer<Integer, String>} of Ant priority and
 * message.
 */
final class ShimSupport {
    private ShimSupport() {
    }

    /**
     * Creates and initializes a fresh project whose messages go to the request's {@code "logger"}.
     */
    @SuppressWarnings("unchecked")
    static Project newProject(Map<String, Object> request) {
        Project project = new Project();
        Object baseDirectory = request.get("baseDirectory");
        if (baseDirectory != null) {
            project.setBaseDir(new File(baseDirectory.toString()));
        }
        project.init();
        project.addReference(MagicNames.REFID_PROJECT_HELPER, ProjectHelper.getProjectHelper());
        project.addBuildListener(new LoggerListener((BiConsumer<Integer, String>) request.get("logger")));
        return project;
    }

    /**
     * Returns the result map of a failure reported by Ant: its message, its location (empty if unknown) and the stack
     * trace. Never throws.
     */
    static Map<String, Object> failure(BuildException e) {
        Location location = e.getLocation();
        return failure(e.getMessage(), location == null ? "" : location.toString(), e);
    }

    /**
     * Returns the result map of a failure that Ant did not report as a {@link BuildException}. Never throws.
     */
    static Map<String, Object> failure(Throwable e) {
        return failure(e.toString(), "", e);
    }

    private static Map<String, Object> failure(String message, String location, Throwable e) {
        Map<String, Object> result = success();
        result.put("failure", message);
        result.put("location", location);
        StringWriter trace = new StringWriter();
        e.printStackTrace(new PrintWriter(trace));
        result.put("trace", trace.toString());
        return result;
    }

    /**
     * Returns the result map of a success. Never throws: the Ant version is {@code null} if Ant cannot report it.
     */
    static Map<String, Object> success() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("antVersion", antVersion());
        return result;
    }

    private static String antVersion() {
        try {
            return Main.getAntVersion();
        } catch (BuildException e) {
            return null;
        }
    }

    /**
     * Forwards logged messages, prefixed with {@code [ant:<task>] } when a task is known.
     */
    private static final class LoggerListener implements BuildListener {
        private final BiConsumer<Integer, String> logger;

        LoggerListener(BiConsumer<Integer, String> logger) {
            this.logger = logger;
        }

        @Override
        public void messageLogged(BuildEvent event) {
            Task task = event.getTask();
            String message = event.getMessage();
            if (task != null && task.getTaskName() != null) {
                message = "[ant:" + task.getTaskName() + "] " + message;
            }
            logger.accept(event.getPriority(), message);
        }

        @Override
        public void buildStarted(BuildEvent event) {
        }

        @Override
        public void buildFinished(BuildEvent event) {
        }

        @Override
        public void targetStarted(BuildEvent event) {
        }

        @Override
        public void targetFinished(BuildEvent event) {
        }

        @Override
        public void taskStarted(BuildEvent event) {
        }

        @Override
        public void taskFinished(BuildEvent event) {
        }
    }
}
