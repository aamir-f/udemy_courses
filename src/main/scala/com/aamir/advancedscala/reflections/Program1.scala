package com.aamir.advancedscala.reflections

import scala.reflect.runtime.universe

object Program1 extends App {

  //reflections + macros + quasiquotes = meta-programming

  case class Person(name: String) {
    def sayMyName: Unit = println(s"Hi my name is: $name")
  }

  import scala.reflect.runtime.{universe => ru} // type alias
  //1. Mirros: something that can reflect things.

  // now, instantiate a mirror
  val m = ru.runtimeMirror(getClass.getClassLoader)

  //create a class object by name = "description"
  val clazz: ru.ClassSymbol = m.staticClass("com.aamir.advancedscala.reflections.Program1.Person")

  //create a reflected mirror to access class members, we can invoke things
  val cm: ru.ClassMirror = m.reflectClass(clazz)

    // get the constructor
  val constructor: ru.MethodSymbol = clazz.primaryConstructor.asMethod

  //reflect the contructor, by creating constructor mirror
  val constMirror: ru.MethodMirror = cm.reflectConstructor(constructor)

  //invoke the constructor
  val instance: Any = constMirror.apply("John")
  println(instance)
}

object Program2 extends App {

  case class Person(name: String) {
    def sayMyName: Unit = println(s"Hi my name is: $name")
  }

  //e.g I've an instance obtained from somewhere else
  val p = Person("Mary") // say, we don't know type is Person

  //method name computed from somewhere else
  val methodName: String = "sayMyName"

  import scala.reflect.runtime.universe

  //steps

  // obtain the general mirror
  val m: universe.Mirror = universe.runtimeMirror(getClass.getClassLoader)

  //reflect the instance
  val reflected: universe.InstanceMirror = m.reflect(p)

  //method symbol
  val methodSymbol: universe.MethodSymbol = universe.typeOf[Person].decl(universe.TermName(methodName)).asMethod

  // reflect method
  val method: universe.MethodMirror = reflected.reflectMethod(methodSymbol)

  //invoke the method
  method.apply()

}

object Program3 extends App {
  import scala.reflect.runtime.universe._

  case class Person(name: String) {
    def sayMyName: Unit = println(s"Hi my name is: $name")
  }

  val p = Person("Mary") // say, we don't know type is Person
  val methodName: String = "sayMyName"

  val m: universe.Mirror = universe.runtimeMirror(getClass.getClassLoader)

  val reflected: universe.InstanceMirror = m.reflect(p)
  val methodSymbol: universe.MethodSymbol = reflected.symbol.info.member(TermName("sayMyName")).asMethod
  val reflectedMethod: universe.MethodMirror = reflected.reflectMethod(methodSymbol)
  reflectedMethod.apply()
}

object Program4 extends App {
  // type erasure problem, type erasure was introduced to support backward compatibility in Java

  // pp #1: differentiate types at runtime
  val numbers = List(1,2,3)
  numbers match  {
    case listOfStrings: List[String] => println("list of strings") //println this
    case listOfNumbers: List[Int] => println("list of numbers")
  }

  // pp #2: limitations on overloads
  //  def processList(list: List[Int]): Int = 43
  //  def processList(list: List[String]): Int = 45

  /** TypeTags
   * Reflection Api workaround which allows carrying the complete generic type at compile-time to run-time
   */

  case class Person(name: String) {
    def sayMyName: Unit = println(s"Hi my name is: $name")
  }
  import universe._
  val tt: universe.TypeTag[Person] = typeTag[Person]
  val t: universe.Type = tt.tpe
  println(tt.tpe) // fully qualified name of this type

   class MyMap[K, V]

   def getTypeArguments[T: TypeTag](value: T) = typeTag.tpe match {
     case TypeRef(pre, symb, typeArguments) => {
       println("pre => " + pre)
       println("symb => " + symb)
       typeArguments
     }
     case _ => Nil
   }


  val myMap = new MyMap[Int, String]
  val typeArgs = getTypeArguments(myMap) // implicit --> (typeTag: TypeTag[MyMap[Int, String]])
  println(typeArgs)

  def isSubType[A, B](implicit typeTagA: TypeTag[A], typeTagB: TypeTag[B]): Boolean = {
    typeTagA.tpe <:< typeTagB.tpe
  }

  class Animal
  class Dog extends Animal

  println(isSubType[Dog, Animal])

  //linking of typetags with reflection Api
  import scala.reflect.runtime.universe._
  val methodName = "sayMyName"



  val p = new Person("jammie")
  val m: universe.Mirror = universe.runtimeMirror(getClass.getClassLoader)
  val reflected: universe.InstanceMirror = m.reflect(p)
  val methodSymbol: universe.MethodSymbol = typeTag[Person].tpe.decl(universe.TermName(methodName)).asMethod
  val reflectedMethod: universe.MethodMirror = reflected.reflectMethod(methodSymbol)
  reflectedMethod.apply()

}