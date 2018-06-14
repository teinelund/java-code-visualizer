package org.teinelund.javacodevisualizer.factory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.teinelund.javacodevisualizer.factory.TestUtility.createMavenProject;
import static org.teinelund.javacodevisualizer.factory.TestUtility.createSrcDirectoryWithSubDirectoriesWithJavaSourceCode;
import static org.teinelund.javacodevisualizer.factory.TestUtility.deleteDirectory;

class MavenProjectPathTest {

    private static FileSystem fs = null;
    private static MavenProjectPath sut = null;
    static Path projectPath = null;
    private static Path srcPath = null;
    private static Path pomXmlPath = null;
    private static Path projectSubproject1Path = null;
    private static Path projectSubproject2Path = null;
    private static Path projectTargetPath = null;


    @BeforeAll
    static void setup() {
        fs = Jimfs.newFileSystem(Configuration.unix());
        sut = MavenProjectPath.instance();
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


    //
    // Contains Java Source Code
    //


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
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(fs, projectPath, srcPath, SrcDirectoryContentType.INCLUDE_JAVA_SOURCE_FILE);
        // Test
        boolean result = sut.containsJavaSourceCode(srcPath);
        // Verify
        assertTrue(result);
    }

    @Test
    void containsJavaSourceCodeWhereSrcDirectoryContainsSubDirectoriesWithNoJavaSourceCode() throws IOException {
        // Initialize
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(fs, projectPath, srcPath, SrcDirectoryContentType.NO_JAVA_SOURCE_FILE);
        // Test
        boolean result = sut.containsJavaSourceCode(srcPath);
        // Verify
        assertFalse(result);
    }


    //
    // Is Maven Project
    //

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
        createMavenProject(srcPath, pomXmlPath, ProjectType.PROJECT_WITHOUT_SRC_DIRECTORY);
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void isMavenProjectWhereProjectOnlyContainsSrcDirectory() throws IOException {
        // Initialize
        createMavenProject(srcPath, pomXmlPath, ProjectType.PROJECT_WITHOUT_POM_XML_FILE);
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void isMavenProjectWhereProjectDoesNotContainAnyJavaSourceCode() throws IOException {
        // Initialize
        createMavenProject(srcPath, pomXmlPath, ProjectType.LEGAL_PROJECT);
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertFalse(result);
    }

    @Test
    void isMavenProjectWhereProjectIsLegal() throws IOException {
        // Initialize
        createMavenProject(srcPath, pomXmlPath, ProjectType.LEGAL_PROJECT);
        MavenProjectPathMock2 sut = new MavenProjectPathMock2();
        // Test
        boolean result = sut.isMavenProject(projectPath);
        // Verify
        assertTrue(result);
    }

    //
    // Get Maven Project Paths
    //


    @Test
    void getMavenProjectWherePathDoesNotExist() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        Path path = fs.getPath("/this/path/does/not/exist");
        // Test
        List<Path> result = sut.getMavenProjectPaths(path, excludePaths);
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
        List<Path> result = sut.getMavenProjectPaths(projectPath, excludePaths);
        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void getMavenProjectWhereProjectIsLegal() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        MavenProjectPathMock sut = new MavenProjectPathMock(true);
        final int EXPECTED_SIZE = 1;
        // Test
        List<Path> result = sut.getMavenProjectPaths(projectPath, excludePaths);
        // Verify
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
    }

    @Test
    void getMavenProjectWhereProjectContainsTwoSubProjects() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        MavenProjectPathMock sut = new MavenProjectPathMock(false);
        createSubProjects(false);
        final int EXPECTED_SIZE = 2;
        // Test
        List<Path> result = sut.getMavenProjectPaths(projectPath, excludePaths);
        // Verify
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
    }

    @Test
    void getMavenProjectWhereProjectContainsTwoSubProjectsAndTargetDirectory() throws IOException {
        // Initialize
        List<Path> excludePaths = new LinkedList<>();
        MavenProjectPathMock sut = new MavenProjectPathMock(false);
        createSubProjects(true);
        final int EXPECTED_SIZE = 2;
        // Test
        List<Path> result = sut.getMavenProjectPaths(projectPath, excludePaths);
        // Verify
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
    }

    void createSubProjects(boolean createTargetDirectory) throws IOException {
        // src
        Files.createDirectories(projectSubproject1Path);
        Files.createDirectories(projectSubproject2Path);
        if (createTargetDirectory) {
            Files.createDirectories(projectTargetPath);
        }
    }

}

class MavenProjectPathMock extends MavenProjectPath {

    private boolean isProjectLegalMavenProject = false;

    public MavenProjectPathMock(boolean isProjectLegalMavenProject) {
        this.isProjectLegalMavenProject = isProjectLegalMavenProject;
    }

    @Override
    boolean isMavenProject(Path path) throws IOException {
        if (MavenProjectPathTest.projectPath.equals(path)) {
            return this.isProjectLegalMavenProject;
        }
        return true;
    }
}

class MavenProjectPathMock2 extends MavenProjectPath {

    @Override
    boolean containsJavaSourceCode(Path path) throws IOException {
        return true;
    }
}