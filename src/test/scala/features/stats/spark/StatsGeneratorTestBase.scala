package features.stats.spark

import java.io.File
import java.nio.file.{Files, Paths}

import featureStatistics.feature_statistics.DatasetFeatureStatisticsList
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * # ==============================================================================
  * # Licensed under the Apache License, Version 2.0 (the "License");
  * # you may not use this file except in compliance with the License.
  * # You may obtain a copy of the License at
  * #
  * #     http://www.apache.org/licenses/LICENSE-2.0
  * #
  * # Unless required by applicable law or agreed to in writing, software
  * # distributed under the License is distributed on an "AS IS" BASIS,
  * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * # See the License for the specific language governing permissions and
  * # limitations under the License.
  * # ==============================================================================
  */

/**
  * Created by chesterchen on 5/11/18.
  */
abstract class StatsGeneratorTestBase extends FunSuite with BeforeAndAfterAll {

    val appName = "protoGenerator"
    val SPARK_MASTER_URL = "local[2]"
    var sc: SparkContext = _
    var sqlContext: SQLContext = _
    val generator = new FeatureStatsGenerator(DatasetFeatureStatisticsList())
    var spark: SparkSession = _

    override protected def beforeAll(): Unit = {
      val sparkConf = new SparkConf().setMaster(SPARK_MASTER_URL).setAppName(appName)
      //sparkConf.set("spark.sql.session.timeZone", "GMT")

      sc = SparkContext.getOrCreate(sparkConf)
      sqlContext = SqlContextFactory.getOrCreate(sc)
      spark = sqlContext.sparkSession
      spark.sparkContext.setLogLevel("ERROR")
    }

  private[spark] def persistProto(proto: DatasetFeatureStatisticsList, base64Encode: Boolean, file: File ):Unit = {
    if (base64Encode) {
      import java.util.Base64
      val b = Base64.getEncoder.encode(proto.toByteArray)
      import java.nio.charset.StandardCharsets.UTF_8
      import java.nio.file.{Files, Paths}

      Files.write(Paths.get(file.getPath), new String(b, UTF_8).getBytes(UTF_8))
    }
    else {
      Files.write(Paths.get(file.getPath), proto.toByteArray)
    }
  }


  private[spark] def loadProto(base64Encode: Boolean, file: File ): DatasetFeatureStatisticsList = {
    import java.util.Base64
    val bs = Files.readAllBytes(Paths.get(file.getPath))
    val bytes = if (base64Encode) Base64.getDecoder.decode(bs) else bs
    DatasetFeatureStatisticsList.parseFrom(bytes)
  }

  private[spark] def toJson(proto: DatasetFeatureStatisticsList) : String = {
    import scalapb.json4s.JsonFormat
    JsonFormat.toJsonString(proto)
  }

}
