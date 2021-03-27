import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.sql.{SparkSession, _}
import org.apache.spark.sql.functions._
import graphframes._
import org.graphframes.GraphFrame
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD


object shortestpath extends App {
  val spark = SparkSession.builder.master("local[2]").appName("load_graph_data").config("spark.executor.memory", "3g").getOrCreate;

  spark.sparkContext.setLogLevel("ERROR")
  import spark.implicits._

  val graph = GraphLoader.edgeListFile(spark.sparkContext, "file:////Users/surajshashidhar/Desktop/web-Google.txt")
  val sourceId: VertexId = 0L

  val cg = new createGraph(spark);

  val og = cg.createGraphFromFile();

  val gg = og.toGraphX;
  val m = gg.triplets.map(row => row.srcId +  " | " + row.srcAttr.toString()  + " | " + row.attr.toString() +  " | " + row.dstId);
  println(" --------  graph triplets ------------n  ")

  m.take(10).foreach(println)

  val ap = new Astar_sp(spark)
  val res = ap.calculate_astar_sp(gg, 91769840, 1928366671)
  res.take(10).foreach(println)

  val res1 = ap.calculate_astar_sp(gg,176616871L, 2657395883L)
  res1.take(10).foreach(println)

  val res2 = ap.calculate_astar_sp(gg, 29353963L, 51518449L)
  res2.take(10).foreach(println)

  val res3 = ap.calculate_astar_sp(gg, 11986228L, 122596365L)
  res3.take(10).foreach(println)

  val res4 = ap.calculate_astar_sp(gg, 31195854L, 151814859L)
  res4.take(10).foreach(println)

  //val x = cg.uat()
  //x.map(row => (row(0).asInstanceOf[Long] ,row(1).asInstanceOf[Long])).map(row => ap.calculate_astar_sp(gg, row._1, row._2)).map(row => row(0).toString + "|" + row(1).toString + "|" + row(2) + "|" + row(3)).collect().foreach(println)

//row(0) + "|" + row(1) + "|" + row(2) + "|" + row(3) + "|" + row(4)
  /*
  val gx = graph.mapVertices( (id, _) =>
    if (id == sourceId) Array(0.0,0.0, id, id)
    else Array(Double.PositiveInfinity,Double.PositiveInfinity, id, id)).
    mapEdges( e => 1 )
//
  gx.cache()
  gx.vertices.take(10)
  gx.edges.take(10)
  println(" ---- the gx is ------ ")
  gx.triplets.map(row => row.srcId +" , " + row.srcAttr.mkString(" - ") + " , " + row.attr + " , " + row.dstId + " , "  + row.dstAttr.mkString(" - ")).collect().foreach(println);

  println( " ----------  end of the 1st module and start of shortest path program ----------- ");
  //Array(Double.PositiveInfinity, -1) -- initial message
  //message is of the type [Infinity, -1]

  val msssp = gx.pregel(Array(Double.PositiveInfinity,  Double.PositiveInfinity, -1, -1))(
    (id, dist, newDist) =>
    {
      if (dist(0) <= newDist(0) && dist(1) <= newDist(1))
        {

          println(id);
          dist
        }
      else if(dist(0) <= newDist(0) && dist(1) > newDist(1))
        {
          println(dist.mkString(" ---dist--- ")+ " --- "+ newDist.mkString(" newDist "))
          Array(dist(0), newDist(1), dist(2))
        }
      else if(dist(0) > newDist(0) && dist(1) <= newDist(1) )
      {
        Array(newDist(0), dist(1), newDist(2))
      }
      else if(dist(0) > newDist(0) && dist(1) > newDist(1))
        {
          println(newDist.mkString(" ---newDist--- ") + " --- "+ dist.mkString(" dist "))
          println(id)
          newDist
        }
      else {
        newDist
      }
    },

    triplet => {
      println(triplet.srcId + " | " + triplet.srcAttr(0) + " | " + triplet.srcAttr(1) + " | " + triplet.attr + " | "+ triplet.dstAttr(0) + " | "+ triplet.dstAttr(1) + " | " + triplet.dstId)

      if(triplet.srcId == sourceId)
        {
          if(triplet.srcAttr(0) + triplet.attr  < triplet.dstAttr(0))
            Iterator((triplet.dstId, Array(triplet.srcAttr(0) + triplet.attr, Double.PositiveInfinity, triplet.srcId)))
          else
            Iterator.empty
        }

      else if (triplet.srcAttr(0) + triplet.attr  < triplet.dstAttr(0) && triplet.srcAttr(1) + triplet.attr  < triplet.dstAttr(1)) {
        println(triplet.srcId + " |@| " + triplet.srcAttr(0) +  " |@| " + triplet.srcAttr(1) + " |@| " + triplet.attr + " |@| "+ triplet.dstAttr(0) + " |@| " + triplet.dstAttr(1) + " |@| " + triplet.dstId)
        Iterator((triplet.dstId, Array(triplet.srcAttr(0) + triplet.attr, triplet.srcAttr(1) + triplet.attr, triplet.srcId)))
      }
      else if (triplet.srcAttr(0) + triplet.attr  < triplet.dstAttr(0) && triplet.srcAttr(1) + triplet.attr  <= triplet.dstAttr(1)) {
        println(triplet.srcId + " |@| " + triplet.srcAttr(0) +  " |@| " + triplet.srcAttr(1) + " |@| " + triplet.attr + " |@| "+ triplet.dstAttr(0) + " |@| " + triplet.dstAttr(1) + " |@| " + triplet.dstId)
        Iterator((triplet.dstId, Array(triplet.srcAttr(0) + triplet.attr, triplet.dstAttr(1), triplet.srcId)))
      }
      else if (triplet.srcAttr(0) + triplet.attr  < triplet.dstAttr(0) && triplet.srcAttr(1) + triplet.attr  > triplet.dstAttr(0)) {
        println(triplet.srcId + " |#| " + triplet.srcAttr(0) +  " |#| " + triplet.srcAttr(1) + " |#| " + triplet.attr + " |#| "+ triplet.dstAttr(0) + " |#| " + triplet.dstAttr(1) + " |#| " + triplet.dstId)
        Iterator((triplet.dstId, Array(triplet.srcAttr(0) + triplet.attr, triplet.dstAttr(0), triplet.srcId)))
      }
      else if (triplet.srcAttr(0) + triplet.attr   > triplet.dstAttr(0) && triplet.srcAttr(0) + triplet.attr  < triplet.dstAttr(1)) {
        println(triplet.srcId + " |$| " + triplet.srcAttr(0) +  " |$| " + triplet.srcAttr(1) + " |$| " + triplet.attr + " |$| "+ triplet.dstAttr(0) + " |$| " + triplet.dstAttr(1) + " |$| " + triplet.dstId)
        Iterator((triplet.dstId, Array(triplet.dstAttr(0), triplet.srcAttr(0) + triplet.attr, triplet.srcId)))
      }
      else {
        Iterator.empty
      }

    },

    (a, b) => {
      println("source Id is: " + sourceId)
      println(a.mkString(" ---a--- "))
      println(b.mkString(" ---b--- "))
      if (a(0) < b(0) && a(1) < b(1))
        { a }
      else if (a(0) < b(0) && a(1) >= b(1))
        { Array(a(0), b(1), a(2), b(3)) }
      else if (a(0) >= b(0) && a(1) < b(1))
        { Array(b(0), a(1), b(2), a(3)) }
      else if (a(0) >= b(0) && a(1) >= b(1))
        { b }
      else
        { b }
    }
  )

  println(" ------  mssp output -------- ");
  //val ans = msssp.vertices.map(vertex => "Target Vertex " + vertex._1 + ": distance is " + vertex._2(0) + ", previous node is Vertex " + vertex._2(2) + " second shortest distance is " + " - " + vertex._2(1) + " , " + vertex._2.length)
  //ans.collect().foreach(println)

  println(" ----------------- Printing the final answer -------------- -- ")
  val ans2 = msssp.vertices.map(vertex => "Shortest distances from Source Vertex " + sourceId + " to target landmark vertex: " + vertex._1 + " are  " + vertex._2(0) + " | " + vertex._2(1))
  ans2.collect().foreach(println)
   */

}