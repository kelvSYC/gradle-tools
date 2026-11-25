package com.kelvsyc.gradle.ant.shim;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Support code of the Ant shim, loaded in an isolated class loader next to Ant. Boundary types are JDK types only:
 * requests and results are maps, and log messages go to a {@code BiConsumer<Integer, String>} of Ant priority and
 * message.
 */
final class ShimSupport {
    /** The reference under which Ant's {@code ProjectHelper2} keeps its parsing context. */
    private static final String PARSING_CONTEXT = "ant.parsing.context";

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
     * Sets {@code seeds} as user properties of {@code project}, as {@code -D} on Ant's command line would.
     */
    static void applySeeds(Project project, Map<String, String> seeds) {
        for (Map.Entry<String, String> seed : seeds.entrySet()) {
            project.setUserProperty(seed.getKey(), seed.getValue());
        }
    }

    /**
     * Sets each map of {@code exports} in order as ordinary properties: the first definition of a name wins, and user
     * properties win over all of them.
     */
    static void applyExports(Project project, List<Map<String, String>> exports) {
        PropertyHelper helper = PropertyHelper.getPropertyHelper(project);
        for (Map<String, String> export : exports) {
            for (Map.Entry<String, String> property : export.entrySet()) {
                helper.setNewProperty(property.getKey(), property.getValue());
            }
        }
    }

    /**
     * Parses the build files of a request's {@code "files"} (a list of maps with {@code "file"}, an absolute path, and
     * optionally {@code "baseDirectory"}) in order into {@code project}, as Ant's own parser does for one top-level
     * build file each, and returns every file the parse loaded: the top-level files and every file they
     * {@code import} or {@code include}, in load order.
     * <p>
     * Before each file, the project's base directory is set to the file's own {@code <project basedir>} (relative to
     * the file's directory), else to {@code "baseDirectory"}, else to the file's directory, and {@code ant.file} to
     * the file. A target name that one top-level file defines and an earlier one already registered (itself or
     * through its imports) fails, naming both top-level files: Ant would otherwise silently ignore the later
     * definition.
     */
    @SuppressWarnings("unchecked")
    static List<File> parseBuildFiles(Project project, Map<String, Object> request) {
        Set<File> loaded = new LinkedHashSet<>();
        Map<String, File> definedBy = new HashMap<>();
        for (Map<String, Object> entry : (List<Map<String, Object>>) request.get("files")) {
            File file = new File((String) entry.get("file")).getAbsoluteFile();
            TopLevelScan scan = TopLevelScan.of(file);
            for (String name : scan.targets) {
                File earlier = definedBy.get(name);
                if (earlier != null) {
                    throw new BuildException("Target '" + name + "' is defined in both " + earlier + " and " + file);
                }
            }
            // A fresh parsing context and no implicit target, so that this file is parsed as a top-level file, not as
            // part of the previous one.
            project.getReferences().remove(PARSING_CONTEXT);
            project.getTargets().remove("");
            project.setBaseDir(baseDirectory(file, scan.baseDirectory, (String) entry.get("baseDirectory")));
            project.setUserProperty(MagicNames.ANT_FILE, file.getPath());
            Set<String> before = new LinkedHashSet<>(project.getTargets().keySet());
            ProjectHelper helper = ProjectHelperRepository.getInstance().getProjectHelperForBuildFile(
                new FileResource(file));
            project.addReference(MagicNames.REFID_PROJECT_HELPER, helper);
            helper.parse(project, file);
            for (String name : project.getTargets().keySet()) {
                if (!name.isEmpty() && !before.contains(name)) {
                    definedBy.put(name, file);
                }
            }
            for (Object source : helper.getImportStack()) {
                File loadedFile = toFile(source);
                if (loadedFile != null) {
                    loaded.add(loadedFile);
                }
            }
        }
        return new ArrayList<>(loaded);
    }

    /**
     * Returns the targets of {@code project} by name, without Ant's unnamed implicit target.
     */
    static Map<String, Target> namedTargets(Project project) {
        Map<String, Target> targets = new LinkedHashMap<>(project.getTargets());
        targets.remove("");
        return targets;
    }

    private static File baseDirectory(File file, String ownBaseDirectory, String requested) {
        File parent = file.getParentFile();
        if (ownBaseDirectory != null) {
            return FileUtils.getFileUtils().resolveFile(parent, ownBaseDirectory);
        }
        return requested != null ? new File(requested) : parent;
    }

    private static File toFile(Object source) {
        if (source instanceof File) {
            return ((File) source).getAbsoluteFile();
        }
        if (source instanceof Resource) {
            FileProvider provider = ((Resource) source).as(FileProvider.class);
            if (provider != null) {
                return provider.getFile().getAbsoluteFile();
            }
        }
        return null;
    }

    /**
     * Runs {@code body} in a fresh project (see {@link #newProject}) and returns its result, or the failure map of
     * whatever it throws. Fires the project's build-finished event afterwards, which releases Ant's resources. Never
     * throws.
     */
    static Map<String, Object> runInProject(Map<String, Object> request, Function<Project, Map<String, Object>> body) {
        Map<String, Object> result;
        Project project = null;
        Throwable error = null;
        try {
            project = newProject(request);
            result = body.apply(project);
        } catch (BuildException e) {
            error = e;
            result = failure(e);
        } catch (RuntimeException | LinkageError e) {
            error = e;
            result = failure(e);
        }
        if (project != null) {
            try {
                project.fireBuildFinished(error);
            } catch (RuntimeException | LinkageError e) {
                if (error == null) {
                    result = failure(e);
                }
            }
        }
        return result;
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
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * The {@code basedir} attribute and the directly defined target names of a top-level build file, read without
     * resolving any entity or DTD. A file that cannot be read this way yields nothing: Ant's own parse reports it.
     */
    private static final class TopLevelScan extends DefaultHandler {
        private final List<String> targets = new ArrayList<>();
        private String baseDirectory;
        private int depth;

        static TopLevelScan of(File file) {
            TopLevelScan scan = new TopLevelScan();
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.newSAXParser().parse(file, scan);
            } catch (SAXException | IOException | ParserConfigurationException e) {
                return new TopLevelScan();
            }
            return scan;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (depth == 0) {
                baseDirectory = attributes.getValue("", "basedir");
            } else if (depth == 1 && uri.isEmpty()
                && ("target".equals(localName) || "extension-point".equals(localName))) {
                String name = attributes.getValue("", "name");
                if (name != null) {
                    targets.add(name);
                }
            }
            depth++;
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            depth--;
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
