#!/bin/bash

git clone git@github.com:wbsg/ldif.git
cd ldif/ldif
export version=`grep '<ldif.version>' pom.xml | cut -d'>' -f 2 |cut -d'<' -f 1`

export ldifdir="ldif-"${version}
export ldifhadoopdir="ldif-hadoop-"${version}

mkdir ${ldifdir}
mkdir ${ldifhadoopdir}
mkdir ${ldifdir}/bin
mkdir ${ldifhadoopdir}/bin
cp bin/ldif-hadoop* ${ldifhadoopdir}/bin/
sed -i 's/..\/ldif-hadoop-executor\/target/..\/lib/' ${ldifhadoopdir}/bin/*
sed -i 's/ldif-hadoop-executor\\target/lib/' ${ldifhadoopdir}/bin/*

cp bin/ldif* ${ldifdir}/bin
rm ${ldifdir}/bin/ldif-hadoop*
sed -i 's/..\/ldif-singlemachine\/target/..\/lib/' ${ldifdir}/bin/*
sed -i 's/ldif-singlemachine\\target/lib/' ${ldifdir}/bin/*

mkdir ${ldifdir}/lib
mkdir ${ldifhadoopdir}/lib
cp LICENSE ${ldifdir}
cp LICENSE ${ldifhadoopdir}

mkdir ${ldifdir}/resources
cp resources/log4j.properties ${ldifdir}/resources/
mkdir ${ldifhadoopdir}/resources
cp resources/log4j.properties ${ldifhadoopdir}/resources 

mkdir ${ldifdir}/examples
mkdir ${ldifhadoopdir}/examples

cp -r examples/life-science/ examples/music/  examples/lwdm2012/ ${ldifdir}/examples/
cp -r examples/music/ ${ldifhadoopdir}/examples/

mvn install

cp ldif-singlemachine/target/ldif-single-*-jar-with-dependencies.jar ${ldifdir}/lib
cp ldif-hadoop-executor/target/ldif-hadoop-exe-*-jar-with-dependencies.jar ${ldifhadoopdir}/lib

zip -r ${ldifdir}.zip ${ldifdir}
zip -r ${ldifhadoopdir}.zip ${ldifhadoopdir}

mv *.zip ../..
