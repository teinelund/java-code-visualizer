package org.teinelund.javacodevisualizer.dom;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class JavaProjectObjectModelFactoryImpl implements JavaProjectObjectModelFactory {


    @Override
    public JavaProjectObjectModel createrAndStore(List<Path> javaProjectPaths, Path storagePath) {
        return null;
    }

    @Override
    public JavaProjectObjectModel create(Path storagePath) {
        return null;
    }

    /**
     * This method returns a list of Path objects, which are maven projects (with java source code),
     * given a list of Path objects.
     *
     * @param javaProjectPaths is a list of Path objects.
     * @param excludePaths is a list of Path objects containing Path to exclude.
     * @return a list of Path objects.
     * @throws IOException
     */
    List<Path> getMavenProjects(List<Path> javaProjectPaths, List<Path> excludePaths) throws IOException {
        List<Path> paths = new LinkedList<>();
        for (Path javaProjectPath : javaProjectPaths) {
            paths.addAll(getMavenProject(javaProjectPath, excludePaths));
        }
        return paths;
    }

    /**
     * This method returns a list of Path objects, which all are maven projects (with java source code),
     * given a Path object.
     *
     * @param javaProjectPath is a Path object.
     * @param excludePaths is a list of Path objects containing Path to exclude.
     * @return a list of Path objects.
     * @throws IOException
     */
    List<Path> getMavenProject(Path javaProjectPath, List<Path> excludePaths) throws IOException {

        if (!Files.exists(javaProjectPath)) {
            return new LinkedList<>();
        }
        if (excludePaths.contains(javaProjectPath)) {
            return new LinkedList<>();
        }

        List<Path> paths = new LinkedList<>();
        if (isMavenProject(javaProjectPath)) {
            paths.add(javaProjectPath);
        }
        else {
            DirectoryStream<Path> stream = Files.newDirectoryStream(javaProjectPath);
            for (Path fileOrDirectoryPath : stream) {
                if (Files.isDirectory(fileOrDirectoryPath) && "target".equals(fileOrDirectoryPath.getFileName().toString())) {
                    continue;
                }
                if (Files.isDirectory(fileOrDirectoryPath)) {
                    paths.addAll(getMavenProject(fileOrDirectoryPath, excludePaths));
                }
            }
        }
        return paths;
    }

    /**
     * This method validated that a Path object contains a valid Maven project. To be a valid Maven
     * project a Path must:
     * # be a directory
     * # that directory must contains a pom.xml file
     * # that directory must contain a src directory
     * # that src directory or its sub directories contain one or more java source files.
     *
     * @param path is a Path object
     * @return true if the Path is a Maven project, otherwise false.
     * @throws IOException
     */
    boolean isMavenProject(Path path) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        Path srcDirectory = null;
        boolean pomXmlFileFound = false;
        for (Path fileOrDirectoryPath : stream) {
            if (Files.isDirectory(fileOrDirectoryPath) && "target".equals(fileOrDirectoryPath.getFileName().toString())) {
                continue;
            }
            if (Files.isDirectory(fileOrDirectoryPath) && "src".equals(fileOrDirectoryPath.getFileName().toString())) {
                srcDirectory = fileOrDirectoryPath;
                continue;
            }
            if (Files.isRegularFile(fileOrDirectoryPath) && "pom.xml".equals(fileOrDirectoryPath.getFileName().toString())) {
                pomXmlFileFound = true;
                continue;
            }
        }
        stream.close();
        if (! (srcDirectory != null && pomXmlFileFound)) {
            return false;
        }

        return containsJavaSourceCode(srcDirectory);
    }

    /**
     * This method searches a directory and all sub directories for a java source code file. If found, returns true
     * otherwise returns false.
     *
     * @param path is a Path object.
     * @return true if a java source code was found, otherwise false.
     */
    boolean containsJavaSourceCode(Path path) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path fileOrDirectoryPath : stream) {
            String filename = fileOrDirectoryPath.getFileName().toString();
            if (Files.isRegularFile(fileOrDirectoryPath) && filename.endsWith(".java")) {
                return true;
            }
            if (Files.isDirectory(fileOrDirectoryPath)) {
                if (containsJavaSourceCode(fileOrDirectoryPath)) {
                    return true;
                }
            }
        }
        return false;
    }

/*

    void validateJavaProjectsPaths(List<Path> javaProjectPaths) throws IOException {
        for (Path javaProject : javaProjectPaths) {
            if (!Files.exists(javaProject)) {
                throw new JavaProjectDoesNotExistException();
            }

            DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
                public boolean accept(Path file) throws IOException {
                    return "pom.xml".equals(file.getFileName().toString()) ||
                            "src".equals(file.getFileName().toString());
                }
            };

            DirectoryStream<Path> stream = Files.newDirectoryStream(javaProject, filter);
            boolean srcDirectoryNotFound = true;
            boolean pomXmlFileNotFound = true;
            for (Path fileOrDirectory : stream) {
                if (Files.isDirectory(fileOrDirectory) && "src".equals(fileOrDirectory.getFileName().toString())) {
                    srcDirectoryNotFound = false;
                }
                if (Files.isRegularFile(fileOrDirectory) && "pom.xml".equals(fileOrDirectory.getFileName().toString())) {
                    pomXmlFileNotFound = false;
                }
            }
            if (srcDirectoryNotFound || pomXmlFileNotFound) {
                throw new IllegalJavaProjectException();
            }
        }
    }

    enum JavaProjectType {JAVA_PROJECT, JAVA_PROJECT_MODULE;}

    JavaProjectType getJavaProjectType(Path mavenProject) throws IOException {
        if (!Files.exists(mavenProject)) {
            throw new JavaProjectDoesNotExistException();
        }

        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
            public boolean accept(Path file) throws IOException {
                return "pom.xml".equals(file.getFileName().toString()) ||
                        "src".equals(file.getFileName().toString());
            }
        };

        DirectoryStream<Path> stream = Files.newDirectoryStream(mavenProject, filter);
        boolean srcDirectoryNotFound = true;
        boolean pomXmlFileNotFound = true;
        for (Path fileOrDirectory : stream) {
            if (Files.isDirectory(fileOrDirectory) && "src".equals(fileOrDirectory.getFileName().toString())) {
                srcDirectoryNotFound = false;
            }
            if (Files.isRegularFile(fileOrDirectory) && "pom.xml".equals(fileOrDirectory.getFileName().toString())) {
                pomXmlFileNotFound = false;
            }
        }
        if ((srcDirectoryNotFound && pomXmlFileNotFound) ||
                (!srcDirectoryNotFound && pomXmlFileNotFound)) {
            throw new IllegalJavaProjectException();
        }
        if (srcDirectoryNotFound && !pomXmlFileNotFound) {
            return JavaProjectType.JAVA_PROJECT_MODULE;
        }
        // ! srcDirectoryNotFound && ! pomXmlFileNotFound
        return JavaProjectType.JAVA_PROJECT;
    }
    */
}
