package org.teinelund.javacodevisualizer.factory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.teinelund.javacodevisualizer.dom.AccessModifier;
import org.teinelund.javacodevisualizer.dom.DomainObjectModelFactory;
import org.teinelund.javacodevisualizer.dom.JavaProjectObjectModel;
import org.teinelund.javacodevisualizer.dom.JavaType;
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
    private static Path javaSourceFile = null;

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
        JavaProjectObjectModelFactoryImplMock2 sut = new JavaProjectObjectModelFactoryImplMock2(true);
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
        JavaProjectObjectModelFactoryImplMock2 sut = new JavaProjectObjectModelFactoryImplMock2(false);
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
        JavaProjectObjectModelFactoryImplMock2 sut = new JavaProjectObjectModelFactoryImplMock2(false);
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



    //
    // Parse Java File
    //



    private final String CLASS_NAME = "Customer";
    private final String INNER_CLASS_NAME = "CustomerCreditCard";
    private final String PACKAGE_NAME = "org.teinelund";
    private final String CLASS_NAME_CUSTOMER = "Customer";
    private final String CLASS_NAME_ORDER = "Order";


    @Test
    void parseJavaFileWhereSourceFileIsAClass() {
        // Initialize
        final int EXPECTED_SIZE = 1;
        Reader reader = createJavaSourceFileContainingAClassReader();
        // Test
        List<JavaTypeDeclarationPath> result = sut.parseJavaFile(reader, javaSourceFile);
        // Verify
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getName()).isEqualTo(CLASS_NAME);
        assertThat(result.get(0).getJavaType()).isEqualTo(JavaType.CLASS);
        assertThat(result.get(0).getAccessModifier()).isEqualTo(AccessModifier.PUBLIC);
        assertThat(result.get(0).getPackageName()).isEqualTo(PACKAGE_NAME);
        assertThat(result.get(0).getPathToTypeDeclaration()).isEqualTo(javaSourceFile);
    }

    Reader createJavaSourceFileContainingAClassReader() {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + PACKAGE_NAME + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("import java.io.IOException;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("public class " + CLASS_NAME + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append("   private String name;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("   public void setName(String name) {"); sb.append(java.lang.System.lineSeparator());
        sb.append("      this.name = name;"); sb.append(java.lang.System.lineSeparator());
        sb.append("   }"); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        return new StringReader(sb.toString());
    }

    @Test
    void parseJavaFileWhereSourceFileIsAInterface() {
        // Initialize
        final int EXPECTED_SIZE = 1;
        Reader reader = createJavaSourceFileContainingAInterfaceReader();
        // Test
        List<JavaTypeDeclarationPath> result = sut.parseJavaFile(reader, javaSourceFile);
        // Verify
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getName()).isEqualTo(CLASS_NAME);
        assertThat(result.get(0).getJavaType()).isEqualTo(JavaType.INTERFACE);
        assertThat(result.get(0).getAccessModifier()).isEqualTo(AccessModifier.PUBLIC);
        assertThat(result.get(0).getPackageName()).isEqualTo(PACKAGE_NAME);
        assertThat(result.get(0).getPathToTypeDeclaration()).isEqualTo(javaSourceFile);
    }

    Reader createJavaSourceFileContainingAInterfaceReader() {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + PACKAGE_NAME + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("import java.io.IOException;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("public interface " + CLASS_NAME + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("   public void setName(String name);"); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        return new StringReader(sb.toString());
    }

    @Test
    void parseJavaFileWhereSourceFileIsAEnum() {
        // Initialize
        final int EXPECTED_SIZE = 1;
        Reader reader = createJavaSourceFileContainingAEnumReader();
        // Test
        List<JavaTypeDeclarationPath> result = sut.parseJavaFile(reader, javaSourceFile);
        // Verify
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getName()).isEqualTo(CLASS_NAME);
        assertThat(result.get(0).getJavaType()).isEqualTo(JavaType.ENUM);
        assertThat(result.get(0).getAccessModifier()).isEqualTo(AccessModifier.PUBLIC);
        assertThat(result.get(0).getPackageName()).isEqualTo(PACKAGE_NAME);
        assertThat(result.get(0).getPathToTypeDeclaration()).isEqualTo(javaSourceFile);
    }

    Reader createJavaSourceFileContainingAEnumReader() {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + PACKAGE_NAME + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("import java.io.IOException;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("public enum " + CLASS_NAME + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("   NORMAL_CUSTOMER, VIP_CUSTOMER;"); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        return new StringReader(sb.toString());
    }

    @Test
    void parseJavaFileWhereSourceFileContainsClassAndEnum() {
        // Initialize
        final int EXPECTED_SIZE = 2;
        Reader reader = createJavaSourceFileContainingClassAndEnumReader();
        // Test
        List<JavaTypeDeclarationPath> result = sut.parseJavaFile(reader, javaSourceFile);
        // Verify
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getName()).isEqualTo(CLASS_NAME);
        assertThat(result.get(0).getJavaType()).isEqualTo(JavaType.CLASS);
        assertThat(result.get(0).getAccessModifier()).isEqualTo(AccessModifier.PUBLIC);
        assertThat(result.get(0).getPackageName()).isEqualTo(PACKAGE_NAME);
        assertThat(result.get(0).getPathToTypeDeclaration()).isEqualTo(javaSourceFile);
        assertThat(result.get(1)).isNotNull();
        assertThat(result.get(1).getName()).isEqualTo(CLASS_NAME);
        assertThat(result.get(1).getJavaType()).isEqualTo(JavaType.ENUM);
        assertThat(result.get(1).getAccessModifier()).isEqualTo(AccessModifier.PACKAGE);
        assertThat(result.get(1).getPackageName()).isEqualTo(PACKAGE_NAME);
        assertThat(result.get(1).getPathToTypeDeclaration()).isEqualTo(javaSourceFile);
    }

    Reader createJavaSourceFileContainingClassAndEnumReader() {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + PACKAGE_NAME + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("import java.io.IOException;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("public class " + CLASS_NAME + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append("   private String name;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("   public void setName(String name) {"); sb.append(java.lang.System.lineSeparator());
        sb.append("      this.name = name;"); sb.append(java.lang.System.lineSeparator());
        sb.append("   }"); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("enum " + CLASS_NAME + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("   NORMAL_CUSTOMER, VIP_CUSTOMER;"); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        return new StringReader(sb.toString());
    }

    //
    // NOTE! TEST IS DISABLED !!!!
    //
    @Disabled
    @Test
    void parseJavaFileWhereSourceFileContainsClassAndInnerClass() {
        // Initialize
        final int EXPECTED_SIZE = 1;
        Reader reader = createJavaSourceFileContainingClassAndInnerClass();
        // Test
        List<JavaTypeDeclarationPath> result = sut.parseJavaFile(reader, javaSourceFile);
        // Verify
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(EXPECTED_SIZE);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getName()).isEqualTo(CLASS_NAME);
        assertThat(result.get(0).getJavaType()).isEqualTo(JavaType.CLASS);
        assertThat(result.get(0).getAccessModifier()).isEqualTo(AccessModifier.PUBLIC);
        assertThat(result.get(0).getPackageName()).isEqualTo(PACKAGE_NAME);
        assertThat(result.get(0).getPathToTypeDeclaration()).isEqualTo(javaSourceFile);
    }

    Reader createJavaSourceFileContainingClassAndInnerClass() {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + PACKAGE_NAME + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("import java.io.IOException;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("public class " + CLASS_NAME + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append("   private String name;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("   public class " + INNER_CLASS_NAME + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append("      private String name;"); sb.append(java.lang.System.lineSeparator());
        sb.append("   }"); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        return new StringReader(sb.toString());
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

    class JavaProjectObjectModelFactoryImplMock4 extends JavaProjectObjectModelFactoryImpl {

        @Override
        List<JavaTypeDeclarationPath> findJavaClassPathsInSrcDirectory(Path path) throws IOException {
            List<JavaTypeDeclarationPath> list = new LinkedList<>();
            list.add(JavaTypeDeclarationPathBuilder.builder().setName("CLASS_1").build());
            list.add(JavaTypeDeclarationPathBuilder.builder().setName("CLASS_2").build());
            return list;
        }
    }

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
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().get(0).getType().getName()).isEqualTo(CLASS_NAME_CUSTOMER);
    }

    JavaProjectObjectModel createJavaProjectObjectModel(JavaProjectObjectModelFactoryImpl sut) {
        JavaProjectObjectModel jpom = DomainObjectModelFactory.instnace().createJavaProjectObjectModel();
        List<JavaTypeDeclarationPath> jtdps = sut.parseJavaFile(createJavaSourceFileContainingClassWithField(CLASS_NAME_CUSTOMER, "String", "name"), javaSourceFile);
        jpom.addJavaTypeDeclarationPaths(jtdps);
        jtdps = sut.parseJavaFile(createJavaSourceFileContainingClassWithField(CLASS_NAME_ORDER, CLASS_NAME_CUSTOMER, "customer"), javaSourceFile);
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