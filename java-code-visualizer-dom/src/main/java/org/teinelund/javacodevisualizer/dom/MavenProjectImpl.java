package org.teinelund.javacodevisualizer.dom;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MavenProjectImpl implements MavenProject {

    /**
     * Name is not guarenteed to be unique. It is legal to have types with same name but different package name.
     */
    Map<String, List<JavaTypeDeclarationPath>> typeNameMap;

    /**
     * Store types by package names.
     */
    Map<String, List<JavaTypeDeclarationPath>> packageNameToTypeListMap;

    Path mavenProjectPath;

    public MavenProjectImpl(Path mavenProjectPath, List<JavaTypeDeclarationPath> javaTypeDeclarationPaths) {
        typeNameMap = new HashMap<>();
        packageNameToTypeListMap = new HashMap<>();
        for (JavaTypeDeclarationPath jtd : javaTypeDeclarationPaths) {
            if (typeNameMap.containsKey(jtd.getName())) {
                List<JavaTypeDeclarationPath> list = typeNameMap.get(jtd.getName());
                list.add(jtd);

            }
            else {
                List<JavaTypeDeclarationPath> list = new LinkedList<>();
                list.add(jtd);
                typeNameMap.put(jtd.getName(), list);
            }
            if (packageNameToTypeListMap.containsKey(jtd.getPackageName())) {
                List<JavaTypeDeclarationPath> list = packageNameToTypeListMap.get(jtd.getPackageName());
                list.add(jtd);
            }
            else {
                List<JavaTypeDeclarationPath> list = new LinkedList<>();
                list.add(jtd);
                packageNameToTypeListMap.put(jtd.getPackageName(), list);
            }
        }
        this.mavenProjectPath = mavenProjectPath;
    }

    @Override
    public List<JavaTypeDeclarationPath> getAllTypesGivenName(String name) {
        return Collections.unmodifiableList(this.typeNameMap.get(name));
    }

    @Override
    public List<JavaTypeDeclarationPath> getAllTypesGivenPackageName(String packageName) {
        return Collections.unmodifiableList(this.packageNameToTypeListMap.get(packageName));
    }

    @Override
    public Collection<String> getAllTypeNames() {
        return Collections.unmodifiableCollection(this.typeNameMap.keySet());
    }

    @Override
    public Collection<String> getAllPackageNames() {
        return Collections.unmodifiableCollection(this.packageNameToTypeListMap.keySet());
    }

    @Override
    public Path getMavenProjectPath() {
        return this.mavenProjectPath;
    }
}
