for i in `find ./ -name pom.xml`
do
  cp $i $i.bak
  sed -i 's/>0\.4</>0.5</g' $i
done
