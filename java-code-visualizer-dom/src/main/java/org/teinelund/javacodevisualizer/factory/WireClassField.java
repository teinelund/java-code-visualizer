package org.teinelund.javacodevisualizer.factory;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.teinelund.javacodevisualizer.dom.JavaProjectObjectModel;
import org.teinelund.javacodevisualizer.dom.JavaType;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;
import org.teinelund.javacodevisualizer.dom.MavenProject;

import java.util.List;
import java.util.Optional;

class WireClassField {

    private static WireClassField wireClassField = null;

    private WireClassField() {}

    public static WireClassField instnace() {
        if (wireClassField == null) {
            wireClassField = new WireClassField();
        }
        return wireClassField;
    }

    void wireClasses(JavaProjectObjectModel jpom, List<MavenProject> mavenProjects) {
        addJavaTypeDeclarationPaths(jpom, mavenProjects);
    }


    void addJavaTypeDeclarationPaths(JavaProjectObjectModel jpom, List<MavenProject> mavenProjects) {
        for (MavenProject mavenProject : mavenProjects) {
            jpom.addJavaTypeDeclarationPaths(mavenProject.getAllTypes());
        }
    }

    void wireClassFields(JavaProjectObjectModel jpom) {
        // Iterate through all classes (and interfaces and enums)
        for (String typeName : jpom.getAllTypeNames()) {
            // Get a list of classes (and interfaces and enums) which have the same name (but different packages).
            // In most cases, the list contains only one class (or interface or enum).
            List<JavaTypeDeclarationPath> list = jpom.getAllTypesGivenName(typeName);
            // Iterate through the list of classes (or interfaces and enums) with the name 'typeName'.
            for (JavaTypeDeclarationPath thisClass : list) {
                wireClassFields(thisClass, jpom);
            }
        }
    }

    void wireClassFields(JavaTypeDeclarationPath thisClass, JavaProjectObjectModel jpom) {
        if (thisClass.getJavaType() == JavaType.CLASS) {
            // Get the TypeDeclaration, which is the parsed Java Parser object.
            TypeDeclaration<?> typeDeclaration = thisClass.getTypeDeclaration();
            // Get all declared fields in the class.
            List<FieldDeclaration> fields = typeDeclaration.getFields();
            for (FieldDeclaration fieldDeclaration : fields) {
                wireClassField(fieldDeclaration, thisClass, jpom);
            }
        }
    }

    void wireClassField(FieldDeclaration fieldDeclaration, JavaTypeDeclarationPath thisClass,
                        JavaProjectObjectModel jpom) {
        for (VariableDeclarator variable : fieldDeclaration.getVariables()) {
            Type fieldType = variable.getType();
            if (fieldType.isClassOrInterfaceType()) {
                ClassOrInterfaceType fieldClassType = (ClassOrInterfaceType) fieldType;
                SimpleName variableTypeName = fieldClassType.getName();
                Optional<ClassOrInterfaceType> optionalPackageName = fieldClassType.getScope();
                List<JavaTypeDeclarationPath> classesWithSameNameAsFieldDeclaration = jpom.getAllTypesGivenName(
                        variableTypeName.asString());
                if (optionalPackageName.isPresent()) {
                    String packageName = optionalPackageName.get().toString();
                    wireClassField(packageName, variable, classesWithSameNameAsFieldDeclaration, thisClass, jpom);
                }
                else {
                    wireClassField(variable, classesWithSameNameAsFieldDeclaration, thisClass, jpom);
                }
            }
        }
    }

    void wireClassField(VariableDeclarator variable, List<JavaTypeDeclarationPath> classesWithSameNameAsFieldDeclaration,
                        JavaTypeDeclarationPath thisClass, JavaProjectObjectModel jpom) {
        if (classesWithSameNameAsFieldDeclaration.size() == 1) {
            JavaTypeDeclarationPath fieldClass = classesWithSameNameAsFieldDeclaration.get(0);
            thisClass.addField(variable.getName().asString(), fieldClass);
        }
        else {
            for (JavaTypeDeclarationPath foundFieldClass : classesWithSameNameAsFieldDeclaration) {
                if (thisClass.getPackageName().equals(foundFieldClass.getPackageName())) {
                    thisClass.addField(variable.getName().asString(), foundFieldClass);
                    break;
                }
            }

        }
    }

    void wireClassField(String packageName, VariableDeclarator variable,
                        List<JavaTypeDeclarationPath> classesWithSameNameAsFieldDeclaration,
                        JavaTypeDeclarationPath thisClass, JavaProjectObjectModel jpom) {
        if (classesWithSameNameAsFieldDeclaration.size() == 1) {
            JavaTypeDeclarationPath fieldClass = classesWithSameNameAsFieldDeclaration.get(0);
            if (fieldClass.getPackageName().equals(packageName)) {
                thisClass.addField(variable.getName().asString(), fieldClass);
            }
        }
        else {
            for (JavaTypeDeclarationPath foundFieldClass : classesWithSameNameAsFieldDeclaration) {
                if (foundFieldClass.getPackageName().equals(packageName)) {
                    thisClass.addField(variable.getName().asString(), foundFieldClass);
                    break;
                }
            }

        }
    }
}
