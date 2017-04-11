package com.cloudywaters

import scala.collection.JavaConverters._
import scala.io.Source
import java.io.InputStream

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.ListObjectsRequest
import org.apache.spark.sql.SparkSession

object SparkS {
  //  val ctx = new Spark(new SparkConf().setAppName("test").setMaster("local[*]"))
  val ctx = SparkSession
    .builder()
    .appName("Spark SQL basic example")
    .config("spark.some.config.option", "some-value")
    .getOrCreate()
}

object First {

  def main(args: Array[String]) {
    import SparkS.ctx.implicits._

    val bucket: String = "588439395328-datalake-v1"
    val request = new ListObjectsRequest()
    request.setBucketName(bucket)
    request.setMaxKeys(10)

    val region: Regions = Regions.US_WEST_2

    def s3: AmazonS3 = AmazonS3ClientBuilder.standard()
      .withRegion(region)
      .withCredentials(new ProfileCredentialsProvider("cloudywaters"))
      .build()

    var inputLinesRDD_raw: RDD[String] = Spark.ctx.emptyRDD[String]

    var listing = s3.listObjects(request)
    var proceed = true
    while (proceed) {
      if (listing.getObjectSummaries.isEmpty) {
        proceed = false
      } else {
        val s3FileKeys = listing.getObjectSummaries.asScala.map(_.getKey).toList
        println(s3FileKeys)
        val inputLines = Spark.ctx.parallelize(s3FileKeys).flatMap {
          key =>
            Source.fromInputStream(s3.getObject(bucket, key).getObjectContent: InputStream).getLines
        }
        inputLinesRDD_raw = inputLinesRDD_raw.union(inputLines)
        listing = s3.listNextBatchOfObjects(listing)
      }
    }

    val jsonDf = SparkS.ctx.read.json(inputLinesRDD_raw)
    println(jsonDf)
  }
}