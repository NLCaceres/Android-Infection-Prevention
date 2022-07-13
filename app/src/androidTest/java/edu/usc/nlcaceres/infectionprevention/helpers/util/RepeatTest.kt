package edu.usc.nlcaceres.infectionprevention.helpers.util

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

// Create an annotation to add after @Test annotation
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
annotation class RepeatTest(val value: Int = 1)

// Then have the rule call it however many times you want! (Add it via @get:Rule or @Rule @JvmField)
class RepeatRule : TestRule {
  private class RepeatStatement(private val statement: Statement, private val repeat: Int) : Statement() {
    @Throws(Throwable::class)
    override fun evaluate() {
      for (i in 0 until repeat) {
        statement.evaluate()
      }
    }
  }

  override fun apply(statement: Statement, description: Description): Statement {
    var result = statement // Grab test we're calling
    val repeat = description.getAnnotation(RepeatTest::class.java) // Grab test for later
    if (repeat != null) {
      val times = repeat.value // Now we can get # of times to call test
      result = RepeatStatement(statement, times) // Use our private class to call the test with a loop
    }
    return result
  }
}