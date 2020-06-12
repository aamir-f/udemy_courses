
val l = List(1, 2, 3, 4, 5, 6)
val x = l.drop(1)
l zip x
res0 forall (x => x._1 < x._2)

val words = List("one", "two", "one", "three")
words.groupBy(identity).map(x => (x._1, x._2.length))


List(1, 2, 3, 4, 5, 6).sliding(2, 1).toList

val s = Set(1, 2, 3, 4)

val ss: Boolean = s(22) //no order

//for order

import scala.collection.immutable.TreeSet

val t = TreeSet(1, 2, 3, 4, 5)
//implemented as RedBlack tree, i.e a balanced binary search tree. O(log n) lookup.
t(0)


def CC[A](a: A*): List[A] = {
  if (a.isEmpty) Nil else ::(a.head, CC(a.tail: _*))
}
CC(1, 2, 3, 4, 5, 56)

def head[A](list: List[A]): A = list match {
  case Nil    => throw new Exception("Empty List")
  case h :: _ => h
}
head(List(3, 4, 4))

List(1, 2, 3).dropWhile(_ % 2 != 0)

def myDrop[A](list: List[A], n: Int): List[A] = n match {
  case 0 => list
  case _ => myDrop(list.tail, n - 1)
}
myDrop(List(1, 2, 3), 2)

def myDropWhile[A](list: List[A], f: A => Boolean): List[A] = list match {
  case h :: tail if f(h) => myDropWhile(tail, f)
  case _                 => list
}
myDropWhile(List(2,4,3,7,9),(x: Int) => x % 2 == 0 )

def myDropWhileCurried[A](list: List[A])(f: A => Boolean):List[A] = list match {
  case h :: tail if f(h) => myDropWhileCurried(tail)(f)
  case _                 => list
}
myDropWhileCurried(List(2,4,5,7))((x: Int) => x % 2 == 0)