package langexp.compiler

import langexp.parser.Ast
import langexp.parser.AstNode
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

  void compile() {
    file = file ?: (FilenameUtils.removeExtension(inputFile) + '.sbvm')
    if (verbose) {
      println("Compiling to $file")
    }
    new File(file).withPrintWriter { pw ->
      pw.println("; Stack based virtual machine")
      pw.println("; VERSION $VERSION")
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
      case FUNCTION:
        def address = lookup(node.value.tokenValue())
        instructions << "${InstructionCode.PUSHVAL}  ${intToHex(node.children.size())}"
        instructions << "${InstructionCode.CALLFUNC} ${intToHex(address)}"
        break
      case STRING:
        def address = StackVmUtils.pushString(data, node.value.tokenValue())
        instructions << "${InstructionCode.PUSHADDR} ${intToHex(address)}"
        break
      case SYMBOL:
        def address = lookup(node.value.tokenValue())
        instructions << "${InstructionCode.PUSHADDR} ${intToHex(address)}"
        break
    }
  }

  static String intToHex(int val) {
    Integer.toHexString(val).toUpperCase()
  }

  int lookup(String symbol) {
    if (!symbolTable.containsKey(symbol)) {
      symbolTable[symbol] = StackVmUtils.pushSymbol(data, symbol)
    }
    symbolTable[symbol]
  }

}
