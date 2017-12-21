package freegroups

import LinNormBound._

import spire.math._
import spire.implicits._


sealed abstract class LinNormBound(val word: Word, val bound: Rational){
  def ++(that: LinNormBound) = Triang(this, that)

  def *:(n: Int) = ConjGen(n, this)

  def +:(n: Int) = Gen(n) ++ this
}

object LinNormBound{
  def inverse: LinNormBound => LinNormBound = {
    case Gen(n) => Gen(-n)
    case ConjGen(n, pf) => ConjGen(-n, inverse(pf))
    case Triang(a, b) => Triang(inverse(b), inverse(a))
    case PowerBound(baseword, n, pf) => PowerBound(baseword.inv, n, inverse(pf))
    case Empty => Empty
  }

  case class Gen(n: Int) extends LinNormBound(Word(Vector(n)), 1){
    override val toString = Word(Vector(n)).toString
  }

  case class ConjGen(n: Int,pf: LinNormBound) extends LinNormBound(n +: pf.word :+ (-n), pf.bound)

  case class Triang(pf1: LinNormBound, pf2: LinNormBound) extends LinNormBound(pf1.word ++ pf2.word, pf1.bound + pf2.bound)

  case class PowerBound(baseword: Word, n: Int, pf: LinNormBound) extends LinNormBound(baseword, pf.bound/n){
    assert(pf.word == Word(Vector.fill(n)(baseword.ls).reduce(_ ++ _)), s"power bound failed, ${pf.word}, $baseword, $n")
  }

  case object Empty extends LinNormBound(Word(Vector()), 0)
}
