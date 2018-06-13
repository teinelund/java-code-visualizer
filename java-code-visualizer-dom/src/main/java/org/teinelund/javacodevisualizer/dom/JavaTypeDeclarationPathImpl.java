package org.teinelund.javacodevisualizer.dom;

import com.github.javaparser.ast.body.TypeDeclaration;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class JavaTypeDeclarationPathImpl implements JavaTypeDeclarationPath {

    private String name;
    private String packageName;
    private Path pathToTypeDeclaration;
    private JavaType javaType;
    private AccessModifier accessModifier;
    private TypeDeclaration<?> typeDeclaration;
    private List<Field> fields;

    private JavaTypeDeclarationPathImpl( JavaTypeDeclarationPathBuilder javaTypeDeclarationPathBuilder) {
        this.name = javaTypeDeclarationPathBuilder.name;
        this.packageName = javaTypeDeclarationPathBuilder.packageName;
        this.pathToTypeDeclaration = javaTypeDeclarationPathBuilder.pathToTypeDeclaration;
        this.javaType = javaTypeDeclarationPathBuilder.javaType;
        this.accessModifier = javaTypeDeclarationPathBuilder.accessModifier;
        this.typeDeclaration = javaTypeDeclarationPathBuilder.typeDeclaration;
        this.fields = new LinkedList<>();
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

    @Override
    public TypeDeclaration<?> getTypeDeclaration() {
        return this.typeDeclaration;
    }

    @Override
    public void addField(String fieldName, JavaTypeDeclarationPath fieldClass) {
        this.fields.add(FieldImpl.builder().setName(fieldName).setType(fieldClass).build());
    }

    @Override
    public List<Field> getFields() {
        return Collections.unmodifiableList(this.fields);
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
        private TypeDeclaration<?> typeDeclaration;


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
    }
}