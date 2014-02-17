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

import org.semanticweb.yars.nx.parser.NxParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

import static ldif.modules.sieve.fusion.policylearner.FPLConfigStrings.*;
import static ldif.modules.sieve.fusion.policylearner.XMLUtils.*;

/* Learns the optimal fusion function per property from the lists in FLP input specification.
   Creates the final Sieve specifications with optimal functions and the FLP report with detailed information per each fusion function.
*/
public class SimpleLearner
{
    private FPLConfig input;

    // filenames for property and config files
    private String integrationPropertiesText =
            "discardFaultyQuads=false\nvalidateSources=false\nrewriteURIs=false\noutputQualityScores=false\nqualityFromProvenanceOnly=false\nrunStatusMonitor=false\noutput=fused-only";
    private String integrationPropertiesFile = "integration.properties.temp";
    private String schedulerPropertiesText = "oneTimeExecution = true\ndiscardFaultyQuads=true\nrunStatusMonitor=false";
    private String schedulerPropertiesFile = "scheduler.properties.temp";
    private String schedulerConfigFile = "schedulerConfig.temp.xml";
    private String integrationJobFile = "integrationJob.temp.xml";

    // final FPL report
    private PrintWriter report = null;

    // Generate per property per fusion function spec files into path
    public boolean learn(FPLConfig _input)
    {
        try
        {
            // map to collect temporary files names:
            Map<String, String> tempFiles = new HashMap<String, String>();

            input = _input;
            String path = input.getWorkingDir();

            report = new PrintWriter(input.getWorkingDir()+input.getFusionReport(), "UTF-8");
            report.println("== FUSION POLICY LEARNING REPORT ==");
            report.println();
            report.println("Gold standard: " + input.getGoldPath());
            report.println("Final Sieve specification: " + input.getFinalSieveSpec());
            report.println("Selection method: " + input.getSelectionMethod());
            if (input.ifMaxCorrectValues()) report.println("Error threshold: " + input.getSelectionError());
            else report.println("Results for the alternative method " + SEL_MAX_CORR_VAL + " are reported with the default error threshold of " + input.getSelectionError());
            report.println();

            File theDir = new File(path);
            if (!theDir.exists()) return false;

            // create integration property file:
            writeStringToFile(path+integrationPropertiesFile,integrationPropertiesText);
            tempFiles.put(path+integrationPropertiesFile, null);
            // create scheduler property file:
            writeStringToFile(path+schedulerPropertiesFile,schedulerPropertiesText);
            tempFiles.put(path+schedulerPropertiesFile, null);
            // path to integration job file:
            String integrationJob = path+integrationJobFile;
            // create scheduler config file:
            Node sc = createSchedulerConfig(input.getDumpLocation());
            printNode(sc, path+schedulerConfigFile);
            tempFiles.put(path+schedulerConfigFile, null);
            tempFiles.put(path+integrationJobFile, null);

            // LDIF dir as file:
            File ldif = new File(input.getLDIFfDir());
            // prepare command line arguments:
            String[] cmd = {input.getSieveExec(), path+schedulerConfigFile};

            // generated a Sieve spec for each class->property->fusion function
            Node finalSpec = createFinalSieveSpec();
            int propCnt = 0;
            for (String prop : input.getProperties().keySet())
            {
                System.out.println("Learning an optimal fusion function for "+prop+" property...");

                // int-->ff map
                NodeList ff = input.getProperties().get(prop);
                Map<Integer,Node> ffmap = new HashMap<Integer,Node>();
                for (int i = 0; i < ff.getLength(); i++)
                {
                    ffmap.put(i,ff.item(i));
                }

                // map for collecting fusion function values:
                Map<String,ValueList> vals = new HashMap<String,ValueList>();
                // for each fusion function:
                for (int i = 0; i < ffmap.size(); i++) // (int i = 0; i < ff.getLength(); i++)
                {
                    String id = propCnt+"-"+i;

                    // create Sieve spec for a single fusion function:
                    Node n = createSieveSpecTempl(prop, ffmap.get(i),i); // (prop, ff.item(i),i);
                    String sieveSpec = path+"sieve-"+id+".xml";
                    printNode(n, sieveSpec);
                    tempFiles.put(sieveSpec, null);
                    // create integration job (replacing the same file):
                    String output = "output-"+id+".nq.temp";
                    tempFiles.put(path+output, null);
                    Node ij = createIntegrationJob(output, "sieve-"+id+".xml", input.getDumpLocation());
                    printNode(ij, path+integrationJobFile);
                     // run Sieve from command line:
                    try
                    {
                        System.out.println("Getting values for "+printNodeToStr(ffmap.get(i))+"...");
                        Process proc = Runtime.getRuntime().exec(cmd, null, ldif);
                        // int exitVal = proc.waitFor(); - hanging
                        InputStream in = proc.getInputStream();
                        InputStream err = proc.getErrorStream();
                        BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String line = null;
                        while ((line = out.readLine()) != null) {}

                        // --> Sieve output for FF i generated

                        // Collect fusion function values:
                        FileInputStream is = new FileInputStream(path+output);
                        NxParser nxp = new NxParser(is); // https://code.google.com/p/nxparser/
                        while (nxp.hasNext())
                        {
                            org.semanticweb.yars.nx.Node[] ns = nxp.next();
                            if (ns.length == 4)
                            {
                                String subj = ns[0].toString();
                                String obj = ns[2].toString();
                                String pr = WikiLangPrefix(ns[3].toString());
                                if(!vals.containsKey(subj)) vals.put(subj,new ValueList(ffmap.size()));
                                vals.get(subj).set(i,obj,pr);
                            }
                        }
                        is.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                // load gold standard:
                GoldStandard gold = new GoldStandard();
                gold.load(input.getFullName(prop), input.getWorkingDir() + input.getGoldPath(), vals);
                // reporting:
                report.println("*** Learning an optimal fusion function for "+prop+" property ***");
                report.println("Number of gold standard values = "+gold.getSize());
                String type = "NOMINAL";
                if (gold.isNumeric()) type  = "NUMERIC";
                report.println("According to the gold standard, " +prop+" is " + type);

                // print fusion function list:
                report.println();
                report.println("Pool of fusion functions:");
                for (int i = 0; i < ffmap.size(); i++)
                {
                    report.println(i + " : " + printNodeToStr(ffmap.get(i)));
                }
                // learning:
                int best = -1;
                if (gold.isNumeric()) best = learnNumeric(vals, ffmap.size());
                else best = learnNominal(vals, ffmap.size());
                report.println();

                // add the Property/FusionFunction node to the final spec
                if (best != -1) addPropToFinalSieveSpec(finalSpec, prop, ffmap.get(best));

                // printing the value matrix
                if (input.isPrintValueMatrix())
                {
                    report.println("Value matrix:");
                    for (String c : vals.keySet())
                    {
                        report.print(c);
                        for (int i = 0; i < ffmap.size(); i++)
                        {
                            report.print("\t"+vals.get(c).getVal(i)+" ("+vals.get(c).getProv(i)+")");
                        }
                        report.print("\t"+vals.get(c).getGold()+" (gold)\n");
                    }
                    report.println();
                }

                propCnt++;
            }
            System.out.println("Creating the final Sieve spec...");
            // create the final spec
            printNode(finalSpec.getOwnerDocument(), input.getWorkingDir()+input.getFinalSieveSpec());
            report.close();

            // delete temp files:
            for (String fn : tempFiles.keySet())
            {
                File file = new File(fn);
                file.delete();
            }

            System.out.println("Learning completed.");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /* ======= LEARNING ======= */
    //
    private int learnNumeric(Map<String,ValueList> data, int size)
    {
        // FF_SIZE --> size, replaced vals and gold

        int[] correct_counts = new int[size];
        double[] abs_err = new double[size];
        double[] cnt = new double[size];
        for (int i = 0; i < size; i++)
        {
            correct_counts[i] = 0;
            abs_err[i] = 0.0;
            cnt[i] = 0.0;
        }
        // calculate error per feature
        for (String c : data.keySet())
        {
            double gold = data.get(c).getDGold();
            for (int i = 0; i < size; i++)
            {
                if (gold != 0)
                {
                    if (Math.abs(data.get(c).getDVal(i)-gold)/gold <= input.getSelectionError())
                        correct_counts[i]++;
                    abs_err[i] += Math.abs(data.get(c).getDVal(i)-gold)/gold;
                    cnt[i] = cnt[i] + 1.0;
                }
                // else System.out.println(c); // TODO: check
            }
        }

        // select the feature with minimal error:
        report.println();
        report.println("Errors per fusion function (functions identified by int ID):");
        double min_abs = 1.0;
        int min_ind = -1; // the FF index for the MinAbsError selection method
        int max_count = 0;
        int max_ind = -1;  // the FF index for the MaxCorrectValues selection method
        for (int i = 0; i < size; i++)
        {
            if (cnt[i] != 0)
            {
                abs_err[i] = Math.sqrt(abs_err[i])/cnt[i];

                report.println(i+", mean absolute error : "+abs_err[i]+", count : "+cnt[i]);
                report.println(i+", number of " + input.getSelectionError()*100 + "% correct values : "+correct_counts[i]);

                if (abs_err[i] < min_abs)
                {
                    min_abs = abs_err[i];
                    min_ind = i;
                }
                if (correct_counts[i] > max_count)
                {
                    max_count = correct_counts[i];
                    max_ind = i;
                }
            }
        }
        if (min_ind >= 0)
        {
            report.println();
            report.println(SEL_MIN_ABS_ERR+": best fusion function ID, error %, count: " + min_ind+", "+abs_err[min_ind]*100+", "+cnt[min_ind]);
            report.println(SEL_MAX_CORR_VAL+": best fusion function ID, number of correct values : "+max_ind+", "+correct_counts[max_ind]);
        }

        if (input.ifMinAbsError()) return min_ind;
        if (input.ifMaxCorrectValues()) return max_ind;

        return -1;
    }
    //
    private int learnNominal(Map<String,ValueList> data, int size)
    {
        int count = 0;
        int[] feature_counts = new int[size];
        for (int i = 0; i < size; i++) feature_counts[i] = 0;
        for (String c : data.keySet())
        {
            String gold_val = data.get(c).getGold();
            if (gold_val != null)
            {
                count++;
                for (int i = 0; i < size; i++)
                {
                    if (data.get(c).getVal(i).equals(gold_val)) feature_counts[i]++;
                }
            }
        }

        if (count == 0) return -1;

        report.println();
        report.println("Errors per fusion function (functions identified by int ID):");
        int max = 0;
        for (int i = 0; i < size; i++)
        {
            if (max < feature_counts[i]) max = feature_counts[i];
            double r = 100.0 - 100.0*feature_counts[i]/count;
            report.println(i+" : "+feature_counts[i]+" out of "+count+", relative error: "+r);
        }

        report.println();
        report.println("Best fusion function:");
        int best = 0;
        for (int i = 0; i < size; i++)
        {
            if (feature_counts[i] == max)
            {
                report.println(i+" : "+feature_counts[i]+" out of "+count);
                best = i;
            }
        }

        return best;
    }
    /* ============== */

    /* ======= CREATING SIEVE SPECS ======= */
     // Create a new document, add prefixes and quality scores
    private Node createSieveSpecTempl(String propName, Node ff, int index)
    {
        Node root = createDocument(rootSieve);
        addAttribute(root, "xmlns", "http://www4.wiwiss.fu-berlin.de/ldif/");

        root.appendChild(root.getOwnerDocument().adoptNode(input.getPrefixes().cloneNode(true)));
        root.appendChild(root.getOwnerDocument().adoptNode(input.getQA().cloneNode(true)));
        String ffName = "test-"+index;
        Node fusionNode = addNewElementWithAttribute(root, rootFusion, inaName, ffName);
        Node classNode = addNewElementWithAttribute(fusionNode, rootClass, inaName, input.getClassName());
        Node propNode = addNewElementWithAttribute(classNode, rootProperty, inaName, propName);
        propNode.appendChild(root.getOwnerDocument().adoptNode(ff.cloneNode(true)));

        return root;
    }

    // Create a new document, add prefixes, quality scores, and an empty FusionFunction/Class node
    private Node createFinalSieveSpec()
    {
        Node root = createDocument(rootSieve);
        addAttribute(root, "xmlns", "http://www4.wiwiss.fu-berlin.de/ldif/");

        root.appendChild(root.getOwnerDocument().adoptNode(input.getPrefixes().cloneNode(true)));
        root.appendChild(root.getOwnerDocument().adoptNode(input.getQA().cloneNode(true)));
        Node fusionNode = addNewElementWithAttribute(root, rootFusion, inaName, ffFinalName); // TODO: add method, inferred type, etc.
        Node classNode = addNewElementWithAttribute(fusionNode, rootClass, inaName, input.getClassName());

        return classNode;
    }
    // Add a property node to the final Sieve spec template
    private void addPropToFinalSieveSpec(Node classNode, String propName, Node ff)
    {
        Node propNode = addNewElementWithAttribute(classNode, rootProperty, inaName, propName);
        propNode.appendChild(classNode.getOwnerDocument().adoptNode(ff.cloneNode(true)));
    }

    // Create integration job file
    private Node createIntegrationJob(String output, String sieve, String dumps)
    {
        Node integrationJobNode = createDocument("integrationJob");
        addAttribute(integrationJobNode, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        addAttribute(integrationJobNode, "xsi:schemaLocation", "http://www4.wiwiss.fu-berlin.de/ldif/ ../xsd/IntegrationJob.xsd");
        addAttribute(integrationJobNode, "xmlns", "http://www4.wiwiss.fu-berlin.de/ldif/");

        addNewElementWithValue(integrationJobNode, "properties", integrationPropertiesFile);
        addNewElementWithValue(integrationJobNode, "sieve", sieve);
        addNewElementWithValue(integrationJobNode, "runSchedule", "onStartup");
        addNewElementWithValue2(integrationJobNode, "sources", "source", dumps);
        addNewElementWithValue3(integrationJobNode, "outputs", "output", "file", output);

        return integrationJobNode;
    }

    // Create scheduler config file
    private Node createSchedulerConfig(String dumps)
    {
        Node schedulerConfigNode = createDocument("scheduler");
        addAttribute(schedulerConfigNode, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        addAttribute(schedulerConfigNode, "xsi:schemaLocation", "http://www4.wiwiss.fu-berlin.de/ldif/ ../xsd/SchedulerConfig.xsd");
        addAttribute(schedulerConfigNode, "xmlns", "http://www4.wiwiss.fu-berlin.de/ldif/");

        addNewElementWithValue(schedulerConfigNode, "importJob", "importJobs");
        addNewElementWithValue(schedulerConfigNode, "integrationJob", integrationJobFile);
        addNewElementWithValue(schedulerConfigNode, "properties", schedulerPropertiesFile);
        addNewElementWithValue(schedulerConfigNode, "dataSources", "dataSources");
        addNewElementWithValue(schedulerConfigNode, "dumpLocation", dumps);

        return schedulerConfigNode;
    }
    /* ============== */

    /* Utils */
    // Create a file with one string in it
    private static void writeStringToFile(String path, String text)
    {
        try
        {
            PrintWriter out = new PrintWriter(path);
            out.println(text);
            out.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    // For value provenance in DBpedia
    private String WikiLangPrefix(String s)
    {
        if (s.contains("wikipedia.org")) return s.substring(7,9);
        return s;
    }
}
