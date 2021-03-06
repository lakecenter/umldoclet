/*
 * Copyright 2016-2018 Talsma ICT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.talsmasoftware.umldoclet.uml;

import nl.talsmasoftware.umldoclet.configuration.TypeDisplay;
import nl.talsmasoftware.umldoclet.rendering.indent.IndentingPrintWriter;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class Type extends UMLPart implements Comparable<Type> {
    /**
     * Classification of a UML Type.
     *
     * @author Sjoerd Talsma
     */
    public enum Classification {
        ENUM, INTERFACE, ANNOTATION, ABSTRACT_CLASS, CLASS;

        public String toUml() {
            return name().toLowerCase().replace('_', ' ');
        }
    }

    private final Namespace namespace;
    private final Classification classfication;
    public final TypeName name;
    private final boolean isDeprecated, addPackageToName;
    private Link link;

    public Type(Namespace namespace, Classification classification, TypeName name) {
        this(namespace, classification, name, false, false, null);
    }

    private Type(Namespace namespace, Classification classification, TypeName name, boolean isDeprecated,
                 boolean addPackageToName, Collection<? extends UMLPart> children) {
        super(namespace);
        this.namespace = requireNonNull(namespace, "Containing package is <null>.");
        this.classfication = requireNonNull(classification, "Type classification is <null>.");
        this.name = requireNonNull(name, "Type name is <null>.");
        this.isDeprecated = isDeprecated;
        this.addPackageToName = addPackageToName;
        if (children != null) children.forEach(this::addChild);
    }

    private Link link() {
        if (link == null) link = Link.toType(this);
        return link;
    }

    public Type deprecated() {
        return new Type(getNamespace(), classfication, name, true, addPackageToName, getChildren());
    }

    public Type addPackageToName() {
        return new Type(getNamespace(), classfication, name, isDeprecated, true, getChildren());
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public Classification getClassfication() {
        return classfication;
    }

    @Override
    void setParent(UMLPart parent) {
        super.setParent(parent);
        if (namespace.getParent() == null) namespace.setParent(parent);
    }

    private <IPW extends IndentingPrintWriter> IPW writeNameTo(IPW output, Namespace namespace) {
        if (addPackageToName && name.qualified.startsWith(this.namespace.name + '.')) {
            String nameInPackage = name.qualified.substring(this.namespace.name.length() + 1);
            output.append("\"<size:14>").append(nameInPackage)
                    .append("\\n<size:10>").append(this.namespace.name)
                    .append("\" as ");
        }
        output.append(name.toUml(TypeDisplay.QUALIFIED, namespace));
        return output;
    }

    @Override
    public <IPW extends IndentingPrintWriter> IPW writeTo(IPW output) {
        // Namespace aware compensation
        final Namespace namespace = getParent() instanceof PackageUml
                ? new Namespace(getRootUMLPart(), ((PackageUml) getParent()).packageName) : null;
        output.append(classfication.toUml()).whitespace();
        writeNameTo(output, namespace).whitespace();
        if (isDeprecated) output.append("<<deprecated>>").whitespace();
        link().writeTo(output).whitespace();
        writeChildrenTo(output).newline();
        return output;
    }

    @Override
    public <IPW extends IndentingPrintWriter> IPW writeChildrenTo(IPW output) {
        if (!getChildren().isEmpty()) super.writeChildrenTo(output.append('{').newline()).append('}');
        return output;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Type other) {
        return name.compareTo(requireNonNull(other, "Cannot compare Type to <null>.").name);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Type && this.compareTo((Type) other) == 0);
    }

}
