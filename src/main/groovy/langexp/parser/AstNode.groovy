package langexp.parser

import langexp.lex.Token
import org.apache.commons.lang3.builder.ToStringBuilder

class AstNode
{
    enum NodeType {
        FUNCTION,
        STRING,
        SYMBOL,
        COMMENT
    }

    Token value
    NodeType type
    List<AstNode> children = []

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
            append("value", value).
            append("type", type).
            toString();
    }

    boolean isLast(int i)
    {
        return i == children.size() - 1
    }
}
