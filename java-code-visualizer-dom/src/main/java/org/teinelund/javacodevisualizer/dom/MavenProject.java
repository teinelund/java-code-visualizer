package org.teinelund.javacodevisualizer.dom;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface MavenProject {
    public List<JavaTypeDeclarationPath> getAllTypesGivenName(String name);
    public List<JavaTypeDeclarationPath> getAllTypesGivenPackageName(String packageName);
    public Collection<String> getAllTypeNames();
    public Collection<String> getAllPackageNames();
    public Path getMavenProjectPath();
    public List<JavaTypeDeclarationPath> getAllTypes();
}
