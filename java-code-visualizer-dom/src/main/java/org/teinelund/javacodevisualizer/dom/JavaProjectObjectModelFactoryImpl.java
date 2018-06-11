package org.teinelund.javacodevisualizer.dom;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class JavaProjectObjectModelFactoryImpl implements JavaProjectObjectModelFactory {


    @Override
    public JavaProjectObjectModel createrAndStore(List<Path> javaProjectPaths, List<Path> excludePaths, Path storagePath) throws IOException {
        List<Path> mavenProjectPaths = getMavenProjectPaths(javaProjectPaths, excludePaths);
        List<MavenProject> mavenProjects = getMavenProjects(mavenProjectPaths);
        return null;
    }

    @Override
    public JavaProjectObjectModel loadAndCreate(Path storagePath) {
        return null;
    }

    //
    // -------------------------------------------------------------------------------------------------------------
    //

    /**
     * This method returns a list of Path objects, which are maven projects (with java source code),
     * given a list of Path objects.
     *
     * @param javaProjectPaths is a list of Path objects.
     * @param excludePaths is a list of Path objects containing Path to exclude.
     * @return a list of Path objects.
     * @throws IOException
     */
    List<Path> getMavenProjectPaths(List<Path> javaProjectPaths, List<Path> excludePaths) throws IOException {
        List<Path> paths = new LinkedList<>();
        for (Path javaProjectPath : javaProjectPaths) {
            paths.addAll(getMavenProjectPaths(javaProjectPath, excludePaths));
        }
        return paths;
    }

    /**
     * This method returns a list of Path objects, which all are maven projects (with java source code),
     * given a Path object.
     *
     * @param javaProjectPath is a Path object.
     * @param excludePaths is a list of Path objects containing Path to exclude.
     * @return a list of Path objects.
     * @throws IOException
     */
    List<Path> getMavenProjectPaths(Path javaProjectPath, List<Path> excludePaths) throws IOException {

        if (!Files.exists(javaProjectPath)) {
            return new LinkedList<>();
        }
        if (excludePaths.contains(javaProjectPath)) {
            return new LinkedList<>();
        }

        List<Path> paths = new LinkedList<>();
        if (isMavenProject(javaProjectPath)) {
            paths.add(javaProjectPath);
        }
        else {
            DirectoryStream<Path> stream = Files.newDirectoryStream(javaProjectPath);
            for (Path fileOrDirectoryPath : stream) {
                if (Files.isDirectory(fileOrDirectoryPath) && "target".equals(fileOrDirectoryPath.getFileName().toString())) {
                    continue;
                }
                if (Files.isDirectory(fileOrDirectoryPath)) {
                    paths.addAll(getMavenProjectPaths(fileOrDirectoryPath, excludePaths));
                }
            }
        }
        return paths;
    }

    /**
     * This method validated that a Path object contains a valid Maven project. To be a valid Maven
     * project a Path must:
     * # be a directory
     * # that directory must contains a pom.xml file
     * # that directory must contain a src directory
     * # that src directory or its sub directories contain one or more java source files.
     *
     * @param path is a Path object
     * @return true if the Path is a Maven project, otherwise false.
     * @throws IOException
     */
    boolean isMavenProject(Path path) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        Path srcDirectory = null;
        boolean pomXmlFileFound = false;
        for (Path fileOrDirectoryPath : stream) {
            if (Files.isDirectory(fileOrDirectoryPath) && "target".equals(fileOrDirectoryPath.getFileName().toString())) {
                continue;
            }
            if (Files.isDirectory(fileOrDirectoryPath) && "src".equals(fileOrDirectoryPath.getFileName().toString())) {
                srcDirectory = fileOrDirectoryPath;
                continue;
            }
            if (Files.isRegularFile(fileOrDirectoryPath) && "pom.xml".equals(fileOrDirectoryPath.getFileName().toString())) {
                pomXmlFileFound = true;
                continue;
            }
        }
        stream.close();
        if (! (srcDirectory != null && pomXmlFileFound)) {
            return false;
        }

        return containsJavaSourceCode(srcDirectory);
    }

    /**
     * This method searches a directory and all sub directories for a java source code file. If found, returns true
     * otherwise returns false.
     *
     * @param path is a Path object.
     * @return true if a java source code was found, otherwise false.
     */
    boolean containsJavaSourceCode(Path path) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path fileOrDirectoryPath : stream) {
            String filename = fileOrDirectoryPath.getFileName().toString();
            if (Files.isRegularFile(fileOrDirectoryPath) && filename.endsWith(".java")) {
                return true;
            }
            if (Files.isDirectory(fileOrDirectoryPath)) {
                if (containsJavaSourceCode(fileOrDirectoryPath)) {
                    return true;
                }
            }
        }
        return false;
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
            MavenProject mp = new MavenProjectImpl(path, javaTypeDeclarationPaths);
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

    /**
     * Transform a Path to a Reader and invokes the method parseJavaFile, which returns a list of
     * JavaTypeDeclarationPath.
     *
     * @param path is the path to the java source file
     * @return a list of JavaTypeDeclarationPath.
     * @throws IOException
     */
    List<JavaTypeDeclarationPath> readJavaFile(Path path) throws IOException {
        BufferedReader reader = Files.newBufferedReader(path);
        List<JavaTypeDeclarationPath> javaTypeDeclarationPaths = parseJavaFile(reader, path);
        return javaTypeDeclarationPaths;
    }

    /**
     * Parse a java souece file into a list of JavaTypeDeclarationPath.
     *
     * TODO: What to do with inner classes, local classes, annonymous classes and static inner classes.
     *
     * @param reader is a Reader to a java source file.
     * @param path is the Path object aimong at the java source file.
     * @return a list of JavaTypeDeclarationPath.
     */
    List<JavaTypeDeclarationPath> parseJavaFile(Reader reader, Path path) {
        CompilationUnit compilationUnit = JavaParser.parse(reader);
        String packageName = "";
        if (compilationUnit.getPackageDeclaration().isPresent()) {
            PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
            packageName = packageDeclaration.getNameAsString();
        }
        NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
        List<JavaTypeDeclarationPath> javaTypeDeclarationPaths = new LinkedList<>();
        for (TypeDeclaration<?> typeDeclaration : types) {
            EnumSet<Modifier> modifiers = typeDeclaration.getModifiers();
            AccessModifier accessModifier = AccessModifier.PACKAGE;
            for (Modifier modifier : modifiers) {
                switch (modifier) {
                    case PUBLIC:
                        accessModifier = AccessModifier.PUBLIC;
                        break;
                    case PROTECTED:
                        accessModifier = AccessModifier.PROTECTED;
                        break;
                    case PRIVATE:
                        accessModifier = AccessModifier.PRIVATE;
                        break;
                }
            }
            String name = "";
            JavaType javaType = JavaType.CLASS;
            if (typeDeclaration instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classOrInterfaceDecl = (ClassOrInterfaceDeclaration) typeDeclaration;
                name = classOrInterfaceDecl.getNameAsString();
                if (classOrInterfaceDecl.isInterface()) {
                    javaType = JavaType.INTERFACE;
                }

            }
            else if (typeDeclaration instanceof EnumDeclaration) {
                EnumDeclaration enumDeclaration = (EnumDeclaration) typeDeclaration;
                name = enumDeclaration.getNameAsString();
                javaType = JavaType.ENUM;
            }
            javaTypeDeclarationPaths.add(new JavaTypeDeclarationPathImpl.JavaTypeDeclarationPathBuilder().setName(name).
                    setPackageName(packageName).setAccessModifier(accessModifier).setJavaType(javaType).
                    setPathToTypeDeclaration(path).build());
        }
        return javaTypeDeclarationPaths;
    }

    //
    // -------------------------------------------------------------------------------------------------------------
    //

    
}
