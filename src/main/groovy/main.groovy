import langexp.command.AstCommand
import langexp.command.StackVmCommand
import org.apache.commons.lang3.time.StopWatch

def cli = new CliBuilder(usage: 'langexp [options] [targets]', header: 'Options:')
cli.h(longOpt: 'help', 'print this message')
cli.t(longOpt: 'target', args: 1, argName: 'target', 'set the target to execute/compile to [SBVM, AST]')
cli.c(longOpt: 'compile', 'only compile the source instead of executing it')
cli.o(longOpt: 'output', args: 1, argName: 'outputFile', 'file to write to')
def options = cli.parse(args)

if (!options?.arguments()) {
  cli.usage()
  System.exit(1)
}

def command = new StackVmCommand(options: options)
if (options.t) {
  switch (options.t.toUpperCase()) {
    case 'SBVM':
      break
    case 'AST':
      command = new AstCommand(options: options)
      break
    default:
      println "Invalid target type of ${options.t}"
      cli.usage()
      System.exit(1)
  }
}

println "Target set to ${command.type}"

options.arguments().each { inputFile ->

  command.inputFile = inputFile

  new File(inputFile).withReader { reader ->
    def stopwatch = new StopWatch()
    stopwatch.start()

    println "-- Parse Phase --"
    command.parse()
    stopwatch.split()
    println "Execution Took: ${stopwatch.toSplitString()}"

    command.ast.printAsciiTree()
    if (command.tokeniser.errors) {
      println "\nFound the following lexical errors:"
      command.tokeniser.errors.each { println it }
    }
    if (command.parser.errors) {
      println "\nFound the following syntax errors:"
      command.parser.errors.each { println it }
    }

    if (!command.tokeniser.errors && !command.parser.errors) {
      println "-- Compile Phase --"
      command.compile()
      stopwatch.split()
      println "Execution Took: ${stopwatch.toSplitString()}"

      if (!options.c) {
        println "-- Execute Phase --"
        def result = command.execute()
        stopwatch.split()
        println "Execution Took: ${stopwatch.toSplitString()}"
        println "Result = $result"
        command.dumpData()
      }
    }

    stopwatch.stop()
    println "Total Time: $stopwatch"
  }
}
