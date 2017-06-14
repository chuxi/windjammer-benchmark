package org.apache.spark.windjammer.bench

import java.io.FileOutputStream

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.util.Benchmark

/**
  * Created by king on 17-6-14.
  */
object CacheBenchmark {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Usage: CacheBenchmark [large | huge | gigantic | bigdata]")
      sys.exit(-1)
    }
    val scale = args(0)

    val sparkConf = new SparkConf().setAppName("CacheBenchmark")
    val spark = SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    runScale(spark, scale)
  }


  def runScale(spark: SparkSession, scale: String): Unit = {
    // we get this from spark-sql 'select count(*) from uservisits_huge;'
    val N = 9994452L

    val targetTable = s"uservisits_$scale"

    val stockData = spark.sql(s"select * from $targetTable")
    stockData.cache()
    // it makes the cache work
    stockData.count()

    spark.table(targetTable)
      .createOrReplaceTempView(s"wjm_$targetTable")
    spark.sql(s"cache table wjm_$targetTable")

    val fileOutputStream = new FileOutputStream(s"CacheBenchmark-$scale-Read-results.txt")

    val benchmark = new Benchmark(s"$scale table cache read", N,
      output = Some(fileOutputStream))

    benchmark.addCase("inMemoryCache", 5) { iter =>
      stockData.queryExecution.toRdd.foreach(_ => Unit)
    }

    benchmark.addCase("windjammer", 5) { iter =>
      spark.table(s"wjm_$targetTable").queryExecution.toRdd.foreach(_ => Unit)
    }

    benchmark.run()
  }

}
