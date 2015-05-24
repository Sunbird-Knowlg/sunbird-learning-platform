package org.ekstep.ilimi.analytics.model

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.ekstep.ilimi.analytics.conf.AppConf
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.hive.HiveContext

trait BaseModel extends Serializable {
    
    var location:String = null;
    var sc: SparkContext = null;
    var hiveCtx: HiveContext = null;
    
    def validate(args: Array[String]) : Boolean;
    
    def compute(args: Array[String]);
    
    def initializeSparkContext(location: String) = {
        this.location = location;
        val conf = new SparkConf().setMaster("local").setAppName("TestSparkDriver");
        this.sc = new SparkContext(conf);
        if("S3".equals(location)) {
            this.sc.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", AppConf.getConfig("s3_aws_key"));
            this.sc.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", AppConf.getConfig("s3_aws_secret"));
        }
        this.hiveCtx = new HiveContext(this.sc);
        Console.println("### Spark Context instantiated ###");
    }
    
    def getSparkContext() : SparkContext  = {
        this.sc;
    }
    
    def closeSparkContext() {
        sc.stop();
    }
    
    def getPath(relPath: String) : String = this.location match {
        case "S3" => AppConf.getConfig("s3_bucket") + relPath;
        case _ => relPath
    } 
        
    def loadInput(input: String, table: String) {
        Console.println("### Fetching Input:" + getPath(input) + " ###");
        val events = hiveCtx.jsonFile(getPath(input)).persist();
        Console.println("### Data fetched. # of records - " + events.count() + " ###");
        events.registerTempTable(table);
    }
    
    def queryData(hiveSql: String) : DataFrame = {
        hiveCtx.sql(hiveSql);
    }

}