package langexp.lex

class Token
{
    enum Type {
        UNKNOWN,
        EOF,
        WHITESPACE,
        NEWLINE,
        SYMBOL,
        OPERATOR,
        NUMBER,
        STRING,
        COMMENT
    }

    Type type = Type.UNKNOWN
    StringBuilder matched = new StringBuilder()
    String firstMatched

    String toString() {
        "$type($matched)"
    }

    void appendToMatched(def s) {
        matched.append(s)
        if (!firstMatched) {
            firstMatched = s
        }
    }
}
