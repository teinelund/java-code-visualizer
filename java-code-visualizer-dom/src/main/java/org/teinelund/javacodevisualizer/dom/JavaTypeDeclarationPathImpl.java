package org.teinelund.javacodevisualizer.dom;

import java.nio.file.Path;

class JavaTypeDeclarationPathImpl implements JavaTypeDeclarationPath {

    private String name;
    private String packageName;
    private Path pathToTypeDeclaration;
    private JavaType javaType;
    private AccessModifier accessModifier;

    private JavaTypeDeclarationPathImpl( JavaTypeDeclarationPathBuilder javaTypeDeclarationPathBuilder) {
        this.name = javaTypeDeclarationPathBuilder.name;
        this.packageName = javaTypeDeclarationPathBuilder.packageName;
        this.pathToTypeDeclaration = javaTypeDeclarationPathBuilder.pathToTypeDeclaration;
        this.javaType = javaTypeDeclarationPathBuilder.javaType;
        this.accessModifier = javaTypeDeclarationPathBuilder.accessModifier;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPackageName() {
        return this.packageName;
    }

    @Override
    public Path getPathToTypeDeclaration() {
        return this.pathToTypeDeclaration;
    }

    @Override
    public JavaType getJavaType() {
        return this.javaType;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return this.accessModifier;
    }

    public static JavaTypeDeclarationPathBuilder builder() {
        return new JavaTypeDeclarationPathBuilder();
    }

    public static class JavaTypeDeclarationPathBuilder {
        private String name;
        private String packageName;
        private Path pathToTypeDeclaration;
        private JavaType javaType;
        private AccessModifier accessModifier;


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

        public JavaTypeDeclarationPath build() {
            return new JavaTypeDeclarationPathImpl( this );
        }
    }
}
