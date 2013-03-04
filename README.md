LDIF - Linked Data Integration Framework
====

LDIF translates heterogeneous Linked Data from the Web into a clean, local target representation while keeping track of data provenance.

#### Get Started

To see LDIF in action, please follow these steps:

1. [Download](https://dl.mes-semantics.com/ldif/ldif-0.5.1.zip) the latest release
2. Unzippack the archive and change into the extracted directory ldif-0.5.1
3. Run LDIF on the Music example

        bin/ldif examples/music/light/schedulerConfig.xml    

4. While the example is running (it will take few minutes), check the progress of the scheduled jobs through the status monitor interface, available at [http://localhost:5343](http://localhost:5343).
5. Integration results will be written into integrated_music_light.nq in the working directory


#### Learn More

Learn more about LDIF at [http://ldif.wbsg.de/](http://ldif.wbsg.de/).
