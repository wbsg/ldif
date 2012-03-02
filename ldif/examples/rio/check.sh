mkdir tmp
sort integrated.nq > tmp/obtained
cat correct/fused_correct.nq correct/quality_correct.nq | sort > tmp/desired
echo "comparing obtained with desired triples"
diff tmp/obtained tmp/desired 
rm tmp/obtained tmp/desired
rmdir tmp
