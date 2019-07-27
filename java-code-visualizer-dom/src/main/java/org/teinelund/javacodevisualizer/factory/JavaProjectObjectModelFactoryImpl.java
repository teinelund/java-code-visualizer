package org.teinelund.javacodevisualizer.factory;

import org.teinelund.javacodevisualizer.dom.DomainObjectModelFactory;
import org.teinelund.javacodevisualizer.dom.JavaProjectObjectModel;
import org.teinelund.javacodevisualizer.dom.MavenProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavaProjectObjectModelFactoryImpl implements JavaProjectObjectModelFactory {


    @Override
    public JavaProjectObjectModel createrAndStore(List<Path> javaProjectPaths, List<Path> excludePaths, Path storagePath) throws IOException {
        List<Path> mavenProjectPaths = MavenProjectPath.instance().getMavenProjectPaths(javaProjectPaths, excludePaths);
        List<MavenProject> mavenProjects = FetchMavenProject.instance().getMavenProjects(mavenProjectPaths);
        JavaProjectObjectModel jpom = DomainObjectModelFactory.instance().createJavaProjectObjectModel();
        WireClassField.instance().wireClasses(jpom, mavenProjects);
        WireClassField.instance().wireClassFields(jpom);
        return jpom;
    }

    @Override
    public JavaProjectObjectModel loadAndCreate(Path storagePath) {
        return null;
    }

}
