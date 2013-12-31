package langexp.command

import langexp.compiler.StackVmCompiler
import langexp.interpreter.StackVmInterpreter

class StackVmCommand extends BaseCommand {

  def type = 'SBVM'
  StackVmInterpreter interpreter
  StackVmCompiler compiler

  void compile() {
    if (options.v) {
      println "Compiling AST to SBVM ..."
    }
    compiler = new StackVmCompiler(ast: ast, inputFile: inputFile, verbose: options.v)
    if (options.o) {
      compiler.file = options.o
    }
    compiler.compile()
  }

  Object execute() {
    if (options.v) {
      println "Executing SBVM ..."
      println "-" * 80
    }
    interpreter = new StackVmInterpreter(inputFile: compiler.file)
    interpreter.loadFile()
    def result = interpreter.execute()
    if (options.v) {
      println "-" * 80
    }
    result
  }

  void dumpData() {
    interpreter.dumpData()
  }

  String print(values) {
    values.join(' ')
  }

}
