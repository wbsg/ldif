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

import java.util.Map;

/* Loads gold standard values from "fnGold" into existing value matrix "data" given the "property", detects the data type (numeric or nominal) */
public class GoldStandard
{
    private Integer size = 0;
    private boolean isNumeric = false;

    public int load(String property, String fnGold, Map<String,ValueList> data)
    {
        size = 0;
        int numeric = 0;
        try
        {
            FileInputStream is = new FileInputStream(fnGold);
            NxParser nxp = new NxParser(is); // https://code.google.com/p/nxparser/
            while (nxp.hasNext())
            {
                org.semanticweb.yars.nx.Node[] ns = nxp.next();
                if (ns.length == 3)
                {
                    String subj = ns[0].toString();
                    String prop = ns[1].toString();
                    String obj = ns[2].toString();
                    if (prop.equals(property) && data.containsKey(subj))
                    {
                        // detect type, numeric or nominal:
                        if (data.get(subj).setGold(obj) != null) numeric++;
                        size++;
                    }
                }
            }
            is.close();
        }
        catch (IOException e)
        {
            // e.printStackTrace();
            System.out.println("ERROR: gold standard file " + fnGold + " not found");
            return -1;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            System.out.println("ERROR: gold standard file " + fnGold + " parsing error");
            return -1;
        }


        if (numeric == size) isNumeric = true; // TODO: account for errors
        else isNumeric = false;

        return size;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public void setNumeric(boolean numeric) {
        isNumeric = numeric;
    }
}
