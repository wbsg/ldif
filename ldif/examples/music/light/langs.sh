grep -P "@en <\S+dbpedia.3" dumps/dbpedia.3.nq | sed -r "s|dbpedia.3|dbpedia.en|" > dumps/dbpedia.en.nq
grep -P "@fr <\S+dbpedia.3" dumps/dbpedia.3.nq | sed -r "s|dbpedia.3|dbpedia.fr|" > dumps/dbpedia.fr.nq
grep -P "@es <\S+dbpedia.3" dumps/dbpedia.3.nq | sed -r "s|dbpedia.3|dbpedia.es|" > dumps/dbpedia.es.nq

grep -P "@en <\S+dbpedia.2" dumps/dbpedia.2.nq | sed -r "s|dbpedia.2|dbpedia.en|" >> dumps/dbpedia.en.nq
grep -P "@fr <\S+dbpedia.2" dumps/dbpedia.2.nq | sed -r "s|dbpedia.2|dbpedia.fr|" >> dumps/dbpedia.fr.nq
grep -P "@es <\S+dbpedia.2" dumps/dbpedia.2.nq | sed -r "s|dbpedia.2|dbpedia.es|" >> dumps/dbpedia.es.nq

grep -P "@en <\S+dbpedia.1" dumps/dbpedia.1.nq | sed -r "s|dbpedia.1|dbpedia.en|" >> dumps/dbpedia.en.nq
grep -P "@fr <\S+dbpedia.1" dumps/dbpedia.1.nq | sed -r "s|dbpedia.1|dbpedia.fr|" >> dumps/dbpedia.fr.nq
grep -P "@es <\S+dbpedia.1" dumps/dbpedia.1.nq | sed -r "s|dbpedia.1|dbpedia.es|" >> dumps/dbpedia.es.nq


