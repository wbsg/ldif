for i in `find ./ -name pom.xml`
do
  cp $i $i.bak
  sed -i 's/>0\.5</>0.5.1</g' $i
done
