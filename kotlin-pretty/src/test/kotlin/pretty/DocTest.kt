package pretty

import arrow.core.AndThen
import arrow.core.Eval
import arrow.core.extensions.eval.applicative.applicative
import arrow.core.extensions.monoid
import arrow.core.identity
import arrow.core.some
import arrow.fx.IO
import arrow.typeclasses.Show
import pretty.doc.arbitrary.arbitrary
import pretty.pagewidth.arbitrary.arbitrary
import propCheck.*
import propCheck.instances.arbitrary
import propCheck.testresult.testable.testable

class DocTest : PropertySpec({
    /*
    "layoutPretty should never render to a document that contains a fail"(Args(maxSuccess = 1_000)) {
        forAll(Doc.arbitrary(String.arbitrary()), Show { "<Doc>" }) { doc ->
            forAll(PageWidth.arbitrary()) { pw ->
                val sDoc = doc.layoutPretty(PageWidth.default())
                val hasFail = sDoc.hasFail().not()

                hasFail
            }
        }
    }
    "layoutSmart should never render to a document that contains a fail"(Args(maxSuccess = 1_000)) {
        forAll(Doc.arbitrary(String.arbitrary()), Show { "<Doc>" }) { doc ->
            forAll(PageWidth.arbitrary()) { pw ->
                val sDoc = doc.layoutSmart(PageWidth.default())
                val hasFail = sDoc.hasFail().not()

                hasFail
            }
        }
    }
    "layoutCompact should never render to a document that contains a fail"(Args(maxSuccess = 1_000)) {
        forAll(Doc.arbitrary(String.arbitrary()), Show { "<Doc>" }) { doc ->
            forAll(PageWidth.arbitrary()) { pw ->
                val sDoc = doc.layoutCompact()
                val hasFail = sDoc.hasFail().not()

                hasFail
            }
        }
    }
    "fuse should never change how a doc is rendered"(Args(maxSuccess = 1_000)) {
        forAll(Doc.arbitrary(String.arbitrary()), Show { "Doc: \"${diag()}\"" }) { doc ->
            forAll(PageWidth.arbitrary()) { pw ->
                forAll(Boolean.arbitrary()) { b ->
                    // TODO clean this up... Exceptions will get caught in the propCheck rework
                    try {
                        val sDoc = doc.layoutPretty(pw).renderStringAnn()
                        val sDocFused = doc.fuse(b).layoutPretty(pw).renderStringAnn()
                        counterexample({
                            "Fused doc: ${doc.fuse(b).diag()}"
                        }, sDoc.eqv(sDocFused))
                    } catch (e: Exception) {
                        TestResult.testable().run { failed("Exception", e.some()).property() }
                    }
                }
            }
        }
    }
     */
    "group2 renders the same as group2"(Args(maxSuccess = 1_000)) {
        forAll(Doc.arbitrary(String.arbitrary()), Show { "Doc: \"${diag(nest = listOf(0))}\"" }) {
            forAll(PageWidth.arbitrary()) { pw ->
                val a = it.group2().layoutPretty(PageWidth.default()).renderStringAnn()
                val b = it.group().layoutPretty(PageWidth.default()).renderStringAnn()

                counterexample({
                    "A: \"$a\"" + "\n" +
                            "ADoc \"${it.group2().diag(nest = listOf(0))}\""
                }, counterexample({
                    "B: \"$b\"" + "\n" +
                            "BDoc \"${it.group().diag(nest = listOf(0))}\""
                }, a == b))
            }
        }
    }
})

fun SimpleDoc<String>.renderStringAnn(): String =
    renderDecorated(String.monoid(), ::identity, { "<-$it" }, { "$it->" })

tailrec fun <A> SimpleDoc<A>.hasFail(): Boolean = when (val dF = unDoc.value()) {
    is SimpleDocF.Fail -> true
    is SimpleDocF.Nil -> false
    is SimpleDocF.RemoveAnnotation -> dF.doc.hasFail()
    is SimpleDocF.AddAnnotation -> dF.doc.hasFail()
    is SimpleDocF.Line -> dF.doc.hasFail()
    is SimpleDocF.Text -> dF.doc.hasFail()
}