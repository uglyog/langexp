package langexp.util

/*
 *  Reproduced from
 * 	http://groovy.codehaus.org/Formatting+simple+tabular+text+data
 * (with some code reformatting according to my readability preferences)
 */

class Table {
  def columns = []     // contains columns names and their length
  def columnLen = [:]  // contains lengthes of the columns
  def rows = []

  Table addColumn(String name, int size) {
    columns << [name: name, size: size];
    columnLen[name] = size
    return this
  }

  Table addRow(row) {
    rows << row
    return this
  }

  def print() {
    println()
    def headerDiv = columns.collect { "_" * (it.size + 1) }.join(' ')
    println(headerDiv)
    println(columns.collect { ' ' + it.name.center(it.size) }.join(' '))
    println(headerDiv)
    rows.each { row ->
      def rowStr = []
      columns.eachWithIndex { col, i ->
        rowStr << ' ' + row[i].toString().padRight(col.size).substring(0, col.size)
      }
      println(rowStr.join(' '))
    }
    println(headerDiv)
  }
}
