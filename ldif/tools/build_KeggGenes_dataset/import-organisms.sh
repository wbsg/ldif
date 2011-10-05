#!/bin/bash

for i in ../genes/organisms/*/*.ent; do php db_import_kegg_genes.php $i; done
for i in ../genes/organisms_est/*/*.ent; do php db_import_kegg_genes.php $i; done
for i in ../genes/organisms_new/*/*.ent; do php db_import_kegg_genes.php $i; done
