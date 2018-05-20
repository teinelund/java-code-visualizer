package org.teinelund.javacodevisualizer.dom;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaProjectObjectModelFactoryImplTest {

    private static FileSystem fs = null;
    private static JavaProjectObjectModelFactoryImpl sut = null;
    private static Path projectPath = null;
    private static Path srcPath = null;
    private static Path pomXmlPath = null;
    private static Path projectSubproject1Path = null;
    private static Path projectSubproject2Path = null;
    private static Path projectTargetPath = null;

    @BeforeAll
    static void setup() {
        fs = Jimfs.newFileSystem(Configuration.unix());
        sut = new JavaProjectObjectModelFactoryImpl();
        projectPath = fs.getPath("/Users/Cody/Projects/Project");
        srcPath = fs.getPath(projectPath.toString(), "src");
        pomXmlPath = fs.getPath(projectPath.toString(), "pom.xml");
        projectSubproject1Path = fs.getPath("/Users/Cody/Projects/Project/SubProject1");
        projectSubproject2Path = fs.getPath("/Users/Cody/Projects/Project/SubProject2");
        projectTargetPath = fs.getPath("/Users/Cody/Projects/Project/target");
    }

    @BeforeEach
    void initTest() throws IOException {
        if (!Files.exists(projectPath)) {
            Files.createDirectories(projectPath);
            //System.out.println("  initTest. Path: " + projectPath.toString() + " is created.");
        }
    }

    @AfterEach
    void cleanUpTest() throws IOException {
        if (Files.exists(srcPath)) {
            deleteDirectory(srcPath);
        }
        if (Files.exists(pomXmlPath)) {
            Files.delete(pomXmlPath);
        }
    }

    /**
     * Help method to delete a directory recursevly. Apache IO's FileUtils.delete(...) does not work with Google
     * jimfs.
     *
     * @param path
     * @throws IOException
     */
    void deleteDirectory(Path path) throws IOException {
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

    @Test
    void containsJavaSourceCodeWhereSrcDirectoryIsEmpty() throws IOException {
        // Initialize
        Files.createDirectories(srcPath);
        // Test
        boolean result = sut.containsJavaSourceCode(srcPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void containsJavaSourceCodeWhereSrcDirectoryContainsSubDirectoriesWithJavaSourceCode() throws IOException {
        // Initialize
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(SrcDirectoryContentType.INCLUDE_JAVA_SOURCE_FILE);
        // Test
        boolean result = sut.containsJavaSourceCode(srcPath);
        // Verify
        assertTrue(result);
    }

    @Test
    void containsJavaSourceCodeWhereSrcDirectoryContainsSubDirectoriesWithNoJavaSourceCode() throws IOException {
        // Initialize
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(SrcDirectoryContentType.NO_JAVA_SOURCE_FILE);
        // Test
        boolean result = sut.containsJavaSourceCode(srcPath);
        // Verify
        assertFalse(result);
    }

    void createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(SrcDirectoryContentType srcDirectoryContentType) throws IOException {
        // src
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

    @Test
    void isMavenProjectWhereProjectPathIsEmpty() throws IOException {
        // Initialize
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void isMavenProjectWhereProjectOnlyContainsPomXmlFile() throws IOException {
        // Initialize
        createMavenProject(ProjectType.PROJECT_WITHOUT_SRC_DIRECTORY);
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void isMavenProjectWhereProjectOnlyContainsSrcDirectory() throws IOException {
        // Initialize
        createMavenProject(ProjectType.PROJECT_WITHOUT_POM_XML_FILE);
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void isMavenProjectWhereProjectDoesNotContainAnyJavaSourceCode() throws IOException {
        // Initialize
        createMavenProject(ProjectType.LEGAL_PROJECT);
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void isMavenProjectWhereProjectIsLegal() throws IOException {
        // Initialize
        createMavenProject(ProjectType.LEGAL_PROJECT);
        JavaProjectObjectModelFactoryImplMock sut = new JavaProjectObjectModelFactoryImplMock();
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertTrue(result);
    }

    void createMavenProject(ProjectType projectType) throws IOException {
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

    @Test
    void getMavenProjectWherePathDoesNotExist() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        Path path = fs.getPath("/this/path/does/not/exist");
        // Test
        List<Path> result = sut.getMavenProject(path, excludePaths);
        // Verify
        assertTrue(result.isEmpty());
    }


    @Test
    void getMavenProjectWherePathExistInExcludePaths() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        Path path = fs.getPath("/Users/Cody/Projects/Project");
        excludePaths.add(path);
        // Test
        List<Path> result = sut.getMavenProject(projectPath, excludePaths);
        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void getMavenProjectWhereProjectIsLegal() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        JavaProjectObjectModelFactoryImplMock2 sut = new JavaProjectObjectModelFactoryImplMock2(true);
        final int EXPECTED_SIZE = 1;
        // Test
        List<Path> result = sut.getMavenProject(projectPath, excludePaths);
        // Verify
        assertEquals(EXPECTED_SIZE, result.size());
    }

    @Test
    void getMavenProjectWhereProjectContainsTwoSubProjects() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        JavaProjectObjectModelFactoryImplMock2 sut = new JavaProjectObjectModelFactoryImplMock2(false);
        createSubProjects(false);
        final int EXPECTED_SIZE = 2;
        // Test
        List<Path> result = sut.getMavenProject(projectPath, excludePaths);
        // Verify
        assertEquals(EXPECTED_SIZE, result.size());
    }

    @Test
    void getMavenProjectWhereProjectContainsTwoSubProjectsAndTargetDirectory() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        JavaProjectObjectModelFactoryImplMock2 sut = new JavaProjectObjectModelFactoryImplMock2(false);
        createSubProjects(true);
        final int EXPECTED_SIZE = 2;
        // Test
        List<Path> result = sut.getMavenProject(projectPath, excludePaths);
        // Verify
        assertEquals(EXPECTED_SIZE, result.size());
    }

    void createSubProjects(boolean createTargetDirectory) throws IOException {
        // src
        Files.createDirectories(projectSubproject1Path);
        Files.createDirectories(projectSubproject2Path);
        if (createTargetDirectory) {
            Files.createDirectories(projectTargetPath);
        }
    }

    enum SrcDirectoryContentType {INCLUDE_JAVA_SOURCE_FILE, NO_JAVA_SOURCE_FILE;}

    enum ProjectType {LEGAL_PROJECT, PROJECT_WITHOUT_SRC_DIRECTORY, PROJECT_WITHOUT_POM_XML_FILE;}

    class JavaProjectObjectModelFactoryImplMock extends JavaProjectObjectModelFactoryImpl {

        @Override
        boolean containsJavaSourceCode(Path path) throws IOException {
            return true;
        }
    }

    class JavaProjectObjectModelFactoryImplMock2 extends JavaProjectObjectModelFactoryImpl {

        private boolean isProjectLegalMavenProject = false;

        public JavaProjectObjectModelFactoryImplMock2(boolean isProjectLegalMavenProject) {
            this.isProjectLegalMavenProject = isProjectLegalMavenProject;
        }

        @Override
        boolean isMavenProject(Path path) throws IOException {
            if (projectPath.equals(path)) {
                return this.isProjectLegalMavenProject;
            }
            return true;
        }
    }

}