for i in `find ./ -name pom.xml`
do
  cp $i $i.bak
  sed -i 's/0.3.1/0.3.1/g' $i
done
