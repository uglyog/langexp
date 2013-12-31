package langexp.interpreter

import langexp.lex.Token
import langexp.parser.Ast
import langexp.parser.AstNode
import langexp.parser.Parser
import langexp.parser.Symbol
import langexp.util.Table

import static langexp.parser.AstNode.NodeType.*

class AstInterpreter {
  Ast ast
  Parser parser
  def symbolTable

  def execute() {
    setupSymbolTable()
    evaluate(ast.root)
  }

  def evaluate(AstNode astNode) {
    def result = null
    def results = astNode.children.collect() { evaluate(it) }
    switch (astNode.type) {
      case SEQUENCE:
        result = handleSequence(astNode, results)
        break
      case STRING:
        result = astNode.value.tokenValue()
        break
      case SYMBOL:
        result = evaluateSymbol(astNode.value)
        break
      case COMMENT:
        result = VOID
        break
      default:
        result = results.join()
    }
    result
  }

  static def handleSequence(AstNode astNode, List results) {
    def result
    switch (astNode.subType) {
      case EXPRESSION:
        if (results[0].type.type == Symbol.Type.FUNCTION) {
          result = executeFunction(results[0].value, results.tail())
        } else {
          result = results
        }
        break
      default:
        result = results - VOID
    }
    result
  }

  def evaluateSymbol(Token symbol) {
    def symbolName = symbol.tokenValue()
    if (!symbolTable.containsKey(symbolName)) {
      symbolTable[symbolName] = new Variable(type: new Symbol(type: Symbol.Type.SYMBOL), value: symbolName)
    }
    symbolTable[symbolName]
  }

  static def executeFunction(def symbol, List params) {
    def result = null
    switch (symbol as String) {
      case 'print':
        result = functionPrint(params)
        break
      default:
        throw new Exception("Unknown function $symbol")
    }
    result
  }

  static String functionPrint(List params) {
    String printStr = params.collect {
      if (it instanceof Variable) {
        it.value
      } else {
        it
      }
    }.join(' ')
    println(printStr)
    printStr
  }

  void printSymbolTable() {
    def table = new Table().addColumn('Symbol', 20).addColumn('Type', 20).addColumn('Value', 60)
    symbolTable.each {
      table.addRow([it.key, it.value.type, it.value.value])
    }
    table.print()
  }

  void dumpData() {
    println "Symbol Table:"
    printSymbolTable()
  }

  void setupSymbolTable() {
    symbolTable = new HashMap(parser.symbolTable.collect {
      [it.key, new Variable(type: it.value, value: it.key)]
    }.flatten().toSpreadMap())
  }
}
