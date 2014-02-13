This folder contains data and specification files for the multilingual DBpedia example. 
The goal is to fuse data for the same city from multiple language editions of DBpedia (hence, Wikipedia).

DBpedia project: http://dbpedia.org/

*** Data ***
dumps-3cities - data and provenance dumps for 3 capital European cities
dumps-nl.zip - data and provenance dumps for 542 Dutch cities
gold - gold standard based on geonames.org for both dumps, Dutch cities dump is zipped

*** Sieve ***
sieve - specification file for Sieve, to test it run
bin\ldif examples\dbpedia-multilang\schedulerConfig.xml


*** Sieve Fusion Policy Learner (FPL) ***
SieveFPL.xml - specification file for the Fusion Policy Learner Sieve extension, to test it run
java -jar lib\ldif-sieve-fpl-0.1.1-jar-with-dependencies.jar examples\dbpedia-multilang\SieveFPL.xml

FLP will generate 
* the optimal Sieve specification,
* FLP report.

To test Sieve with the optimal specification, change the sieve spec folder (<sieve> element) in the integrationJob.xml.

To test FLP and Sieve with the Dutch city data, perform the following steps:
* unpack dumps-nl.zip and gold\cities1000-Netherlands.gold.nt
* change in schedulerConfig.xml, <dumpLocation> and in SieveFPL.xml, <dumpLocation> to dumps-nl
* change in SieveFPL.xml, <GoldStandard> to gold\cities1000-Netherlands.gold.nt
* run the FPL




