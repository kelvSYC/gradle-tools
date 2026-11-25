package com.kelvsyc.gradle.ant.shim;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.ExtensionPoint;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.LoadProperties;
import org.apache.tools.ant.taskdefs.Property;
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
     * build file each.
     * <p>
     * Before each file, the project's base directory is set to the file's own {@code <project basedir>} (relative to
     * the file's directory), else to {@code "baseDirectory"}, else to the file's directory, and {@code ant.file} to
     * the file. A target name that one top-level file registers (itself or through its imports) and an earlier one
     * already registered fails, naming both top-level files: Ant would otherwise silently keep the earlier target. A
     * file imported by several top-level files is parsed once per importer; its targets are not duplicates, and keep
     * the first importer.
     * Within one top-level file, Ant's own rules apply (the importing file's target wins over an imported one).
     * <p>
     * The existing files that the parse's {@code property file=} and {@code loadproperties srcFile=} tasks read are
     * reported as {@link ParsedBuild#readFiles}. Other inputs of the parse (environment, URLs, resources, commands)
     * are not tracked.
     */
    @SuppressWarnings("unchecked")
    static ParsedBuild parseBuildFiles(Project project, Map<String, Object> request) {
        ParsedBuild parsed = new ParsedBuild();
        Set<File> loaded = new LinkedHashSet<>();
        ReadFilesListener readFiles = new ReadFilesListener();
        project.addBuildListener(readFiles);
        try {
            parseAll(project, request, parsed, loaded);
        } finally {
            project.removeBuildListener(readFiles);
        }
        parsed.loadedFiles.addAll(loaded);
        parsed.readFiles.addAll(readFiles.files);
        return parsed;
    }

    @SuppressWarnings("unchecked")
    private static void parseAll(Project project, Map<String, Object> request, ParsedBuild parsed, Set<File> loaded) {
        for (Map<String, Object> entry : (List<Map<String, Object>>) request.get("files")) {
            File file = new File((String) entry.get("file")).getAbsoluteFile();
            TopLevelScan scan = TopLevelScan.of(file);
            for (String name : scan.targets) {
                checkNotRegistered(parsed, name, file);
            }
            // A fresh parsing context and no implicit target, so that this file is parsed as a top-level file, not as
            // part of the previous one.
            project.getReferences().remove(PARSING_CONTEXT);
            project.getTargets().remove("");
            File baseDirectory = baseDirectory(file, scan.baseDirectory, (String) entry.get("baseDirectory"));
            parsed.baseDirectories.put(file, baseDirectory);
            enterTopLevelFile(project, file, baseDirectory);
            ProjectHelper helper = ProjectHelperRepository.getInstance()
                .getProjectHelperForBuildFile(new FileResource(file));
            project.addReference(MagicNames.REFID_PROJECT_HELPER, helper);
            parseHidingEarlierTargets(project, helper, file, parsed);
            for (Object source : helper.getImportStack()) {
                File loadedFile = toFile(source);
                if (loadedFile != null) {
                    loaded.add(loadedFile);
                }
            }
        }
    }

    /**
     * Sets the base directory and {@code ant.file} of {@code project} to those of the top-level file {@code file}, as
     * while it is parsed, or while one of its targets runs.
     */
    static void enterTopLevelFile(Project project, File file, File baseDirectory) {
        project.setBaseDir(baseDirectory);
        project.setUserProperty(MagicNames.ANT_FILE, file.getPath());
    }

    /**
     * Parses {@code file} with the earlier top-level files' targets (but not their extension points, which the file
     * may extend) hidden, so that Ant registers every target the file defines or imports instead of silently ignoring
     * the names already taken; such a name then fails. Restores the hidden targets afterwards.
     */
    private static void parseHidingEarlierTargets(Project project, ProjectHelper helper, File file,
                                                  ParsedBuild parsed) {
        Map<String, Target> hidden = new HashMap<>();
        for (Map.Entry<String, Target> target : project.getTargets().entrySet()) {
            if (!(target.getValue() instanceof ExtensionPoint)) {
                hidden.put(target.getKey(), target.getValue());
            }
        }
        Set<String> before = new HashSet<>(project.getTargets().keySet());
        project.getTargets().keySet().removeAll(hidden.keySet());
        try {
            helper.parse(project, file);
            for (Map.Entry<String, Target> target : project.getTargets().entrySet()) {
                Target earlier = hidden.get(target.getKey());
                if (earlier != null && !sameDefinition(earlier, target.getValue())) {
                    checkNotRegistered(parsed, target.getKey(), file);
                }
            }
        } finally {
            project.getTargets().putAll(hidden);
        }
        for (String name : project.getTargets().keySet()) {
            if (!name.isEmpty() && !before.contains(name)) {
                parsed.owners.put(name, file);
            }
        }
    }

    /**
     * Whether two targets come from the same place: a file imported by two top-level files defines its targets twice.
     */
    private static boolean sameDefinition(Target first, Target second) {
        Location one = first.getLocation();
        Location two = second.getLocation();
        return one != null && two != null && one.getFileName() != null
            && one.getFileName().equals(two.getFileName()) && one.getLineNumber() == two.getLineNumber()
            && one.getColumnNumber() == two.getColumnNumber();
    }

    private static void checkNotRegistered(ParsedBuild parsed, String name, File file) {
        File earlier = parsed.owners.get(name);
        if (earlier != null) {
            throw new BuildException("Target '" + name + "' is defined in both " + earlier + " and " + file);
        }
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
     * The outcome of {@link #parseBuildFiles}.
     */
    static final class ParsedBuild {
        /** The top-level files and every file they imported or included, in load order. */
        final List<File> loadedFiles = new ArrayList<>();
        /** The existing files that {@code property file=} and {@code loadproperties} read during the parse. */
        final List<File> readFiles = new ArrayList<>();
        /** The top-level file that registered each target name. */
        final Map<String, File> owners = new HashMap<>();
        /** The base directory of each top-level file. */
        final Map<File, File> baseDirectories = new HashMap<>();

        /**
         * Sets the base directory and {@code ant.file} of {@code project} to those of the top-level file that
         * registered {@code target}, as Ant runs a target with its top-level file's. Does nothing for an unknown name.
         */
        void enterOwner(Project project, String target) {
            File owner = owners.get(target);
            if (owner != null) {
                enterTopLevelFile(project, owner, baseDirectories.get(owner));
            }
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
            } else if (depth == 1 && (uri.isEmpty() || ProjectHelper.ANT_CORE_URI.equals(uri))
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
     * Records the existing files that finished {@code property} ({@code file=}) and {@code loadproperties}
     * ({@code srcFile=}) tasks read. Ant releases a task's configured instance once it ran, so the file is resolved
     * from the element's attribute, as Ant resolved it: properties expanded, relative to the base directory.
     */
    private static final class ReadFilesListener implements BuildListener {
        private final Set<File> files = new LinkedHashSet<>();

        @Override
        public void taskFinished(BuildEvent event) {
            Task task = event.getTask();
            if (!(task instanceof UnknownElement)) {
                return;
            }
            UnknownElement element = (UnknownElement) task;
            Project project = element.getProject();
            String component = ProjectHelper.genComponentName(element.getNamespace(), element.getTag());
            AntTypeDefinition definition = ComponentHelper.getComponentHelper(project).getDefinition(component);
            Class<?> type = definition == null ? null : definition.getExposedClass(project);
            String attribute = null;
            if (type != null && Property.class.isAssignableFrom(type)) {
                attribute = "file";
            } else if (type != null && LoadProperties.class.isAssignableFrom(type)) {
                attribute = "srcFile";
            }
            Object value = attribute == null ? null : attributeValue(element, attribute);
            if (value != null) {
                File file = project.resolveFile(project.replaceProperties(value.toString()));
                if (file.isFile()) {
                    files.add(file.getAbsoluteFile());
                }
            }
        }

        private static Object attributeValue(UnknownElement element, String name) {
            for (Map.Entry<String, Object> attribute : element.getWrapper().getAttributeMap().entrySet()) {
                if (attribute.getKey().equalsIgnoreCase(name)) {
                    return attribute.getValue();
                }
            }
            return null;
        }

        @Override
        public void messageLogged(BuildEvent event) {
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
