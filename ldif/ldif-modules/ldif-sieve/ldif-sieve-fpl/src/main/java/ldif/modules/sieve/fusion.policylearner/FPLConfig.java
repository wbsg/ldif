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

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static ldif.modules.sieve.fusion.policylearner.FPLConfigStrings.*;
import static ldif.modules.sieve.fusion.policylearner.XMLUtils.*;

/* Loads and stores input FPL specification */
public class FPLConfig
{
    // input parameters
    private String selectionMethod;
    private Double selectionError;
    private String goldPath;
    private String dumpLocation;
    private String sieveExec;
    private String finalSieveSpec;
    private String fusionReport;

    private boolean printValueMatrix = false;
    private Double selectionErrorDefault = 0.05;

    // nodes to copy as they are into new Sieve specs
    private Node prefixes;
    private Node QA;
    // ASSUMPTION: ONE CLASS PER INPUT FILE!
    private String className;
    // map{property name} = NodeList of corresponding FusionFunction nodes
    private Map<String,NodeList> properties = new HashMap<String,NodeList>();
    // map{prefix}=full-namespace
    private Map<String,String> namespaces = new HashMap<String,String>();

    private String LDIFfDir;
    private String workingDir;

     // Read spec file
    public boolean readSpec(String specPath)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(specPath);

            setSelectionMethod(getAttrValue(doc, inSelMethod, inaName));
            if (getSelectionMethod().equals(SEL_MIN_ABS_ERR)) setSelectionError(selectionErrorDefault);
            else setSelectionError(Double.parseDouble(getAttrValue(doc, inSelMethod, inaError)));
            setGoldPath(getValue(doc, inGoldPath));
            setDumpLocation(getValue(doc, inDumpLocation));
            setSieveExec(getValue(doc, inSieveExec));
            setFinalSieveSpec(getValue(doc, inFinalSieveSpec));
            setFusionReport(getValue(doc, inFusionReport));
            String vmtx = getAttrValue(doc, inFusionReport, inaVMtx);
            // <FPLReport valmatrix = "true">FPL_report.txt</FPLReport> :
            if (vmtx != null && vmtx.equals(trueVal)) printValueMatrix = true;
            setPrefixes(getNodeList(doc, inPrefixes).item(0));
            loadNamespaces();
            setQA(getNodeList(doc, inQA).item(0));
            if (getNodeList(doc, inClass).getLength() > 1)
            {
                System.out.println("Only one class element in the input Sieve specification is permitted");
                return false;
            }
            setClassName(getAttrValue(doc, inClass, inaName));

              // properties and fusion functions:
            NodeList nl = getNodeList(doc, inProperty);
            for (int i = 0; i < nl.getLength(); i++)
            {
                String prop = getAttrValue(nl.item(i), inaName);
                NodeList ff = getNodeList(nl.item(i),inFusionFunction);
                getProperties().put(prop, ff);
            }

            // get ldif and working directories:
            File se = new File(getSieveExec());
            setLDIFfDir(se.getParentFile().getParent()); // go two levels up
            File ex = new File(specPath);
            setWorkingDir(ex.getParent() + "\\");
        }
        catch (ParserConfigurationException e)
        {
            System.out.println("Error: FPL specification cannot be parsed correctly.");
            e.printStackTrace();
            return false;
        }
        catch (SAXException e)
        {
            System.out.println("Error: FPL specification cannot be parsed correctly.");
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.out.println("Error: FPL specification cannot be parsed correctly.");
            e.printStackTrace();
            return false;
        }
        catch (Exception e)
        {
            System.out.println("Error: FPL specification cannot be parsed correctly.");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public boolean ifMinAbsError()
    {
        return selectionMethod.equals(SEL_MIN_ABS_ERR);
    }
    public boolean ifMaxCorrectValues()
    {
        return selectionMethod.equals(SEL_MAX_CORR_VAL);
    }

    private boolean loadNamespaces()
    {
        NodeList nl = prefixes.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++)
        {
            if (nl.item(i).getNodeName().equals(inPrefixesPrefix))
            {
                String pre = getAttrValue(nl.item(i), inaID);
                String ns = getAttrValue(nl.item(i), inaNS);
                namespaces.put(pre,ns);
            }
        }
        return true;
    }

    public String getFullName(String s)
    {
        // dbpedia-owl:populationTotal, in gs: <http://dbpedia.org/ontology/populationTotal
        String res = s;
        for (String prefix : namespaces.keySet())
        {
            String ns = namespaces.get(prefix);
            if(s.contains(prefix+":"))
            {
                res = s.replace(prefix+":",ns);
                return res;
            }
        }
        return res;
    }

    public Map<String,String> getNamespaces() {
        return namespaces;
    }

    public String getSelectionMethod() {
        return selectionMethod;
    }

    public void setSelectionMethod(String selectionMethod) {
        this.selectionMethod = selectionMethod;
    }

    public Double getSelectionError() {
        return selectionError;
    }

    public void setSelectionError(Double selectionError) {
        this.selectionError = selectionError;
    }

    public String getGoldPath() {
        return goldPath;
    }

    public void setGoldPath(String goldPath) {
        this.goldPath = goldPath;
    }

    public String getDumpLocation() {
        return dumpLocation;
    }

    public void setDumpLocation(String dumpLocation) {
        this.dumpLocation = dumpLocation;
    }

    public String getSieveExec() {
        return sieveExec;
    }

    public void setSieveExec(String sieveExec) {
        this.sieveExec = sieveExec;
    }

    public String getFinalSieveSpec() {
        return finalSieveSpec;
    }

    public void setFinalSieveSpec(String finalSieveSpec) {
        this.finalSieveSpec = finalSieveSpec;
    }

    public String getFusionReport() {
        return fusionReport;
    }

    public void setFusionReport(String fusionReport) {
        this.fusionReport = fusionReport;
    }

    public Node getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(Node prefixes) {
        this.prefixes = prefixes;
    }

    public Node getQA() {
        return QA;
    }

    public void setQA(Node QA) {
        this.QA = QA;
    }

    public Map<String, NodeList> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, NodeList> properties) {
        this.properties = properties;
    }

    public String getLDIFfDir() {
        return LDIFfDir;
    }

    public void setLDIFfDir(String LDIFfDir) {
        this.LDIFfDir = LDIFfDir;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isPrintValueMatrix() {
        return printValueMatrix;
    }

    public void setPrintValueMatrix(boolean printValueMatrix) {
        this.printValueMatrix = printValueMatrix;
    }
}
