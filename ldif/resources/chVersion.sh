for i in `find ./ -name pom.xml`
do
  cp $i $i.bak
  sed -i 's/>0\.5</>0.6</g' $i
done
