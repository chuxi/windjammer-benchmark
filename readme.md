## Benchmark

sqls for registering test data set, which generated by HiBench Scan Prepare, into hive tables.

```sql

DROP TABLE IF EXISTS uservisits_large;

CREATE EXTERNAL TABLE uservisits_large (sourceIP STRING,destURL STRING,visitDate STRING,adRevenue DOUBLE,userAgent STRING,countryCode STRING,languageCode STRING,searchWord STRING,duration INT ) ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' STORED AS  SEQUENCEFILE LOCATION 'hdfs://wj-02:9000/HiBench/Scan/large/Input/uservisits';

```

you can replace `large` into `tiny | small | large | huge | gigantic | bigdata`, which is listed under `http://wj-02:50070/explorer.html#/HiBench/Scan`.

Then you can run sql in spark-sql environment.
