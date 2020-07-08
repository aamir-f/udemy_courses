trait Animal {
  def eat(): Unit
  def name = "animal"
}

val x: Animal = () => println("Eat")
x.name
x.eat()

new Thread(
  () => println("")
)

class Mutable {
  private var internalMember: Int = 0 // private for OO encapsulation
  def member = internalMember // "getter"
  def member_=(value: Int): Unit = internalMember = value // "setter"
}

val o = new Mutable()
o.member
o.member = 10
o.member

