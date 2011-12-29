java -server -Xmx4G -Xms256M -Dlog4j.configuration=file:resources\log4j.properties -jar ldif-hadoop-executor\target\ldif-hadoop-exe-0.3.1-jar-with-dependencies.jar ldif.hadoop.HadoopIntegrationJob $*
