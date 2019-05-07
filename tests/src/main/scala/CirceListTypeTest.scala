import estrapade.{TestApp, test}

import language.experimental.macros
import MagnoliaEncoder.genEncoder
import io.circe.Encoder

case class Recursive(field: Int, recursion: List[Recursive])

/**
  * Problem here is that we expect the same behaviour as shapeless Lazy provides -
  * the derived codec itself would be visible and used to construct the codec for List using circe std one.
  * Magnolia doesn't see it and derives coproduct codec for List instead, which doesn't look nice for json API:
  * {{{
  * {
  *   "::" : {
  *      "head" : {
  *        "field" : 2,
  *        "recursion" : {
  *           "Nil" : "Nil"
  *         }
  *       },
  *       "tl$access$1" : {
  *         "Nil" : "Nil"
  *       }
  *     }
  *   }
  * }
  * }}}
  */
object CirceRecursiveTypeTest extends TestApp {

  def tests(): Unit = {

    val encoderl = Encoder[List[Recursive]]
    test("Use available encoders for an immutable list") {
      encoderl(List(Recursive(1, List(Recursive(2, Nil), Recursive(3, Nil)))))
    }
      .assert(
        j => j.asArray.isDefined,
        _.toString()
      )
  }
}
