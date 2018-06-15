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

class WireClassFieldTest {

    private static FileSystem fs = null;
    private static WireClassField sut = null;
    private static Path javaSourceFile = null;

    private final String PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM = "org.teinelund.ordersystem";
    private final String PACKAGE_NAME_ORG_TEINELUND_TICKETAPP = "org.teinelund.ticketapp";
    private final String CLASS_NAME_CUSTOMER = "Customer";
    private final String CLASS_NAME_ORDER = "Order";

    @BeforeAll
    static void setup() {
        sut = WireClassField.instnace();
        fs = Jimfs.newFileSystem(Configuration.unix());
        javaSourceFile = fs.getPath("/Users/Cody/Projects/Project/src/org/teinelund/customer.java");
    }

    /**
     * In this test an Order and a Customer in the same package exist. Order has a Customer as a field.
     *
     * Order and Customer should be connected.
     */
    @Test
    public void wireClassFieldsWithOneCustomerExist() {
        // Initialize
        JavaProjectObjectModel jdom = createJavaProjectObjectModel(JavaProjectObjectModelState.ONE_CUSTOMER);
        // Test
        sut.wireClassFields(jdom);
        // Verify
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).size()).isEqualTo(1);
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().size()).isEqualTo(1);
        JavaTypeDeclarationPath fieldClass = jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().get(0).getType();
        assertThat(fieldClass.getName()).isEqualTo(CLASS_NAME_CUSTOMER);
        assertThat(fieldClass.getPackageName()).isEqualTo(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM);
    }

    /**
     * In this test an Order and a Customer in the same package exist. Order has a Customer as a field.
     *
     * Order and Customer should be connected.
     */
    @Test
    public void wireClassFieldsWithOneCustomerWithExplicitPackageName() {
        // Initialize
        JavaProjectObjectModel jdom = createJavaProjectObjectModel(JavaProjectObjectModelState.ONE_CUSTOMER_WITH_EXPLICIT_PACKAGE_NAME);
        // Test
        sut.wireClassFields(jdom);
        // Verify
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).size()).isEqualTo(1);
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().size()).isEqualTo(1);
        JavaTypeDeclarationPath fieldClass = jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().get(0).getType();
        assertThat(fieldClass.getName()).isEqualTo(CLASS_NAME_CUSTOMER);
        assertThat(fieldClass.getPackageName()).isEqualTo(PACKAGE_NAME_ORG_TEINELUND_TICKETAPP);
    }

    /**
     * In this test an Order and two Customer exist. One Customer exist in the same package as Order (the other
     * Customer reside in an other package). Order has a Customer as a field, but no explicit package name
     * is specified.
     *
     * Order should be connected to the Customer in the same package.
     */
    @Test
    public void wireClassFieldsWhereTwoCustomerExistWithoutExplicitPackageName() {
        // Initialize
        JavaProjectObjectModel jdom = createJavaProjectObjectModel(JavaProjectObjectModelState.TWO_CUSTOMER);
        // Test
        sut.wireClassFields(jdom);
        // Verify
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).size()).isEqualTo(1);
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().size()).isEqualTo(1);
        JavaTypeDeclarationPath fieldClass = jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().get(0).getType();
        assertThat(fieldClass.getName()).isEqualTo(CLASS_NAME_CUSTOMER);
        assertThat(fieldClass.getPackageName()).isEqualTo(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM);
    }

    /**
     * In this test an Order and two Customer exist. One Customer exist in the same package as Order (the other
     * Customer reside in an other package). Order has a Customer as a field with explicit package name.
     * is specified.
     *
     * Order should be connected to the Customer in the explicit package.
     */
    @Test
    public void wireClassFieldsWhereTwoCustomerExistWithExplicitPackageName() {
        // Initialize
        JavaProjectObjectModel jdom = createJavaProjectObjectModel(JavaProjectObjectModelState.TWO_CUSTOMER_WITH_EXPLICIT_PACKAGE_NAME);
        // Test
        sut.wireClassFields(jdom);
        // Verify
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).size()).isEqualTo(1);
        assertThat(jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().size()).isEqualTo(1);
        JavaTypeDeclarationPath fieldClass = jdom.getAllTypesGivenName(CLASS_NAME_ORDER).get(0).getFields().get(0).getType();
        assertThat(fieldClass.getName()).isEqualTo(CLASS_NAME_CUSTOMER);
        assertThat(fieldClass.getPackageName()).isEqualTo(PACKAGE_NAME_ORG_TEINELUND_TICKETAPP);
    }

    JavaProjectObjectModel createJavaProjectObjectModel(JavaProjectObjectModelState state) {
        JavaProjectObjectModel jpom = DomainObjectModelFactory.instnace().createJavaProjectObjectModel();
        List<JavaTypeDeclarationPath> jtdps = null;
        switch (state) {
            case ONE_CUSTOMER:
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM, CLASS_NAME_CUSTOMER, null,"String", "name"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM, CLASS_NAME_ORDER, null, CLASS_NAME_CUSTOMER, "customer"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                break;
            case ONE_CUSTOMER_WITH_EXPLICIT_PACKAGE_NAME:
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_TICKETAPP, CLASS_NAME_CUSTOMER, null,"String", "name"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM, CLASS_NAME_ORDER, PACKAGE_NAME_ORG_TEINELUND_TICKETAPP, CLASS_NAME_CUSTOMER, "customer"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                break;
            case TWO_CUSTOMER:
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM, CLASS_NAME_CUSTOMER, null,"String", "name"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM, CLASS_NAME_ORDER, null, CLASS_NAME_CUSTOMER, "customer"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_TICKETAPP, CLASS_NAME_CUSTOMER, null,"String", "name"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                break;
            case TWO_CUSTOMER_WITH_EXPLICIT_PACKAGE_NAME:
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM, CLASS_NAME_CUSTOMER, null,"String", "name"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_ORDERSYSTEM, CLASS_NAME_ORDER, PACKAGE_NAME_ORG_TEINELUND_TICKETAPP, CLASS_NAME_CUSTOMER, "customer"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                jtdps = JavaSourceFileParser.instance().parseJavaFile(
                        createJavaSourceFileContainingClassWithField(PACKAGE_NAME_ORG_TEINELUND_TICKETAPP, CLASS_NAME_CUSTOMER, null,"String", "name"),
                        javaSourceFile);
                jpom.addJavaTypeDeclarationPaths(jtdps);
                break;
        }
        return jpom;
    }

    enum JavaProjectObjectModelState {ONE_CUSTOMER, ONE_CUSTOMER_WITH_EXPLICIT_PACKAGE_NAME, TWO_CUSTOMER, TWO_CUSTOMER_WITH_EXPLICIT_PACKAGE_NAME;}

    Reader createJavaSourceFileContainingClassWithField(String packageName, String className, String fieldTypePackageName, String fieldType, String fieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append("package " + packageName + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("import java.io.IOException;"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("public class " + className + " {"); sb.append(java.lang.System.lineSeparator());
        sb.append("   private ");
        if (fieldTypePackageName != null) {
            sb.append(fieldTypePackageName);
            if (!fieldTypePackageName.endsWith(".")) {
                sb.append(".");
            }
        }
        sb.append(fieldType + " " + fieldName + ";"); sb.append(java.lang.System.lineSeparator());
        sb.append(""); sb.append(java.lang.System.lineSeparator());
        sb.append("}"); sb.append(java.lang.System.lineSeparator());
        return new StringReader(sb.toString());
    }

}