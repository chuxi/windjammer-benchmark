#!/usr/bin/env bash

bin=`dirname ${0}`
bin=`cd ${bin}; pwd`
basedir=${bin}/..
LIB_DIR=${basedir}/lib
JARS_DIR=${basedir}/jars
LOG_DIR=${basedir}/logs
CONF_DIR=${basedir}/conf
cd ${basedir}

if [ ! -d ${LOG_DIR} ]; then
  mkdir -p ${LOG_DIR}
fi

export SPARK_HOME=~/svr/spark

${SPARK_HOME}/bin/spark-submit \
  --master yarn \
  --deploy-mode client \
  --num-executors 4 \
  --executor-cores 1 \
  --driver-memory 4g \
  --executor-memory 1g \
  --class org.apache.spark.windjammer.bench.CacheBenchmark \
  ${JARS_DIR}/bench-cache-1.0-SNAPSHOT.jar $@ \
  > ${LOG_DIR}/benchmark.log 2>&1 &

echo $! > pid