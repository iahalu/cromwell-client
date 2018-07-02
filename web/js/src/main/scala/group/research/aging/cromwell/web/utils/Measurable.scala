package group.research.aging.cromwell.web.utils

import org.scalajs.dom

import scala.scalajs.js

/**
 * Trait that measures time of function execution
 */
trait Measurable {

  def measure(name: String)(fun: () => Unit): (String, Number) = {
    val start = new js.Date().getTime()
    fun()
    val end = new js.Date().getTime()
    val time: Number = end - start
    (name, time)
  }

  def alertedMeasure(name: String)(fun: () => Unit): Unit = {
    val tn: (String, Number) = this.measure(name)(fun)
    dom.window.alert(s"function ${tn._1} exectured for ${tn._2}")
  }

  def loggedMeasure(name: String)(fun: () => Unit): Unit = {
    val tn: (String, Number) = this.measure(name)(fun)
    dom.console.log(s"function ${tn._1} exectured for ${tn._2}")
  }
}
