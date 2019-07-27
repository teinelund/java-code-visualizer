package org.teinelund.javacodevisualizer.factory;

import org.teinelund.javacodevisualizer.dom.DomainObjectModelFactory;
import org.teinelund.javacodevisualizer.dom.JavaTypeDeclarationPath;
import org.teinelund.javacodevisualizer.dom.MavenProject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class FetchMavenProject {

    private static FetchMavenProject fetchMavenProject;

    FetchMavenProject() {}

    public static FetchMavenProject instance() {
        if (fetchMavenProject == null) {
            fetchMavenProject = new FetchMavenProject();
        }
        return fetchMavenProject;
    }

    /**
     * TODO: Instead of a list of Path, the input could be a list of MavanProject objects, containing
     * a Path object to the "src"-folder.
     *
     * @param mavenProjectPaths
     * @return
     * @throws IOException
     */
    List<MavenProject> getMavenProjects(List<Path> mavenProjectPaths) throws IOException {
        List<MavenProject> mavenProjects = new LinkedList<>();
        for (Path path : mavenProjectPaths) {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            Path srcDirectory = null;
            for (Path fileOrDirectoryPath : stream) {
                if (Files.isDirectory(fileOrDirectoryPath) && "src".equals(fileOrDirectoryPath.getFileName().toString())) {
                    srcDirectory = fileOrDirectoryPath;
                    break;
                }
            }
            List<JavaTypeDeclarationPath> javaTypeDeclarationPaths = findJavaClassPathsInSrcDirectory(srcDirectory);
            MavenProject mp = DomainObjectModelFactory.instance().createMavenProject(path, javaTypeDeclarationPaths);
            mavenProjects.add(mp);
        }
        return mavenProjects;
    }



    List<JavaTypeDeclarationPath> findJavaClassPathsInSrcDirectory(Path path) throws IOException {
        List<JavaTypeDeclarationPath> javaTypeDeclarationPaths = new LinkedList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path fileOrDirectoryPath : stream) {
            if (Files.isDirectory(fileOrDirectoryPath)) {
                javaTypeDeclarationPaths.addAll(findJavaClassPathsInSrcDirectory(fileOrDirectoryPath));
            }
            String filename = fileOrDirectoryPath.getFileName().toString();
            if (Files.isRegularFile(fileOrDirectoryPath) && filename.endsWith(".java")) {
                javaTypeDeclarationPaths.addAll(readJavaFile(fileOrDirectoryPath));
            }
        }
        return javaTypeDeclarationPaths;
    }

    List<JavaTypeDeclarationPath> readJavaFile(Path path) throws IOException {
        return JavaSourceFileParser.instance().readJavaFile(path);
    }
}
