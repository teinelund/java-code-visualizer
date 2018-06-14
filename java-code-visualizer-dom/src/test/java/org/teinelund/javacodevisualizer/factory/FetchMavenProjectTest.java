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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.teinelund.javacodevisualizer.factory.TestUtility.createMavenProject;
import static org.teinelund.javacodevisualizer.factory.TestUtility.createSrcDirectoryWithSubDirectoriesWithJavaSourceCode;
import static org.teinelund.javacodevisualizer.factory.TestUtility.deleteDirectory;

class FetchMavenProjectTest {

    private static FileSystem fs = null;
    private static JavaProjectObjectModelFactoryImpl sut = null;
    private static Path projectPath = null;
    private static Path srcPath = null;
    private static Path pomXmlPath = null;

    @BeforeAll
    static void setup() {
        fs = Jimfs.newFileSystem(Configuration.unix());
        sut = new JavaProjectObjectModelFactoryImpl();
        projectPath = fs.getPath("/Users/Cody/Projects/Project");
        srcPath = fs.getPath(projectPath.toString(), "src");
        pomXmlPath = fs.getPath(projectPath.toString(), "pom.xml");
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
    // Find Java Class Paths In Src Directory
    //

    @Test
    public void findJavaClassPathsInSrcDirectoryWhereDirectoriesDoesNotContainAnyJavaFiles() throws IOException {
        // Initialize
        FetchMavenProjectMock sut = new FetchMavenProjectMock();
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(fs, projectPath, srcPath, SrcDirectoryContentType.NO_JAVA_SOURCE_FILE);
        // Test
        List<JavaTypeDeclarationPath> result = sut.findJavaClassPathsInSrcDirectory(projectPath);
        // Verify
        assertThat(result.isEmpty()).isTrue();  //AssertJ
    }

    @Test
    public void findJavaClassPathsInSrcDirectoryWhereDirectoriesDoesContainJavaFiles() throws IOException {
        // Initialize
        FetchMavenProjectMock sut = new FetchMavenProjectMock();
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(fs, projectPath, srcPath, SrcDirectoryContentType.INCLUDE_JAVA_SOURCE_FILE);
        // Test
        List<JavaTypeDeclarationPath> result = sut.findJavaClassPathsInSrcDirectory(projectPath);
        // Verify
        assertThat(result.size()).isEqualTo(2); //AssertJ
    }



    //
    // get Maven Projects
    //

    @Test
    public void getMavenProjects() throws IOException {
        // Initialize
        createMavenProject(srcPath, pomXmlPath, ProjectType.LEGAL_PROJECT);
        FetchMavenProjectMock2 sut = new FetchMavenProjectMock2();
        List<Path> paths = new LinkedList<>();
        paths.add(projectPath);
        // Test
        List<MavenProject> result = sut.getMavenProjects(paths);
        // Verify
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAllTypeNames().size()).isEqualTo(2);
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

