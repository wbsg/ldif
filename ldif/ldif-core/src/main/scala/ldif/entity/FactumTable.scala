package ldif.entity

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 13.05.11
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */

//Each pattern has a table of values (=Factums)
// one column for each path in the pattern
// order is not important
trait FactumTable extends Traversable[FactumRow]

//One value (=Factum) for each path in the pattern
// order here is important
trait FactumRow extends IndexedSeq[Node]