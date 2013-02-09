package langexp.interpreter

import org.apache.commons.lang3.builder.ToStringBuilder

class Variable {
    enum Type {
        SYMBOL
    }

    String name
    Type type
    def value

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
            append("name", name).
            append("type", type).
            append("value", value).
            toString()
    }
}
