package org.teinelund.javacodevisualizer.dom;

import com.github.javaparser.ast.body.TypeDeclaration;

import java.nio.file.Path;
import java.util.List;

public interface JavaTypeDeclarationPath {
    public String getName();
    public String getPackageName();
    public Path getPathToTypeDeclaration();
    public JavaType getJavaType();
    public AccessModifier getAccessModifier();
    public TypeDeclaration<?> getTypeDeclaration();
    public void addField(String s, JavaTypeDeclarationPath fieldClass);
    public List<Field> getFields();
}
