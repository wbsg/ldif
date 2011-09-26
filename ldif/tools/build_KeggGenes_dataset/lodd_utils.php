<?php

/**
 * The LODD Utils includes the database configuration and
 * provides functions for handling XML and strings.
 *
 * @author	Anja Jentzsch <mail@anjajentzsch.de>
 */

$lodd_datasets = array("diseasome", "dailymed", "drugbank", "sider", "stitch", "medicare");

$database_dailymed = "lodd_dailymed";
$database_drugbank = "lodd_drugbank";
$database_diseasome = "lodd_diseasome";
$database_sider = "lodd_sider";

$config_file = "config.php";

if (!file_exists($config_file)) {
	// die ("Copy config/config.php.sample to config/config.php and adjust your settings.");
}
require_once($config_file);

$parent_subtree = array();

/**
 * Searches haystack for needle and 
 * returns an array of the key path if 
 * it is found in the (multidimensional) 
 * array, FALSE otherwise.
 *
 * @mixed array_searchRecursive ( mixed needle, 
 * array haystack [, bool strict[, array path]] )
 */
function array_searchRecursive( $needle, $haystack, $strict=false, $path=array() ) {
	global $parent_subtree;

	if(!is_array($haystack)) {
        return false;
    }
    foreach( $haystack as $key => $val ) {
        if( is_array($val) && $subPath = array_searchRecursive($needle, $val, $strict, $path) ) {
            $path = array_merge($path, array($key), $subPath);
            return $path;
        } else if (((!$strict && (strcasecmp($val, $needle) == 0)) || ($strict && $val === $needle))) {
            if ($haystack["name"] != "LINKHTML") {
	        	$path[] = $key;
				$parent_subtree = $haystack;
/*
				if (!is_array($haystack["child"])) {
					return false;
				} else {
		            return $path;
				}
				*/
				return $path;
            } else {
            	return false;
            }
        }
    }
    return false;
}

function array_searchRecursive_loose($needle, $haystack, $strict=false, $path=array(), $exact = false) {
	global $parent_subtree;

	if( !is_array($haystack) ) {
        return false;
    }
    foreach( $haystack as $key => $val) {
        if( is_array($val) && $subPath = array_searchRecursive_loose($needle, $val, $strict, $path)) {
            $path = array_merge($path, array($key), $subPath);
            return $path;
        } else if( (!$strict && (str_starts_with($val, $needle))) || ($strict && (str_starts_with($val, $needle)))) {
            $path[] = $key;
			$parent_subtree = $haystack;
            return $path;
        } else if (!is_array($val)) {
        	if (preg_match("/".$needle."/i", $val, $match)) {
	            $path[] = $key;
				$parent_subtree = $haystack;
	            return $path;
        	}
        }
    }
    return false;
}

function str_starts_with($haystack, $needle) {
	if (strpos($haystack, $needle) === 0) {
		return true;
	} else {
		return false;
	}
}

class XMLParser {
	var $filename;
	var $xml;
	var $data;

	function XMLParser($xml_file) {
		$this->filename = $xml_file;
		$this->xml = xml_parser_create();
		xml_set_object($this->xml, $this);
		xml_set_element_handler($this->xml, 'startHandler', 'endHandler');
		xml_set_character_data_handler($this->xml, 'dataHandler');
		$this->parse($xml_file);
	}

	function parse($xml_file) {
		if (!($fp = fopen($xml_file, 'r'))) {
			die('Cannot open XML data file: '.$xml_file);
			return false;
		}
		
		$bytes_to_parse = 512;

		while ($data = fread($fp, $bytes_to_parse)) {
			$parse = xml_parse($this->xml, $data, feof($fp));

			if (!$parse) {
				die(sprintf("$xml_file - XML error: %s at line %d",
				xml_error_string(xml_get_error_code($this->xml)),
				xml_get_current_line_number($this->xml)));
				xml_parser_free($this->xml
				);
			}
		}

		return true;
	}

	function startHandler($parser, $name, $attributes) {
		$data['name'] = $name;
		if ($attributes) {
			$data['attributes'] = $attributes;
		}
		$this->data[] = $data;
	}

	function dataHandler($parser, $data) {
		if (strpos($data, "Patients with severely impaired renal function") !== false) {
			echo "";
		}
		if ($data = trim($data)) {
			$index = count($this->data) - 1;
			// ANja $this->data[$index]['content'] = $data;
			$this->data[$index]['content'] .= $data;
		}
	}

	function endHandler($parser, $name) {
		if (count($this->data) > 1) {
			$data = array_pop($this->data);
			$index = count($this->data) - 1;
			$this->data[$index]['child'][] = $data;
		}
	}
}

if (false === function_exists('lcfirst')) {
	/**
     * Make a string's first character lowercase
     *
     * @param string $str
     * @return string the resulting string.
     */
	function lcfirst( $str ) {
		$str[0] = strtolower($str[0]);
		return (string)$str;
	}
}

function camelCase($text) {
	return str_replace(" ", "", lcfirst(ucwords(strtolower(trim(str_replace("_", " ", $text))))));
}

/**
 * Looks for the first occurence of $needle in $haystack and replaces it with $replace.
 *
 * @param string $needle
 * @param string $replace
 * @param string $haystack
 * @return string
 */
function str_replace_once($needle, $replace, $haystack) { 
   $pos = strpos($haystack, $needle); 
   if ($pos === false) { 
       // Nothing found 
       return $haystack; 
   } 
   return substr_replace($haystack, $replace, $pos, strlen($needle)); 
}

function getXmlFields($searchString, $level = false) {
	global $fields;
	global $xmlDrugFile;
	global $errors;
	
	$searchString = str_replace("_", " ", $searchString);
	$searchpath = array_searchRecursive($searchString, $xmlDrugFile->data[0]);
	if ($searchpath !== false ) {
		$subarray = $xmlDrugFile->data[0];
		if ($level == "-1") {
			$searchDepth = sizeof($searchpath)-3;
		} else {
			$searchDepth = sizeof($searchpath)-2;
		}
		for ($i = 0; $i < $searchDepth; $i = $i+1) {
			$subarray = $subarray[$searchpath[$i]];
		}
		$i = $searchpath[$i]+1;
		for ($k = $i; $k < sizeof($subarray); $k=$k+1) {
			if ($subarray[$k]["name"] == "TITLE") {
				getXmlContent($subarray[$k]["content"], ":");
			} else if ($subarray[$k]["name"] == "TEXT") {
				$subsubarray = $subarray[$k]["child"];
				for ($j = 0; $j < sizeof($subsubarray); $j=$j+1) { 
					if ($subsubarray[$j]["name"] == "PARAGRAPH") {
						getXmlContent($subsubarray[$j]["content"]);
					} else {
						$errors[$file] .= " $searchString";
					}
				}
			} else if ($subarray[$k]["name"] == "COMPONENT") {
				$subsubarray = $subarray[$k]["child"];
				for ($j = 0; $j < sizeof($subsubarray); $j=$j+1) { 
					if ($subsubarray[$j]["name"] == "SECTION") {
						for ($l = 0; $l < sizeof($subsubarray[$j]["child"]); $l=$l+1) { 
							$temp = $subsubarray[$j]["child"][$l];
							if ($temp["name"] == "TITLE") {
								getXmlContent($temp["content"], ":");
							} else if ($temp["name"] == "TEXT") {
								for ($m = 0; $m < sizeof($temp["child"]); $m = $m + 1) {
									if ($temp["child"][$m]["name"] == "PARAGRAPH") {
										getXmlContent($temp["child"][$m]["content"]);
									}
								}
							}  else if ($temp["name"] == "COMPONENT") {
								for ($n = 0; $n < sizeof($temp["child"]); $n = $n + 1) {
									$temp1 = $temp["child"][$n];
									if ($temp1["name"] == "SECTION") {
										for ($o = 0; $o < sizeof($temp1["child"]); $o = $o+1) { 
											$temp2 = $temp1["child"][$o];
											if ($temp2["name"] == "TITLE") {
												getXmlContent($temp2["content"], ":");
											} else if ($temp2["name"] == "TEXT") {
												for ($m = 0; $m < sizeof($temp2["child"]); $m = $m + 1) {
													if ($temp2["child"][$m]["name"] == "PARAGRAPH") {
														getXmlContent($temp2["child"][$m]["content"]);
													}
												}
											}
										}
									} else {
										$errors[$file] .= " $searchString";
									}
								}
							}
						}
					} else {
						$errors[$file] .= " $searchString";
					}
				}
			}				
		}
	}
}
function getXmlContent($array, $divider = false) {
	global $fields;

	if ($array) {
		$content = $array;
		if ($divider) {
			$content .= $divider;
		}
		if (sizeof($fields) > 0) {
			if (!$divider) {
				$fields[sizeof($fields)-1] .= " $content";
			} else {
				$fields[sizeof($fields)-1] .= "<br/>$content";
			}
		} else {
			$fields[] = $content;
		}
	}
}

?>