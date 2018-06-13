package org.teinelund.javacodevisualizer.dom;

import java.nio.file.Path;
import java.util.List;

public interface IDomainObjectModelFactory {
    public JavaProjectObjectModel createJavaProjectObjectModel();
    public MavenProject createMavenProject(Path mavenProjectPath, List<JavaTypeDeclarationPath> javaTypeDeclarationPaths);
}
