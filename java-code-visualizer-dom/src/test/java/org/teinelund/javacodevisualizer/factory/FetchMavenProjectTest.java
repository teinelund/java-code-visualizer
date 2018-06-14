package org.teinelund.javacodevisualizer.factory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPathBuilder;
import org.teinelund.javacodevisualizer.dom.MavenProject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FetchMavenProjectTest {

    private static FileSystem fs = null;
    private static JavaProjectObjectModelFactoryImpl sut = null;
    private static Path projectPath = null;
    private static Path srcPath = null;
    private static Path pomXmlPath = null;
    private static Path javaSourceFile = null;

    @BeforeAll
    static void setup() {
        fs = Jimfs.newFileSystem(Configuration.unix());
        sut = new JavaProjectObjectModelFactoryImpl();
        projectPath = fs.getPath("/Users/Cody/Projects/Project");
        srcPath = fs.getPath(projectPath.toString(), "src");
        pomXmlPath = fs.getPath(projectPath.toString(), "pom.xml");
        javaSourceFile = fs.getPath(srcPath.toString(), "org/teinelund/customer.java");
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


    //
    // Find Java Class Paths In Src Directory
    //

    @Test
    public void findJavaClassPathsInSrcDirectoryWhereDirectoriesDoesNotContainAnyJavaFiles() throws IOException {
        // Initialize
        FetchMavenProjectMock sut = new FetchMavenProjectMock();
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(SrcDirectoryContentType.NO_JAVA_SOURCE_FILE);
        // Test
        List<JavaTypeDeclarationPath> result = sut.findJavaClassPathsInSrcDirectory(projectPath);
        // Verify
        assertThat(result.isEmpty()).isTrue();  //AssertJ
    }

    @Test
    public void findJavaClassPathsInSrcDirectoryWhereDirectoriesDoesContainJavaFiles() throws IOException {
        // Initialize
        FetchMavenProjectMock sut = new FetchMavenProjectMock();
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(SrcDirectoryContentType.INCLUDE_JAVA_SOURCE_FILE);
        // Test
        List<JavaTypeDeclarationPath> result = sut.findJavaClassPathsInSrcDirectory(projectPath);
        // Verify
        assertThat(result.size()).isEqualTo(2); //AssertJ
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

    enum SrcDirectoryContentType {INCLUDE_JAVA_SOURCE_FILE, NO_JAVA_SOURCE_FILE;}


    //
    // get Maven Projects
    //

    @Test
    public void getMavenProjects() throws IOException {
        // Initialize
        createMavenProject(ProjectType.LEGAL_PROJECT);
        FetchMavenProjectMock2 sut = new FetchMavenProjectMock2();
        List<Path> paths = new LinkedList<>();
        paths.add(projectPath);
        // Test
        List<MavenProject> result = sut.getMavenProjects(paths);
        // Verify
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAllTypeNames().size()).isEqualTo(2);
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

}


class FetchMavenProjectMock extends FetchMavenProject {

    @Override
    List<JavaTypeDeclarationPath> readJavaFile(Path path) throws IOException {
        List<JavaTypeDeclarationPath> list = new LinkedList<>();
        JavaTypeDeclarationPath jtdp = JavaTypeDeclarationPathBuilder.builder().build();
        list.add(jtdp);
        jtdp = JavaTypeDeclarationPathBuilder.builder().build();
        list.add(jtdp);
        return list;
    }
}

class FetchMavenProjectMock2 extends FetchMavenProject {

    @Override
    List<JavaTypeDeclarationPath> findJavaClassPathsInSrcDirectory(Path path) throws IOException {
        List<JavaTypeDeclarationPath> list = new LinkedList<>();
        list.add(JavaTypeDeclarationPathBuilder.builder().setName("CLASS_1").build());
        list.add(JavaTypeDeclarationPathBuilder.builder().setName("CLASS_2").build());
        return list;
    }
}

enum SrcDirectoryContentType {INCLUDE_JAVA_SOURCE_FILE, NO_JAVA_SOURCE_FILE;}

enum ProjectType {LEGAL_PROJECT, PROJECT_WITHOUT_SRC_DIRECTORY, PROJECT_WITHOUT_POM_XML_FILE;}
