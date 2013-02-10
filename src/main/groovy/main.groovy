import langexp.compiler.StackVmCompiler
import langexp.interpreter.AstInterpreter
import langexp.interpreter.StackVmInterpreter
import langexp.lex.Token
import langexp.lex.Tokeniser
import langexp.parser.Parser
import org.apache.commons.lang3.time.StopWatch

def cli = new CliBuilder(usage: 'langexp [options] [targets]', header: 'Options:')
cli.h(longOpt: 'help', 'print this message')
cli.t(longOpt: 'target', args: 1, argName: 'target', 'set the target to execute/compile to [SBVM]')
cli.c(longOpt: 'compile', 'compile the source instead of executing it')
cli.o(longOpt: 'output', args: 1, argName: 'outputFile', 'file to write to')
def options = cli.parse(args)

if (!options?.arguments()) {
  cli.usage()
  System.exit(1)
}

def targets = ['SBVM']
if (options.t && !targets.contains(options.t.toUpperCase())) {
  println "Invalid target type of ${options.t}"
  cli.usage()
  System.exit(1)
}

options.arguments().each { inputFile ->
  new File(inputFile).withReader { reader ->
    def stopwatch = new StopWatch()
    stopwatch.start()

    if (options.t && !options.c) {
      println "Executing SBVM ..."
      StackVmInterpreter interpreter = new StackVmInterpreter(inputFile: inputFile)
      interpreter.loadFile()
      def result = interpreter.execute()
      stopwatch.split()
      println "Execution Took: ${stopwatch.toSplitString()}"
      println "Result = $result"
      interpreter.dumpData()
    } else {
      def tokeniser = new Tokeniser(input: new PushbackReader(reader))
      def parser = new Parser(tokeniser: tokeniser)
      def ast = parser.parse()
      stopwatch.split()
      println "Parsing Took: ${stopwatch.toSplitString()}"
      ast.printAsciiTree()
      if (tokeniser.errors) {
        println "\nFound the following lexical errors:"
        tokeniser.errors.each { println it }
      }
      if (parser.errors) {
        println "\nFound the following syntax errors:"
        parser.errors.each { println it }
      }

      if (!tokeniser.errors && !parser.errors) {
        if (options.c) {
          println "Compiling AST to SBVM ..."
          def compiler = new StackVmCompiler(ast: ast, inputFile: inputFile)
          if (options.o) {
            compiler.file = options.o
          }
          compiler.compile()
        }
        else {
          println "Executing AST ..."
          AstInterpreter interpreter = new AstInterpreter(ast: ast)
          def result = interpreter.execute()
          stopwatch.split()
          println "Execution Took: ${stopwatch.toSplitString()}"
          println "Result = $result"
          interpreter.dumpData()
        }
      }
    }
    stopwatch.stop()
    println "Total Time: $stopwatch"
  }
}
