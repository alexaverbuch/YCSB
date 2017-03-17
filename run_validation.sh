#!/bin/bash

# cassandra / hbase / gremlin / gremlinkafka
backend=gremlin

# Titan Specific Parametersif backend is gremlin, then locator should point to remote-objects.yaml
locator=/hdd1/ldbc/utilities/titan11/conf/remote-objects.yaml

# conf_path=configuration/ldbc_snb_interactive_SF-0001_titan.properties
conf_path=/home/r32zhou/ldbc/ldbc_snb_interactive_validation/sparksee/readwrite_sparksee--ldbc_driver_config--db_validation.properties
parameters_dir=/home/r32zhou/ldbc/ldbc_snb_interactive_validation/sparksee/validation_set
parameters_csv=/home/r32zhou/ldbc/ldbc_snb_interactive_validation/sparksee/validation_set/validation_params.csv

# Database Implementation
# db=net.ellitron.ldbcsnbimpls.interactive.titan.TitanDb
db=ca.uwaterloo.cs.ldbc.interactive.gremlin.GremlinDb

# jar file for the workload implementation
workload_impl=/home/r32zhou/ldbc/graph-benchmarking/snb-interactive-gremlin/target/snb-interactive-gremlin-1.0-SNAPSHOT-jar-with-dependencies.jar

# if first argument is true, then it will run in debug mode
if [ "$1" = true ] ; then
    JAVA="java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044"
else
    JAVA="java"
fi

exec $JAVA -cp "target/jeeves-0.3-SNAPSHOT.jar:$workload_impl" com.ldbc.driver.Client -w com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcSnbInteractiveWorkload -P $conf_path -p "ldbc.snb.interactive.parameters_dir|$parameters_dir" -p "graphName|$graph_name" -p "locator|$locator" -p "backend|$backend" -db $db -p "ldbc.snb.interactive.update_interleave|1" -vdb $parameters_csv -p consume_updates
