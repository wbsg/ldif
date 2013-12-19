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

/* Stores entries of FusionFunction matrix, including gold values; i identifies fusion function */
public class ValueList
{
    private String[] vals;
    private String[] prov;
    private String gold = null;
    private Double gold_d = null;

    public ValueList(int size)
    {
        vals = new String[size];
        prov = new String[size];
    }
    public boolean set(int i, String v, String p)
    {
        if (wrongSize(i)) return false;
        vals[i] = v;
        prov[i] = p;
        return true;
    }
    public String getVal(int i)
    {
        if (wrongSize(i)) return null;
        return vals[i];
    }
    public Double getDVal(int i)
    {
        if (wrongSize(i)) return null;
        return Double.parseDouble(vals[i]);
    }
    public String getProv(int i)
    {
        if (wrongSize(i)) return null;
        return prov[i];
    }
    private boolean wrongSize(int i)
    {
        if (vals == null) return true;
        if (i < 0 || i >= vals.length) return true;
        return false;
    }

    public String getGold()
    {
        return gold;
    }
    public Double getDGold()
    {
        return gold_d;
        // return Double.parseDouble(gold);
    }
    public Double setGold(String gold)
    {
        this.gold = gold;
        // converting to numeric value, null if impossible:
        this.gold_d = isNumeric(gold);
        return gold_d;
    }

    public static Double isNumeric(String str)
    {
        Double d = null;
        try
        {
            d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return null;
        }
        return d;
    }

}
