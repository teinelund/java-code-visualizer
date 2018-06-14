package org.teinelund.javacodevisualizer.factory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.teinelund.javacodevisualizer.dom.AccessModifier;
import org.teinelund.javacodevisualizer.dom.JavaType;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaSourceFileParserTest {

    private static JavaSourceFileParser sut = null;
    private final String CLASS_NAME = "Customer";
    private final String INNER_CLASS_NAME = "CustomerCreditCard";
    private final String PACKAGE_NAME = "org.teinelund";
    private static FileSystem fs = null;
    private static Path projectPath = null;
    private static Path srcPath = null;
    private static Path javaSourceFile = null;


    @BeforeAll
    static void setup() {
        sut = JavaSourceFileParser.instance();
        fs = Jimfs.newFileSystem(Configuration.unix());
        projectPath = fs.getPath("/Users/Cody/Projects/Project");
        srcPath = fs.getPath(projectPath.toString(), "src");
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
}