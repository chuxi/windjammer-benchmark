package org.apache.spark.windjammer.bench

import java.io.{FileOutputStream, OutputStream}

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.util.Benchmark

/**
  * Created by king on 17-6-14.
  */
object CacheBenchmark {
  private val dataSize = Map(
//    "small" -> 100000L,
//    "large" -> 1000000L,
    "huge" -> 10000000L,
    "gigantic" -> 100000000L
//    "bigdata" -> 2000000000L
  )

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("CacheBenchmark")
    val spark = SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    val fileOutputStream = new FileOutputStream(s"CacheBenchmark-Read-results.txt")

    Seq("huge", "gigantic").foreach { scale =>
      val pre = scale match {
        case "huge" => "in-memory"
        case "gigantic" => "memory-disk"
      }

      run(spark, scale, s"$pre-stock", fileOutputStream) { (s, t) =>
        val data = s.table(t)
        data.cache()
        data.count()
        data
      }

      run(spark, scale, s"$pre-parquet", fileOutputStream) { (s, t) =>
        val data = s.table(t)
        s.table(t).write.mode("overwrite").parquet(s"/tmp/$t")
        s.read.schema(data.schema)
          .parquet(s"/tmp/$t")
      }

      run(spark, scale, s"$pre-windjammer", fileOutputStream) { (s, t) =>
        val data = s.table(t)
        data.cache()
        data.count()
        data
      }
    }
  }


  def run(spark: SparkSession, scale: String, title: String, out: OutputStream)
         (f: (SparkSession, String) => DataFrame): Unit = {
    val N = dataSize(scale)
    val table = s"uservisits_$scale"
    val benchmark = new Benchmark(s"$title read", N, output = Some(out))
    val data = f(spark, table)
    benchmark.addCase(title, 5) { iter =>
      data.queryExecution.toRdd.foreach(_ => Unit)
    }
    benchmark.run()
    // remove cache
    spark.catalog.clearCache()
  }

}
