package org.teinelund.javacodevisualizer.factory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teinelund.javacodevisualizer.dom.DomainObjectModelFactory;
import org.teinelund.javacodevisualizer.dom.JavaProjectObjectModel;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPathBuilder;
import org.teinelund.javacodevisualizer.dom.MavenProject;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaProjectObjectModelFactoryImplTest {

    private static FileSystem fs = null;
    private static JavaProjectObjectModelFactoryImpl sut = null;
    private static Path projectPath = null;
    private static Path srcPath = null;
    private static Path pomXmlPath = null;
    private static Path javaSourceFile = null;

    private final String PACKAGE_NAME = "org.teinelund";
    private final String CLASS_NAME_CUSTOMER = "Customer";
    private final String CLASS_NAME_ORDER = "Order";

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
        JavaProjectObjectModelFactoryImpl sut = new JavaProjectObjectModelFactoryImplMock3();
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(SrcDirectoryContentType.NO_JAVA_SOURCE_FILE);
        // Test
        List<JavaTypeDeclarationPath> result = sut.findJavaClassPathsInSrcDirectory(projectPath);
        // Verify
        assertThat(result.isEmpty()).isTrue();  //AssertJ
    }

    @Test
    public void findJavaClassPathsInSrcDirectoryWhereDirectoriesDoesContainJavaFiles() throws IOException {
        // Initialize
        JavaProjectObjectModelFactoryImpl sut = new JavaProjectObjectModelFactoryImplMock3();
        createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(SrcDirectoryContentType.INCLUDE_JAVA_SOURCE_FILE);
        // Test
        List<JavaTypeDeclarationPath> result = sut.findJavaClassPathsInSrcDirectory(projectPath);
        // Verify
        assertThat(result.size()).isEqualTo(2); //AssertJ
    }

    class JavaProjectObjectModelFactoryImplMock3 extends JavaProjectObjectModelFactoryImpl {

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

    void createSrcDirectoryWithSubDirectoriesWithJavaSourceCode(JavaProjectObjectModelFactoryImplTest.SrcDirectoryContentType srcDirectoryContentType) throws IOException {
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
        JavaProjectObjectModelFactoryImpl sut = new JavaProjectObjectModelFactoryImplMock4();
        List<Path> paths = new LinkedList<>();
        paths.add(projectPath);
        // Test
        List<MavenProject> result = sut.getMavenProjects(paths);
        // Verify
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAllTypeNames().size()).isEqualTo(2);
    }

    void createMavenProject(JavaProjectObjectModelFactoryImplTest.ProjectType projectType) throws IOException {
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

    class JavaProjectObjectModelFactoryImplMock4 extends JavaProjectObjectModelFactoryImpl {

        @Override
        List<JavaTypeDeclarationPath> findJavaClassPathsInSrcDirectory(Path path) throws IOException {
            List<JavaTypeDeclarationPath> list = new LinkedList<>();
            list.add(JavaTypeDeclarationPathBuilder.builder().setName("CLASS_1").build());
            list.add(JavaTypeDeclarationPathBuilder.builder().setName("CLASS_2").build());
            return list;
        }
    }

    enum ProjectType {LEGAL_PROJECT, PROJECT_WITHOUT_SRC_DIRECTORY, PROJECT_WITHOUT_POM_XML_FILE;}


    
    //
    // wire Classes
    //

    @Test
    public void wireClassFieldsWhereClassExist() {
        // Initialize
        JavaProjectObjectModelFactoryImpl sut = new JavaProjectObjectModelFactoryImpl();
        JavaProjectObjectModel jdom = createJavaProjectObjectModel(sut);
        // Test
        sut.wireClassFields(jdom);
        // Verify
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().get(0).getType().getName()).
                isEqualTo(CLASS_NAME_CUSTOMER);
    }

    JavaProjectObjectModel createJavaProjectObjectModel(JavaProjectObjectModelFactoryImpl sut) {
        JavaProjectObjectModel jpom = DomainObjectModelFactory.instnace().createJavaProjectObjectModel();
        List<JavaTypeDeclarationPath> jtdps = JavaSourceFileParser.instance().parseJavaFile(
                createJavaSourceFileContainingClassWithField(CLASS_NAME_CUSTOMER, "String", "name"),
                javaSourceFile);
        jpom.addJavaTypeDeclarationPaths(jtdps);
        jtdps = JavaSourceFileParser.instance().parseJavaFile(
                createJavaSourceFileContainingClassWithField(CLASS_NAME_ORDER, CLASS_NAME_CUSTOMER, "customer"),
                javaSourceFile);
        jpom.addJavaTypeDeclarationPaths(jtdps);
        return jpom;
    }

    Reader createJavaSourceFileContainingClassWithField(String className, String fieldType, String fieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + PACKAGE_NAME + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("import java.io.IOException;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("public class " + className + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append("   private " + fieldType + " " + fieldName + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        return new StringReader(sb.toString());
    }


}