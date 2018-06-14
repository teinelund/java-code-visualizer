package org.teinelund.javacodevisualizer.factory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.teinelund.javacodevisualizer.dom.AccessModifier;
import org.teinelund.javacodevisualizer.dom.JavaType;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPathBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class JavaSourceFileParser {

    private static JavaSourceFileParser javaSourceFileParser = null;

    private JavaSourceFileParser() {}

    public static JavaSourceFileParser instance() {
        if (javaSourceFileParser == null) {
            javaSourceFileParser = new JavaSourceFileParser();
        }
        return javaSourceFileParser;
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
            javaTypeDeclarationPaths.add(JavaTypeDeclarationPathBuilder.builder().setName(name).
                    setPackageName(packageName).setAccessModifier(accessModifier).setJavaType(javaType).
                    setPathToTypeDeclaration(path).setTypeDeclaration(typeDeclaration).build());
        }
        return javaTypeDeclarationPaths;
    }
}
