package superficial

import TwoComplex._
import EdgePath._
import Intersection._
import Staircase._

object DoublePaths {   
 
  /**
   * Given the two canonical geodesic cR d, 
   * outputs the set of crossing double paths in the set D+
   */ 
  def Positive (cR : EdgePath, d : EdgePath, nonPosQuad : NonPosQuad) : Set[Intersection] = {
    require(cR.isPrimitiveLoop, s"$cR is not a primitive loop")
    require(d.isPrimitiveLoop, s"$d is not a primitive loop")
    require(isCanonicalGeodesicLoop(cR, nonPosQuad), s"$cR is not canonical geodesic")  
    require(isCanonicalGeodesicLoop(d, nonPosQuad), s"$d is not canonical geodesic")
     
    val positiveIntersections = cR.intersectionsWith(d, nonPosQuad).filter(el => 
      ((el.length >= 1) && (el.isCrossing(cR,d,nonPosQuad))))

    positiveIntersections  
  } 

  def mod(m : Int, n : Int) : Int = ((m % n) + n) % n 

  /**
   * Given the left and rightmost geodesic cL and cR of c, 
   * and another canonical geodesic d,
   * gives the the set of crossing double paths in the set D0
   */ 
  def Zero (cL : EdgePath, cR : EdgePath, d : EdgePath, nonPosQuad : NonPosQuad) : Set[Intersection] = {
    require(cL.isPrimitiveLoop, s"$cL is not a primitive loop")
    require(cR.isPrimitiveLoop, s"$cR is not a primitive loop")
    require(d.isPrimitiveLoop, s"$d is not a primitive loop")
    require(isCanonicalGeodesicLoop(cR, nonPosQuad), s"$cR is not canonical geodesic")
    require(isCanonicalGeodesicLoop(cL.reverse, nonPosQuad), s"inverse of $cL is not canonical geodesic")
    require(isCanonicalGeodesicLoop(d, nonPosQuad), s"$d is not canonical geodesic")
    require(cR.initial == cL.initial, s"$cR and $cL don't have the same initial vertex")
    
    val spokes : Set[Edge] = getStairCase(cL, cR, nonPosQuad)._2

    /// cL(i) = cR(i)
    def condition11 (intersection : Intersection) : Boolean = {
      edgeVectors(cL)(intersection.start._1).initial == 
      edgeVectors(cR)(intersection.start._1).initial
    }

    def condition12 (intersection : Intersection) : Boolean = {
      val cLEdgeBefore : Edge = edgeVectors(cL)(mod(intersection.start._1 - 1, length(cL)))
      val dEdgeBefore  : Edge = edgeVectors(d)(mod(intersection.start._2 - 1, length(d)))
      val cLEdgeAfter  : Edge = edgeVectors(cL)(intersection.start._1) 
      val dEdgeAfter   : Edge = edgeVectors(d)(intersection.start._2)

      ((cLEdgeBefore == dEdgeBefore) || 
       (cLEdgeAfter  == dEdgeAfter))
    }

    def condition21 (intersection : Intersection) : Boolean = {
      val dEdgeBefore    : Edge = edgeVectors(d)(mod(intersection.start._2 - 1, length(d)))
      val dEdgeAfter     : Edge = edgeVectors(d)(intersection.start._2)
      val endOfSpokeInCL : Option[Int] = cL.findVertexIndex(dEdgeBefore.initial)
      endOfSpokeInCL match {
        case None => false
        case Some(indexInCL) => {
          val cLEdgeBefore    : Edge = edgeVectors(cL)(mod(indexInCL - 1, length(cL)))
          val dTwoEdgeBefore  : Edge = edgeVectors(d)(mod(intersection.start._2 - 2, length(d)))
          val cREdge          : Edge = edgeVectors(cR)(intersection.start._1)
          ((cLEdgeBefore == dTwoEdgeBefore) &&
           // spoke condtion -- this can be a source of error
           (dEdgeBefore.initial  == edgeVectors(cL)(indexInCL).initial) &&
           (dEdgeBefore.terminal == edgeVectors(cR)(intersection.start._1).initial) && 
           (spokes.contains(dEdgeBefore))) 
        }
      }
    }

    def condition22 (intersection : Intersection) : Boolean = {
      val dEdgeAfter     : Edge = edgeVectors(d)(intersection.start._2)
      val dTwoEdgeAfter  : Edge = edgeVectors(d)(mod(intersection.start._2 + 1, length(d)))
      val endOfSpokeInCL : Option[Int] = cL.findVertexIndex(dEdgeAfter.terminal)
      endOfSpokeInCL match {
        case None => false
        case Some(indexInCL) => {
          val cLEdgeAfter  : Edge = edgeVectors(cL)(indexInCL)  
          (dTwoEdgeAfter == cLEdgeAfter)
          // spoke condtion -- this can be a source of error
          (dEdgeAfter.initial  == edgeVectors(cR)(intersection.start._1).initial) &&
          (dEdgeAfter.terminal == edgeVectors(cL)(indexInCL).initial) && 
          (spokes.contains(dEdgeAfter))
        }
      }  
    }

    val zeroIntersections         : Set[Intersection] = 
      cR.intersectionsWith(d, nonPosQuad).filter(el => 
        (el.length == 0)).filter(el => (el.isCrossing(cR, d, nonPosQuad)))
    val satisfyingFirstCondition  : Set[Intersection] = 
      zeroIntersections.filter(el => (condition11(el) && condition12(el)))
    val satisfyingSecondCondition : Set[Intersection] = 
      zeroIntersections.filter(el => (condition21(el) || condition22(el)))

    (satisfyingFirstCondition ++ satisfyingSecondCondition)
  }

  /**
   * Given the left and rightmost geodesic cL and cR of c, 
   * and another canonical geodesic d,
   * gives the the set of crossing double paths in the set D-
   */
  def Negative (cL : EdgePath, cR : EdgePath, d : EdgePath, nonPosQuad : NonPosQuad) : Set[Intersection] = {
    require(cL.isPrimitiveLoop, s"$cL is not a primitive loop")
    require(cR.isPrimitiveLoop, s"$cR is not a primitive loop")
    require(d.isPrimitiveLoop, s"$d is not a primitive loop")
    require(isCanonicalGeodesicLoop(cR, nonPosQuad), s"$cR is not canonical geodesic")
    require(isCanonicalGeodesicLoop(cL.reverse, nonPosQuad), s"inverse of $cL is not canonical geodesic")
    require(isCanonicalGeodesicLoop(d, nonPosQuad), s"$d is not canonical geodesic")
    require(cR.initial == cL.initial, s"$cR and $cL don't have the same initial vertex")

    val cLInverse : EdgePath = cL.reverse 
    val spokes : Set[Edge] = getStairCase(cL, cR, nonPosQuad)._2 

    // Given a vertex finds the edge in the path ending at it
    def findEdgeEndingAt (path : EdgePath, vertex : Vertex) : Option[Edge] = {
      path match {
        case Constant(v) => None
        case Append(init, last) => {
          if (last.terminal == vertex) Some(last)
          else findEdgeEndingAt(init, vertex)
        }
      }
    }

    // Given a vertex finds the edge in the path starting at it
    def findEdgeStartingAt (path : EdgePath, vertex : Vertex) : Option[Edge] = {
      path match {
        case Constant(v) => None
        case Append(init, last) => {
          if (last.initial == vertex) Some(last)
          else findEdgeEndingAt(init, vertex)
        }
      }
    }

    def condition1 (intersection : Intersection) : Boolean = {
      val vertexAtIntersection : Vertex = 
        edgeVectors(cLInverse)(intersection.start._1).initial
      // Since cR is primitive there is an unique edge ending at the vertex at the intersection
      val edgeInCR : Option[Edge] = 
        findEdgeEndingAt(cR, vertexAtIntersection)
      val edgeBeforeInD : Edge = 
        edgeVectors(cLInverse)(mod(intersection.start._2 - 1, length(d)))
      
      edgeInCR match {
        case None => false
        case Some(ed) => (ed == edgeBeforeInD)
      }
    }

    def condition2 (intersection : Intersection) : Boolean = {
      val vertexAtIntersection : Vertex = 
        edgeVectors(cLInverse)(intersection.end._1).initial
      // Since cR is primitive there is an unique edge ending at the vertex at the intersection
      val edgeInCR : Option[Edge] = 
        findEdgeStartingAt(cR, vertexAtIntersection)
      val edgeAfterInD : Edge = 
        edgeVectors(cLInverse)(intersection.end._2)
      
      edgeInCR match {
        case None => false
        case Some(ed) => (ed == edgeAfterInD)
      }
    } 

    def condition3 (intersection : Intersection) : Boolean = {
      val dTwoEdgeBefore : Edge = edgeVectors(d)(mod(intersection.start._2 - 2, length(d)))
      val dEdgeBefore    : Edge = edgeVectors(d)(mod(intersection.start._2 - 1, length(d)))
      val endOfSpokeInCR : Option[Int] = cR.findVertexIndex(dEdgeBefore.initial)
      endOfSpokeInCR match {
        case None => false
        case Some(indexInCR) => {
          val cREdgeBefore : Edge = edgeVectors(cR)(mod(indexInCR - 1, length(cR)))
          (spokes.contains(dEdgeBefore) &&
          (cREdgeBefore == dTwoEdgeBefore))
        }
      }
    }

    def condition4 (intersection : Intersection) : Boolean = {
      val dTwoEdgeAfter  : Edge = edgeVectors(d)(mod(intersection.end._2 + 1, length(d)))
      val dEdgeAfter     : Edge = edgeVectors(d)(intersection.end._2)
      val endOfSpokeInCR : Option[Int] = cR.findVertexIndex(dEdgeAfter.terminal)
      endOfSpokeInCR match {
        case None => false
        case Some(indexInCR) => {
          val cREdgeAfter : Edge = edgeVectors(cR)(indexInCR)
          (spokes.contains(dEdgeAfter) &&
          (cREdgeAfter == dTwoEdgeAfter))
        }
      }
    }

    val negativeIntersections : Set[Intersection] = 
      cLInverse.intersectionsWith(d, nonPosQuad).filter(el => (el.isCrossing(cLInverse, d, nonPosQuad)))
    val goodOnes              : Set[Intersection] = 
      negativeIntersections.filterNot(el => 
      (condition1(el) || condition2(el) || condition3(el) || condition4(el)))
    
    goodOnes
  }

  /**
   * Calculates the geometric intersection numbers of two primitive curves.
   */
  def geometricIntersection (c : EdgePath, d : EdgePath, nonPosQuad : NonPosQuad) : Int = {
    require(c.isPrimitiveLoop, s"$c is not a primitive loop")
    require(d.isPrimitiveLoop, s"$d is not a primitive loop")

    val cR : EdgePath = canoniciseLoop(c, nonPosQuad)
    val cL : EdgePath = canoniciseLoop(c.reverse, nonPosQuad).reverse
    val dR : EdgePath = canoniciseLoop(d, nonPosQuad)

    require(isCanonicalGeodesicLoop(cR, nonPosQuad), s"$cR is not canonical geodesic")
    require(isCanonicalGeodesicLoop(cL.reverse, nonPosQuad), s"inverse of $cL is not canonical geodesic")
    require(isCanonicalGeodesicLoop(dR, nonPosQuad), s"$dR is not canonical geodesic")

    val cL1 : EdgePath = cR.makeBasePointSame(cL)
    val DPositive : Set[Intersection] = Positive(cR, dR, nonPosQuad)
    val DZero     : Set[Intersection] = Zero(cL1, cR, dR, nonPosQuad)
    val DNegative : Set[Intersection] = Negative(cL1, cR, dR, nonPosQuad)

    (DPositive.size + DZero.size + DNegative.size)
  }
}