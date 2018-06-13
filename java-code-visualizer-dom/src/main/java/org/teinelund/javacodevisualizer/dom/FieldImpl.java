package org.teinelund.javacodevisualizer.dom;

class FieldImpl implements Field {

    private String name;
    private JavaTypeDeclarationPath type;

    FieldImpl(FieldBuilder builder) {
        this.name = builder.name;
        this.type = builder.type;
    }

    public String getName() {
        return this.name;
    }
    public JavaTypeDeclarationPath getType() {
        return this.type;
    }

    public static FieldImpl.FieldBuilder builder() {
        return new FieldImpl.FieldBuilder();
    }

    public static class FieldBuilder {
        private String name;
        private JavaTypeDeclarationPath type;


        public FieldImpl.FieldBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public FieldImpl.FieldBuilder setType(JavaTypeDeclarationPath type) {
            this.type = type;
            return this;
        }

        public Field build() {
            return new FieldImpl( this );
        }
    }
}
