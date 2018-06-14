package org.teinelund.javacodevisualizer.factory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtility {


    /**
     * Help method to delete a directory recursevly. Apache IO's FileUtils.delete(...) does not work with Google
     * jimfs.
     *
     * @param path
     * @throws IOException
     */
    public static void deleteDirectory(Path path) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path fileOrDirectoryPath : stream) {
            if (Files.isRegularFile(fileOrDirectoryPath)) {
                Files.delete(fileOrDirectoryPath);
            }
            if (Files.isDirectory(fileOrDirectoryPath)) {
                deleteDirectory(fileOrDirectoryPath);
            }
        }
        Files.delete(path);
    }

    public static void createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(FileSystem fs, Path projectPath, Path srcPath, SrcDirectoryContentType srcDirectoryContentType) throws IOException {
        Files.createDirectories(srcPath);
        Path javaPath = fs.getPath(srcPath.toString(), "java");
        Files.createDirectories(javaPath);
        Path resourcePath = fs.getPath(srcPath.toString(), "resource");
        Files.createDirectories(resourcePath);
        Path readmePath = fs.getPath(srcPath.toString(), "README.txt");
        Files.createFile(readmePath);
        // java
        Path myappPath = fs.getPath(javaPath.toString(), "myapp");
        Files.createDirectories(myappPath);
        // myapp : java/myapp/Application.java
        Path path = null;
        switch (srcDirectoryContentType) {
            case INCLUDE_JAVA_SOURCE_FILE:
                path = fs.getPath(myappPath.toString(), "Application.java");
                Files.createFile(path);
                break;
            case NO_JAVA_SOURCE_FILE:
                path = fs.getPath(myappPath.toString(), "TODO.txt");
                Files.createFile(path);
        }
        // resource : resource/environment.properties
        Path envpropPath = fs.getPath(resourcePath.toString(), "environment.properties");
        Files.createFile(envpropPath);
    }

    public static void createMavenProject(Path srcPath, Path pomXmlPath, ProjectType projectType) throws IOException {
        switch (projectType) {
            case LEGAL_PROJECT:
                Files.createDirectory(srcPath);
                Files.createFile(pomXmlPath);
                break;
            case PROJECT_WITHOUT_POM_XML_FILE:
                Files.createDirectory(srcPath);
                break;
            case PROJECT_WITHOUT_SRC_DIRECTORY:
                Files.createFile(pomXmlPath);
                break;
        }
    }
}
