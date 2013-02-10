package langexp.interpreter

import langexp.lex.Token
import langexp.parser.Ast
import langexp.parser.AstNode
import static langexp.parser.AstNode.NodeType.*

class AstInterpreter
{
    Ast ast
    Map<String, Variable> symbolTable = [
        'print': null
    ]

    def execute() {
        evaluate(ast.root)
    }

    def evaluate(AstNode astNode) {
        def result = null
        def results = astNode.children.collect() { evaluate(it) }
        switch (astNode.type) {
            case FUNCTION:
                result = executeFunction(astNode.value, results)
                break
            case STRING:
                result = astNode.value.tokenValue()
                break
            case SYMBOL:
                result = evaluateSymbol(astNode.value)
                break
        }
        result
    }

    def evaluateSymbol(Token symbol)
    {
        def symbolName = symbol.tokenValue()
        if (!symbolTable.containsKey(symbolName)) {
            symbolTable[symbolName] = new Variable(name: symbolName, type: Variable.Type.SYMBOL, value: symbolName)
        }
        symbolTable[symbolName].value
    }

    static def executeFunction(Token symbol, List params) {
        def result = null
        switch(symbol.tokenValue()) {
            case 'print':
                result = functionPrint(params)
                break
            default:
                throw new Exception("Unknown function ${symbol.tokenValue()}")
        }
        result
    }

    static def functionPrint(List params)
    {
        String printStr = params.join(' ')
        println(printStr)
        printStr
    }

    void printSymbolTable()
    {
        symbolTable.each {
            println it
        }
    }
}
