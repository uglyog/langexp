package langexp.command

import langexp.compiler.StackVmCompiler
import langexp.interpreter.StackVmInterpreter

class StackVmCommand extends BaseCommand {

  def type = 'SBVM'
  StackVmInterpreter interpreter
  StackVmCompiler compiler

  void compile() {
    println "Compiling AST to SBVM ..."
    compiler = new StackVmCompiler(ast: ast, inputFile: inputFile)
    if (options.o) {
      compiler.file = options.o
    }
    compiler.compile()
  }

  Object execute() {
    println "Executing SBVM ..."
    println "-" * 80
    interpreter = new StackVmInterpreter(inputFile: compiler.file)
    interpreter.loadFile()
    def result = interpreter.execute()
    println "-" * 80
    result
  }

  void dumpData() {
    interpreter.dumpData()
  }

}
