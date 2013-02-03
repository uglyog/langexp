import langexp.lex.Token
import langexp.lex.Tokeniser

def cli = new CliBuilder(usage:'langexp [options] [targets]', header:'Options:')
cli.help('print this message')
def options = cli.parse(args)

if (!options?.arguments()) {
    cli.usage()
    System.exit(1)
}

options.arguments().each {
    new File(it).withReader { reader ->
        def tokeniser = new Tokeniser(input: new PushbackReader(reader))
        def token = tokeniser.nextToken()
        while (token.type != Token.Type.EOF) {
            println token
            token = tokeniser.nextToken()
        }
        println token
        if (tokeniser.errors) {
            println "\nFound the following errors:"
            tokeniser.errors.each { println it }
        }
    }
}
