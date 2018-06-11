package org.teinelund.javacodevisualizer.dom;

import java.nio.file.Path;

public interface JavaTypeDeclarationPath {
    public String getName();
    public String getPackageName();
    public Path getPathToTypeDeclaration();
    public JavaType getJavaType();
    public AccessModifier getAccessModifier();
}
