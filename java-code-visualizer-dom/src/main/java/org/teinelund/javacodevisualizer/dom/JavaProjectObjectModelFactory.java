package org.teinelund.javacodevisualizer.dom;

import java.nio.file.Path;
import java.util.List;

public interface JavaProjectObjectModelFactory {
    JavaProjectObjectModel createrAndStore(List<Path> javaProjectPaths, Path storagePath);
    JavaProjectObjectModel create(Path storagePath);
}
