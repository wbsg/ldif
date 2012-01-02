java -server -Xmx2G -Xms256M -Dlog4j.configuration=file:resources\log4j.properties -cp ldif-singlemachine\target\ldif-single-0.4-jar-with-dependencies.jar ldif.local.IntegrationJob $*
