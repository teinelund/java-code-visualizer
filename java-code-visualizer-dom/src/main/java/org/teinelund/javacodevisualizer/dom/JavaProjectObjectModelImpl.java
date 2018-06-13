package org.teinelund.javacodevisualizer.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class JavaProjectObjectModelImpl implements JavaProjectObjectModel {

    /**
     * Name is not guarenteed to be unique. It is legal to have types with same name but different package name.
     */
    Map<String, List<JavaTypeDeclarationPath>> typeNameMap;

    /**
     * Store types by package names.
     */
    Map<String, List<JavaTypeDeclarationPath>> packageNameToTypeListMap;

    public JavaProjectObjectModelImpl() {
        typeNameMap = new HashMap<>();
        packageNameToTypeListMap = new HashMap<>();
    }

    @Override
    public void addJavaTypeDeclarationPaths(List<JavaTypeDeclarationPath> javaTypeDeclarationPaths) {
        for (JavaTypeDeclarationPath jtdp : javaTypeDeclarationPaths) {
            if (typeNameMap.containsKey(jtdp.getName())) {
                List<JavaTypeDeclarationPath> list = typeNameMap.get(jtdp.getName());
                list.add(jtdp);

            }
            else {
                List<JavaTypeDeclarationPath> list = new LinkedList<>();
                list.add(jtdp);
                typeNameMap.put(jtdp.getName(), list);
            }
            if (packageNameToTypeListMap.containsKey(jtdp.getPackageName())) {
                List<JavaTypeDeclarationPath> list = packageNameToTypeListMap.get(jtdp.getPackageName());
                list.add(jtdp);
            }
            else {
                List<JavaTypeDeclarationPath> list = new LinkedList<>();
                list.add(jtdp);
                packageNameToTypeListMap.put(jtdp.getPackageName(), list);
            }
        }
    }

    @Override
    public List<JavaTypeDeclarationPath> getAllTypesGivenName(String name) {
        List<JavaTypeDeclarationPath> list = this.typeNameMap.get(name);
        if (list != null) {
            return Collections.unmodifiableList(list);
        }
        return new LinkedList<JavaTypeDeclarationPath>();
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
}
