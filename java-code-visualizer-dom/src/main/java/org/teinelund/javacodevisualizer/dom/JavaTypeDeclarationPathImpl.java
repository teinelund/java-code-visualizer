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

    public JavaTypeDeclarationPathImpl( JavaTypeDeclarationPathBuilder javaTypeDeclarationPathBuilder) {
        this.name = javaTypeDeclarationPathBuilder.getName();
        this.packageName = javaTypeDeclarationPathBuilder.getPackageName();
        this.pathToTypeDeclaration = javaTypeDeclarationPathBuilder.getPathToTypeDeclaration();
        this.javaType = javaTypeDeclarationPathBuilder.getJavaType();
        this.accessModifier = javaTypeDeclarationPathBuilder.getAccessModifier();
        this.typeDeclaration = javaTypeDeclarationPathBuilder.getTypeDeclaration();
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


}