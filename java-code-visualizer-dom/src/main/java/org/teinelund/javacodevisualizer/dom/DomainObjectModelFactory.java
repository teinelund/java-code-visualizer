package org.teinelund.javacodevisualizer.dom;

import java.nio.file.Path;
import java.util.List;

public class DomainObjectModelFactory implements IDomainObjectModelFactory {

    private static IDomainObjectModelFactory IDomainObjectModelFactory;

    private DomainObjectModelFactory() {}

    public static IDomainObjectModelFactory instnace() {
        if (IDomainObjectModelFactory == null) {
            IDomainObjectModelFactory = new DomainObjectModelFactory();
        }
        return IDomainObjectModelFactory;
    }

    @Override
    public JavaProjectObjectModel createJavaProjectObjectModel() {
        return new JavaProjectObjectModelImpl();
    }

    @Override
    public MavenProject createMavenProject(Path mavenProjectPath, List<JavaTypeDeclarationPath> javaTypeDeclarationPaths) {
        return new MavenProjectImpl(mavenProjectPath, javaTypeDeclarationPaths);
    }
}
