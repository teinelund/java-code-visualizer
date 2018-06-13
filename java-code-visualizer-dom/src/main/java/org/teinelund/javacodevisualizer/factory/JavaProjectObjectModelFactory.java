package org.teinelund.javacodevisualizer.factory;

import org.teinelund.javacodevisualizer.dom.JavaProjectObjectModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface JavaProjectObjectModelFactory {

    /**
     * Reads all java classes that are found in the path list, javaProjectPaths, create a JavaProjectObjectModel and
     * store (serialize) that object in file storagePath.
     *
     * @param javaProjectPaths is a list of Path objects containing Maven projects.
     * @param storagePath is a Path object pointing to the file to serialize the JavaProjectObjectModel.
     * @return
     */
    JavaProjectObjectModel createrAndStore(List<Path> javaProjectPaths, List<Path> excludePaths, Path storagePath) throws IOException;

    /**
     * Reads (deserialize) the file given by the Path storagePath to a JavaProjectObjectModel.
     *
     * @param storagePath is a Path object pointing to the file to deserialize to a JavaProjectObjectModel.
     * @return
     */
    JavaProjectObjectModel loadAndCreate(Path storagePath);
}
