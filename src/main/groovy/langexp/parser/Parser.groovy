package langexp.parser

import langexp.lex.Token
import static langexp.lex.Token.Type.*
import langexp.lex.Tokeniser

class Parser
{
    Tokeniser tokeniser
    Token currentToken
    Ast ast
    def errors, symbolTable = ['print': new Symbol(type: Symbol.Type.FUNCTION)]

    /*
        PARSE -> (STATEMENT EOL)* EOF
     */
    Ast parse() {
        errors = []
        ast = new Ast()
        nextToken()
        while (currentToken.type != EOF) {
            statement()
            if (currentToken.type != NEWLINE && currentToken.type != EOF) {
                errors << tokeniser.errorMessage("Expected EOL or EOF after parsing a statement, instead got $currentToken")
            } else if (currentToken.type == NEWLINE) {
                nextToken()
            }
        }
        ast
    }

    private Token nextToken()
    {
        currentToken = tokeniser.nextToken()
    }

    /*
        STATEMENT -> [COMMENT | EXPRESSION] (EOF | EOL)
     */
    void statement() {
        if (currentToken.type != NEWLINE && currentToken.type != EOF) {
            if (currentToken.type == COMMENT) {
                ast.root.children << new AstNode(value: currentToken, type: AstNode.NodeType.COMMENT)
                nextToken()
            } else {
                ast.root.children << expression()
            }
        }
    }

    /*
        EXPRESSION -> FUNCTION_CALL | STRING | SYMBOL
     */
    AstNode expression() {
        if (currentToken.type == SYMBOL && symbolTable.containsKey(currentToken.matched.toString())) {
            functionCall()
        } else if (currentToken.type == STRING) {
            def node = new AstNode(value: currentToken, type: AstNode.NodeType.STRING)
            nextToken()
            node
        } else if (currentToken.type == SYMBOL) {
            def node = new AstNode(value: currentToken, type: AstNode.NodeType.SYMBOL)
            nextToken()
            node
        } else {
            errors << tokeniser.errorMessage("Expected the start of an expression, instead got $currentToken")
            null
        }
    }

    /*
        FUNCTION_CALL -> SYMBOL [WS] PARAMLIST
     */
    AstNode functionCall() {
        if (currentToken.type == SYMBOL && symbolTable.containsKey(currentToken.matched.toString())) {
            AstNode node = new AstNode(value: currentToken, type: AstNode.NodeType.FUNCTION)
            nextToken()
            if (currentToken.type == WHITESPACE) {
                nextToken()
            }
            paramlist(node)
            node
        }
    }

    /*
        PARAMLIST -> EXPRESSION ([WS] EXPRESSION)* (EOF | EOL)
     */
    void paramlist(AstNode node) {
        while (currentToken.type != NEWLINE && currentToken.type != EOF) {
            node.children << expression()
            if (currentToken.type == WHITESPACE) {
                nextToken()
            }
        }
    }
}
