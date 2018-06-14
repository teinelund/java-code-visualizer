package org.teinelund.javacodevisualizer.factory;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.teinelund.javacodevisualizer.dom.DomainObjectModelFactory;
import org.teinelund.javacodevisualizer.dom.JavaProjectObjectModel;
import org.teinelund.javacodevisualizer.dom.JavaType;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;
import org.teinelund.javacodevisualizer.dom.MavenProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavaProjectObjectModelFactoryImpl implements JavaProjectObjectModelFactory {


    @Override
    public JavaProjectObjectModel createrAndStore(List<Path> javaProjectPaths, List<Path> excludePaths, Path storagePath) throws IOException {
        List<Path> mavenProjectPaths = MavenProjectPath.instance().getMavenProjectPaths(javaProjectPaths, excludePaths);
        List<MavenProject> mavenProjects = FetchMavenProject.instance().getMavenProjects(mavenProjectPaths);
        JavaProjectObjectModel jpom = DomainObjectModelFactory.instnace().createJavaProjectObjectModel();
        wireClasses(jpom, mavenProjects);
        return jpom;
    }

    @Override
    public JavaProjectObjectModel loadAndCreate(Path storagePath) {
        return null;
    }


    //
    // -------------------------------------------------------------------------------------------------------------
    //

    void wireClasses(JavaProjectObjectModel jpom, List<MavenProject> mavenProjects) {
        addJavaTypeDeclarationPaths(jpom, mavenProjects);
    }


    void addJavaTypeDeclarationPaths(JavaProjectObjectModel jpom, List<MavenProject> mavenProjects) {
        for (MavenProject mavenProject : mavenProjects) {
            jpom.addJavaTypeDeclarationPaths(mavenProject.getAllTypes());
        }
    }

    void wireClassFields(JavaProjectObjectModel jpom) {
        for (String typeName : jpom.getAllTypeNames()) {
            List<JavaTypeDeclarationPath> list = jpom.getAllTypesGivenName(typeName);
            for (JavaTypeDeclarationPath jtdp : list) {
                if (jtdp.getJavaType() == JavaType.CLASS) {
                    TypeDeclaration<?> typeDeclaration = jtdp.getTypeDeclaration();
                    List<FieldDeclaration> fields = typeDeclaration.getFields();
                    for (FieldDeclaration fieldDeclaration : fields) {
                        for (VariableDeclarator variable : fieldDeclaration.getVariables()) {
                            List<JavaTypeDeclarationPath> classes = jpom.getAllTypesGivenName(variable.getType().asString());
                            if (classes.size() == 1) {
                                JavaTypeDeclarationPath fieldClass = classes.get(0);
                                jtdp.addField(variable.getName().asString(), fieldClass);
                            }
                            else {
                                // More than one class with the same name? Check which one in the import statement.

                            }
                        }

                    }
                }
            }
        }
    }

}
