package org.teinelund.javacodevisualizer.dom;

import com.github.javaparser.ast.body.TypeDeclaration;

import java.nio.file.Path;

public class JavaTypeDeclarationPathBuilder {

    private String name;
    private String packageName;
    private Path pathToTypeDeclaration;
    private JavaType javaType;
    private AccessModifier accessModifier;
    private TypeDeclaration<?> typeDeclaration;

    public static JavaTypeDeclarationPathBuilder builder() {
        return new JavaTypeDeclarationPathBuilder();
    }


    public JavaTypeDeclarationPathBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public JavaTypeDeclarationPathBuilder setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public JavaTypeDeclarationPathBuilder setPathToTypeDeclaration(Path pathToTypeDeclaration) {
        this.pathToTypeDeclaration = pathToTypeDeclaration;
        return this;
    }

    public JavaTypeDeclarationPathBuilder setJavaType(JavaType javaType) {
        this.javaType = javaType;
        return this;
    }

    public JavaTypeDeclarationPathBuilder setAccessModifier(AccessModifier accessModifier) {
        this.accessModifier = accessModifier;
        return this;
    }

    public JavaTypeDeclarationPathBuilder setTypeDeclaration(TypeDeclaration<?> typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
        return this;
    }

    public JavaTypeDeclarationPath build() {
        return new JavaTypeDeclarationPathImpl( this );
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Path getPathToTypeDeclaration() {
        return pathToTypeDeclaration;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public TypeDeclaration<?> getTypeDeclaration() {
        return typeDeclaration;
    }
}
