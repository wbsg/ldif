<?php

/**
* Creates MySQL database from KEGG GENES
*
* Exemplary entry files:
*	ftp://ftp.genome.jp/pub/kegg/genes/organisms/hsa/H.sapiens.ent
*	ftp://ftp.genome.jp/pub/kegg/genes/organisms/mmu/M.musculus.ent
*
* @author	Christian Becker <chris@beckr.org>
*/

require_once("lodd_utils.php");

$known_fields = array("ENTRY", "NAME", "DEFINITION", "POSITION", "DBLINKS", "AASEQ", "NTSEQ", "MOTIF", "STRUCTURE", "ORTHOLOGY", "PATHWAY", "CLASS", "DISEASE");

function execSQL($sql_query) {
	if (!mysql_query($sql_query)) {
		if (strpos(mysql_error(), "Duplicate entry") === false) {
			die("[DIE] ". mysql_error() . " - query: ".$sql_query . "\n");
		}
	}
}

if ($argc < 2) {
	echo "Usage: php db_import_kegg_genes.php <data.ent>\n";
	die();
}

$file = $argv[1];

$database = "neurobase_kegg_genes_d2r";

mysql_connect ($host, $user, $password) or die ("Database connection could not be established.\n");
mysql_select_db ($database);
if (!mysql_select_db ($database)) {
	$sql_query = "CREATE DATABASE ".$database;
	if (!mysql_query ($sql_query)) {
		die(mysql_error() . " - query: ".$sql_query . "\n");
	}
	mysql_select_db ($database) or die("Database selection could not be established.\n");
}

// execSQL("DROP TABLE IF EXISTS `genes`");
execSQL("CREATE TABLE IF NOT EXISTS `genes` (
	id varchar(30) NOT NULL,
	combinedid varchar(255) NULL,
	type varchar(10) NOT NULL,
	organism varchar(255) NOT NULL,
	position varchar(255) NULL,
	definition varchar(255) NULL,
	aaseq blob NULL,
	ntseq blob NULL,
	PRIMARY KEY  (id),
	INDEX (type),
	INDEX (organism)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;");

foreach (array("names", "motifs", "classes", "pdbstructures") as $rel) {
//	execSQL("DROP TABLE IF EXISTS `genes_$rel`");
	execSQL("CREATE TABLE IF NOT EXISTS `genes_$rel` (
	genes_id varchar(30) NOT NULL,
	name varchar(255) NOT NULL,
	INDEX (genes_id),
	INDEX (name)
	) ENGINE=MyISAM DEFAULT CHARSET=utf8;");
}

foreach (array("diseases", "pathways", "orthologies") as $rel) {
//	execSQL("DROP TABLE IF EXISTS `$rel`");
	execSQL("CREATE TABLE IF NOT EXISTS `$rel` (
		id varchar(30) NOT NULL,
		name varchar(500) NOT NULL,
		PRIMARY KEY  (id)
	) ENGINE=MyISAM DEFAULT CHARSET=utf8;");

//	execSQL("DROP TABLE IF EXISTS `genes_$rel`");
	execSQL("CREATE TABLE IF NOT EXISTS `genes_$rel` (
		genes_id varchar(30) NOT NULL,
		{$rel}_id varchar(30) NOT NULL,
		INDEX (genes_id),
		INDEX ({$rel}_id)
	) ENGINE=MyISAM DEFAULT CHARSET=utf8;");
}

foreach (array("genes", "orthologies") as $rel) {
//	execSQL("DROP TABLE IF EXISTS `{$rel}_dblinks`");
	execSQL("CREATE TABLE IF NOT EXISTS `{$rel}_dblinks` (
		id varchar(30) NOT NULL,
		db varchar(15) NOT NULL,
		external_id varchar(30) NOT NULL,
		UNIQUE (id, db, external_id)
	) ENGINE=MyISAM DEFAULT CHARSET=utf8;");
}

$file_handle = fopen($file, "r");
if (!$file_handle) {
	die ("File not found ".$file);
}
else echo("Importing file $file \n"); 

$gene = null;
$gene_type = null;
$gene_organism = null;
$gene_position = null;
$gene_definition = null;
$gene_aaseq = null;
$gene_ntseq = null;
$lineNo = 0;
$geneNo = 0;
$shortcodeA = explode("/",$file);
$shortcode = $shortcodeA[3];

while (!feof($file_handle)) {
	$line = fgets($file_handle);
	$lineNo++;
	if ($lineNo % 1000 == 0) {
		echo "$lineNo lines; $geneNo genes\n";
	}

	if ($line == "///\n") {
		/* end of entry */
		/*switch ($gene_organism) {
			case "H.sapiens":
				$shortcode = "hsa";
				break;
			case "M.musculus":
				$shortcode = "mmu";
				break;
		}*/
		$query = "INSERT INTO `genes` (id, combinedid, type, organism, position, definition, aaseq, ntseq) VALUES ('$gene', '$gene', '$gene_type', '$gene_organism', " . ($gene_position ? "'$gene_position'" : "NULL") . ", " . ($gene_definition ? "'$gene_definition'" : "NULL") . ", " . ($gene_aaseq ? "'$gene_aaseq'" : "NULL") . ", " . ($gene_ntseq ? "'$gene_ntseq'" : "NULL") . ");";
		//echo $query."\n";
		execSQL($query);		
		$geneNo++;
		//echo $shortcode . $gene. "\n";
		$gene = null;
		$gene_type = null;
		$gene_organism = null;
		$gene_position = null;
		$gene_definition = null;
		$gene_aaseq = null;
		$gene_ntseq = null;
		continue;
	}
		
	/*
	 * Extract data
	 * [1] Predicate
	 * [2] Object
	 * [3] 	first word
	 * [4] 	rest
	 */
	if (!preg_match("/^\s{0,2}(\w+)?\s+(([A-Za-z0-9\-_.]+):?\s*(.*)|.*)\n$/", $line, $match)) {	
		//die("[DIE] Unable to parse line $lineNo: $line\n");
	}
	for ($i=1; $i<count($match); $i++) {
		$match[$i] = mysql_escape_string($match[$i]);
	}
	if ($match[1] && in_array($match[1], $known_fields)) {
		/* New field type */
		$current_field = $match[1];
	} else if (preg_match("/^[A-Z]{5,}/", $match[1])) {
		//die("[DIE] Unknown field type in $lineNo: $line\n");
	}
	/* else: continuation from previous line */

	switch ($current_field) {
		case 'ENTRY':
			$gene = $shortcode.':'.$match[3];
			list($gene_type, $gene_organism) = preg_split("/\s+/", $match[4]);
			break;
		case 'DEFINITION':
			$gene_definition = ($gene_definition ? $gene_definition . "\n" : "") . $match[2]; // support multi-line definition
			break;
		case 'POSITION':
			if ($gene_position != null) { die("Position already set in $lineNo\n"); };
			$gene_position = $match[2];
			break;
		case 'DBLINKS':
			foreach (explode(" ", $match[4]) as $external_id) {
				execSQL("INSERT INTO `genes_dblinks` (id, db, external_id) VALUES ('$gene', '{$match[3]}','$external_id');");
			}
			break;
		case 'NAME':
			foreach (explode(", ", $match[2]) as $name) {
				execSQL("INSERT INTO `genes_names` (genes_id, name) VALUES ('$gene', '$name');");
			}
			break;
		case 'MOTIF':
			execSQL("INSERT INTO `genes_motifs` (genes_id, name) VALUES ('$gene', '{$match[2]}');");
			break;
		case 'CLASS':
			execSQL("INSERT INTO `genes_classes` (genes_id, name) VALUES ('$gene', '{$match[2]}');");
			break;
		case 'STRUCTURE':
			foreach (explode(" ", $match[2]) as $structure) {
				if ($structure != "PDB:") {
					execSQL("INSERT INTO `genes_pdbstructures` (genes_id, name) VALUES ('$gene', '$structure');");
				}
			}
			break;
		case 'AASEQ':
			$gene_aaseq = ($gene_aaseq ? $gene_aaseq . "\n" : "") . $match[2]; // support multi-line sequence
			break;
		case 'NTSEQ':
			$gene_ntseq = ($gene_ntseq ? $gene_ntseq . "\n" : "") . $match[2]; // support multi-line sequence
			break;
		case 'ORTHOLOGY':
			switch ($current_field) {
				case 'ORTHOLOGY':
					$table = 'orthologies';
					break;
			}
			preg_match("/^(.*?)(?:\s+\[(.*?):(.*)\])?$/", $match[4], $matchParts);
			if (strpos($matchParts[1], "[" !== FALSE)) {
				die("[DIE] More than one external datasource on line $lineNo: $line\n");
			}
			if (count($matchParts) > 2) {
				foreach (explode(" ", $matchParts[3]) as $externalId) {
					execSQL("INSERT INTO `{$table}_dblinks` (id, db, external_id) VALUES ('{$match[3]}','{$matchParts[2]}','{$externalId}');");
				}
			}
			execSQL("INSERT INTO `$table` (id, name) VALUES ('{$match[3]}','{$matchParts[1]}');");
			execSQL("INSERT INTO `genes_$table` (genes_id, {$table}_id) VALUES ('$gene','{$match[3]}');");
			break;
		case 'PATHWAY':
			execSQL("INSERT INTO `pathways` (id, name) VALUES ('{$match[3]}','{$match[4]}');");
			execSQL("INSERT INTO `genes_pathways` (genes_id, pathways_id) VALUES ('$gene','{$match[3]}');");
			break;
		case 'DISEASE':
			execSQL("INSERT INTO `diseases` (id, name) VALUES ('{$match[3]}','{$match[4]}');");
			execSQL("INSERT INTO `genes_diseases` (genes_id, diseases_id) VALUES ('$gene','{$match[3]}');");
			break;
		default:
			if (trim($line) != "") {
				die("[DIE] Unrecognized line $lineNo: " . $line . "\n");
			}
	}
}

fclose($file_handle);

?>
