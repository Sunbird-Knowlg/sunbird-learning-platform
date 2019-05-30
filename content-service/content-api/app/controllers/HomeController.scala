package controllers

import javax.inject.Singleton
import play.api.mvc.{Action, Controller}

/*
import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, ControllerComponents}

*/


/**
 * Hello world!
 *
 */
/*object HomeControl {
  /*println( "Hello World!" )*/

  def index( ) : Unit = {
    println("Hello, Scala!")
  }

}*/
@Singleton
 object HomeController extends Controller{
  def index() = Action {
    Ok("Hello Sunbird").as("text/plain")
  }
}