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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class JavaProjectObjectModelFactoryImpl implements JavaProjectObjectModelFactory {


    @Override
    public JavaProjectObjectModel createrAndStore(List<Path> javaProjectPaths, List<Path> excludePaths, Path storagePath) throws IOException {
        List<Path> mavenProjectPaths = MavenProjectPath.instance().getMavenProjectPaths(javaProjectPaths, excludePaths);
        List<MavenProject> mavenProjects = getMavenProjects(mavenProjectPaths);
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



    List<MavenProject> getMavenProjects(List<Path> mavenProjectPaths) throws IOException {
        List<MavenProject> mavenProjects = new LinkedList<>();
        for (Path path : mavenProjectPaths) {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            Path srcDirectory = null;
            for (Path fileOrDirectoryPath : stream) {
                if (Files.isDirectory(fileOrDirectoryPath) && "src".equals(fileOrDirectoryPath.getFileName().toString())) {
                    srcDirectory = fileOrDirectoryPath;
                    break;
                }
            }
            List<JavaTypeDeclarationPath> javaTypeDeclarationPaths = findJavaClassPathsInSrcDirectory(srcDirectory);
            MavenProject mp = DomainObjectModelFactory.instnace().createMavenProject(path, javaTypeDeclarationPaths);
            mavenProjects.add(mp);
        }
        return mavenProjects;
    }



    List<JavaTypeDeclarationPath> findJavaClassPathsInSrcDirectory(Path path) throws IOException {
        List<JavaTypeDeclarationPath> javaTypeDeclarationPaths = new LinkedList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path fileOrDirectoryPath : stream) {
            if (Files.isDirectory(fileOrDirectoryPath)) {
                javaTypeDeclarationPaths.addAll(findJavaClassPathsInSrcDirectory(fileOrDirectoryPath));
            }
            String filename = fileOrDirectoryPath.getFileName().toString();
            if (Files.isRegularFile(fileOrDirectoryPath) && filename.endsWith(".java")) {
                javaTypeDeclarationPaths.addAll(readJavaFile(fileOrDirectoryPath));
            }
        }
        return javaTypeDeclarationPaths;
    }

    List<JavaTypeDeclarationPath> readJavaFile(Path path) throws IOException {
        return JavaSourceFileParser.instance().readJavaFile(path);
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
