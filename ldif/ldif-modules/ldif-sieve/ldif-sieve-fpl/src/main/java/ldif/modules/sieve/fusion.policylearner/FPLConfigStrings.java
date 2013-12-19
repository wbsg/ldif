package ldif.modules.sieve.fusion.policylearner;

/*
 * Copyright 2013 University of Mannheim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* FLP string constants */
public class FPLConfigStrings 
{
    // specification, attribute names:
    public static String inaName = "name";
    public static String inaDescr = "description";
    public static String inaClass = "class";
    public static String inaMetric = "metric";
    public static String inaError = "error";
    public static String inaID = "id";
    public static String inaNS = "namespace";
    public static String inaVMtx = "valmatrix";
    public static String trueVal = "true";

    // specification, tag names:
    public static String inSelMethod = "SieveFPL/Parameters/SelectionMethod"; // name, error
    public static String inGoldPath = "SieveFPL/Input/GoldStandard";
    public static String inDumpLocation = "SieveFPL/Input/dumpLocation";
    public static String inSieveExec = "SieveFPL/Input/SieveExec";
    public static String inFinalSieveSpec = "SieveFPL/Output/SieveSpec";
    public static String inFusionReport = "SieveFPL/Output/FPLReport";
    public static String inPrefixes = "SieveFPL/Sieve/Prefixes";
    public static String inPrefixesPrefix = "Prefix";
    public static String inQA = "SieveFPL/Sieve/QualityAssessment";
    public static String inFusion = "SieveFPL/Sieve/Fusion"; // name, description
    public static String inClass = "SieveFPL/Sieve/Fusion/Class"; // name
    public static String inProperty = "SieveFPL/Sieve/Fusion/Class/Property"; // name
    // public static String inFusionFunction = "SieveFPL/Sieve/Fusion/Class/Property/FusionFunction"; // class, metric
    public static String inFusionFunction = "FusionFunction"; // class, metric

    // for creating Sieve specifications    
    public static String rootSieve = "Sieve";
    public static String rootFusion = "Fusion";
    public static String rootClass = "Class";
    public static String rootProperty = "Property";

    public static String ffFinalName = "Learnt by FPL";

    // selection methods
    public static String SEL_MIN_ABS_ERR = "MinAbsError";
    public static String SEL_MAX_CORR_VAL = "MaxCorrectValues";
}
