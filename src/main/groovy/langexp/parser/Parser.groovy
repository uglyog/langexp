package langexp.parser

import langexp.lex.Token
import static langexp.lex.Token.Type.*
import langexp.lex.Tokeniser

class Parser {
  Tokeniser tokeniser
  Token currentToken
  Ast ast
  def errors, symbolTable = ['print': new Symbol(type: Symbol.Type.FUNCTION)]

  private Token nextToken() {
    currentToken = tokeniser.nextToken()
  }

  /*
      PARSE -> SEQUENCE<STATEMENTS>* EOF
   */
  Ast parse() {
    errors = []
    ast = new Ast()

    nextToken()

    def statements = sequenceOfStatements()
    if (statements) {
      ast.root = statements
    }

    if (currentToken.type != EOF) {
      errors << tokeniser.errorMessage("Expected EOF, instead got $currentToken")
    }

    ast
  }

  /*
    SEQUENCE<STATEMENTS> -> STATEMENT ([EOL] STATEMENT)*
   */
  AstNode sequenceOfStatements() {
    AstNode sequence = null
    while (currentToken.type != EOF && matchStartOfStatement()) {
      AstNode statement = statement()
      if (!sequence) {
        sequence = new AstNode(type: AstNode.NodeType.SEQUENCE, subType: AstNode.NodeType.STATEMENT)
      }
      sequence.children << statement
      if (currentToken.type == NEWLINE) {
        nextToken()
      }
    }
    sequence
  }

  /*
      STATEMENT -> [COMMENT | SEQUENCE<EXPRESSIONS>]
   */
  AstNode statement() {
    AstNode node = null
    if (currentToken.type != NEWLINE && currentToken.type != EOF) {
      if (currentToken.type == COMMENT) {
        node = new AstNode(value: currentToken, type: AstNode.NodeType.COMMENT)
        nextToken()
      } else {
        node = sequenceOfExpressions()
      }
    }
    node
  }

  boolean matchStartOfStatement() {
    currentToken.type == COMMENT || currentToken.type == STRING || currentToken.type == SYMBOL
  }

  /*
    SEQUENCE<EXPRESSIONS> -> EXPRESSION (WS EXPRESSION)*
   */
  AstNode sequenceOfExpressions() {
    def node = null
    if (currentToken.type != NEWLINE && currentToken.type != EOF) {
      node = new AstNode(type: AstNode.NodeType.SEQUENCE, subType: AstNode.NodeType.EXPRESSION)
      while (currentToken.type != NEWLINE && currentToken.type != EOF) {
        node.children << expression()
        if (currentToken.type == WHITESPACE) {
          nextToken()
        }
      }
    }
    node
  }

  /*
      EXPRESSION ->  STRING | SYMBOL
   */
  AstNode expression() {
    def node
    if (currentToken.type == STRING) {
      node = new AstNode(value: currentToken, type: AstNode.NodeType.STRING)
      nextToken()
    } else if (currentToken.type == SYMBOL) {
      node = new AstNode(value: currentToken, type: AstNode.NodeType.SYMBOL)
      nextToken()
    } else {
      errors << tokeniser.errorMessage("Expected the start of an expression, instead got $currentToken")
    }
    node
  }
}
