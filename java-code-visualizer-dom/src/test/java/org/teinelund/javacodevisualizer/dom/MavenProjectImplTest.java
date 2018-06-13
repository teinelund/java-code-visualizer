package org.teinelund.javacodevisualizer.dom;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MavenProjectImplTest {

    private final String CLASS_NAME_1 = "Class1";
    private final String CLASS_NAME_2 = "Class2";
    private final String CLASS_NAME_3 = "Class3";
    private final String PACKAGE_PATH_1 = "org.teinelund.project1";
    private final String PACKAGE_PATH_2 = "org.teinelund.project2";
    private final Path PATH_1 = Paths.get("/Users/Cody/java/project1/org.teinelund.project1.Class1.java");
    private final Path PATH_2 = Paths.get("/Users/Cody/java/project1/org.teinelund.project1.Class2.java");
    private final Path PATH_3 = Paths.get("/Users/Cody/java/project2/org.teinelund.project1.Class3.java");

    @Test
    public void constructor() {
        // Initialize
        // Test
        MavenProject sut = new MavenProjectImpl(null, createJavaTypeDeclarationPaths());
        // Verify
        assertThat(sut.getAllPackageNames()).isNotNull();
        assertThat(sut.getAllTypeNames()).isNotNull();
        assertThat(sut.getAllTypeNames().size()).isEqualTo(3);
        assertThat(sut.getAllPackageNames().size()).isEqualTo(2);
        assertThat(sut.getAllTypesGivenName(CLASS_NAME_1).size()).isEqualTo(1);
        assertThat(sut.getAllTypesGivenName(CLASS_NAME_1).get(0).getName()).isEqualTo(CLASS_NAME_1);
        assertThat(sut.getAllTypesGivenName(CLASS_NAME_2).size()).isEqualTo(1);
        assertThat(sut.getAllTypesGivenName(CLASS_NAME_2).get(0).getName()).isEqualTo(CLASS_NAME_2);
        assertThat(sut.getAllTypesGivenName(CLASS_NAME_3).size()).isEqualTo(1);
        assertThat(sut.getAllTypesGivenName(CLASS_NAME_3).get(0).getName()).isEqualTo(CLASS_NAME_3);
        assertThat(sut.getAllTypesGivenPackageName(PACKAGE_PATH_1).size()).isEqualTo(2);
        assertThat(sut.getAllTypesGivenPackageName(PACKAGE_PATH_2).size()).isEqualTo(1);
    }

    List<JavaTypeDeclarationPath> createJavaTypeDeclarationPaths() {
        List<JavaTypeDeclarationPath> list = new LinkedList<>();
        list.add(JavaTypeDeclarationPathBuilder.builder().setName(CLASS_NAME_1).setPackageName(PACKAGE_PATH_1).
                setPathToTypeDeclaration(PATH_1).setAccessModifier(AccessModifier.PUBLIC).setJavaType(JavaType.CLASS).build());
        list.add(JavaTypeDeclarationPathBuilder.builder().setName(CLASS_NAME_2).setPackageName(PACKAGE_PATH_1).
                setPathToTypeDeclaration(PATH_2).setAccessModifier(AccessModifier.PUBLIC).setJavaType(JavaType.CLASS).build());
        list.add(JavaTypeDeclarationPathBuilder.builder().setName(CLASS_NAME_3).setPackageName(PACKAGE_PATH_2).
                setPathToTypeDeclaration(PATH_3).setAccessModifier(AccessModifier.PUBLIC).setJavaType(JavaType.CLASS).build());
        return list;
    }

}