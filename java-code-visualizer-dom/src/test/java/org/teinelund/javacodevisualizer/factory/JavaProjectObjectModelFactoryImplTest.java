package org.teinelund.javacodevisualizer.factory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.teinelund.javacodevisualizer.dom.DomainObjectModelFactory;
import org.teinelund.javacodevisualizer.dom.JavaProjectObjectModel;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaProjectObjectModelFactoryImplTest {

    private static FileSystem fs = null;
    private static JavaProjectObjectModelFactoryImpl sut = null;
    private static Path javaSourceFile = null;

    private final String PACKAGE_NAME = "org.teinelund";
    private final String CLASS_NAME_CUSTOMER = "Customer";
    private final String CLASS_NAME_ORDER = "Order";

    @BeforeAll
    static void setup() {
        fs = Jimfs.newFileSystem(Configuration.unix());
        javaSourceFile = fs.getPath("/Users/Cody/Projects/Project/src/org/teinelund/customer.java");
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