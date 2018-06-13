package org.teinelund.javacodevisualizer.dom;

import java.util.Collection;
import java.util.List;

public interface JavaProjectObjectModel {
    public void addJavaTypeDeclarationPaths(List<JavaTypeDeclarationPath> javaTypeDeclarationPaths);
    public List<JavaTypeDeclarationPath> getAllTypesGivenName(String name);
    public List<JavaTypeDeclarationPath> getAllTypesGivenPackageName(String packageName);
    public Collection<String> getAllTypeNames();
    public Collection<String> getAllPackageNames();
}
