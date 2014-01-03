package langexp.compiler

import langexp.parser.Ast
import langexp.parser.AstNode
import langexp.parser.Parser
import langexp.parser.Symbol

import static langexp.parser.AstNode.NodeType.*
import org.apache.commons.io.FilenameUtils

class StackVmCompiler {
  static final int VERSION = 0

  enum DataType {
    POINTER,
    INT32,
    STRING_DATA,
    SYMBOL_DATA
  }

  enum InstructionCode {
    PUSHADDR,
    PUSHVAL,
    CALLFUNC
  }

  Ast ast
  String file
  String inputFile
  def symbolTable = [:]
  def instructions = []
  def data = []
  boolean verbose = false
  Parser parser

  void compile() {
    file = file ?: (FilenameUtils.removeExtension(inputFile) + '.sbvm')
    if (verbose) {
      println("Compiling to $file")
    }
    new File(file).withPrintWriter { pw ->
      pw.println("; Stack based virtual machine")
      pw.println("; VERSION $VERSION")
      setupSymbolTable()
      compileAst(ast.root)
      pw.println(".TYPES")
      DataType.each {
        pw.println("${intToHex(it.ordinal())} $it")
      }
      pw.println(".DATA")
      def ldata = data.take(64)
      data = data.drop(64)
      while (!ldata.empty) {
        pw.println((ldata as byte[]).encodeHex().toString())
        ldata = data.take(64)
        data = data.drop(64)
      }
      pw.println(".CODE")
      instructions.each {
        pw.println(it)
      }
    }
  }

  void compileAst(AstNode node) {
    node.children.each() { compileAst(it) }
    switch (node.type) {
      case STRING:
        SymbolEntry address = StackVmUtils.pushString(data, node.value.tokenValue())
        instructions << "${InstructionCode.PUSHADDR} ${intToHex(address.address)}"
        break
      case SYMBOL:
        SymbolEntry address = lookup(node.value.tokenValue())
        instructions << "${InstructionCode.PUSHADDR} ${intToHex(address.address)}"
        break
      case SEQUENCE:
        handleSequence(node)
        break
    }
  }

  void handleSequence(AstNode node) {
    switch (node.subType) {
      case EXPRESSION:
        def firstChild = node.children.first()
        def value = firstChild.value.tokenValue()
        if (symbolTable.containsKey(value) && symbolTable[value].type == FUNCTION) {
          instructions << "${InstructionCode.PUSHVAL}  ${intToHex(node.children.size())}"
          instructions << "${InstructionCode.CALLFUNC}"
        }
        break
    }
  }

  static String intToHex(int val) {
    Integer.toHexString(val).toUpperCase()
  }

  SymbolEntry lookup(String symbol) {
    if (!symbolTable.containsKey(symbol)) {
      symbolTable[symbol] = StackVmUtils.pushSymbol(data, symbol)
    }
    symbolTable[symbol]
  }

  void setupSymbolTable() {
    parser.symbolTable.each {
      symbolTable[it.key] = StackVmUtils.pushSymbol(data, it.key)
      if (it.value.type == Symbol.Type.FUNCTION) {
        symbolTable[it.key].type = FUNCTION
      }
    }
  }

}
