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

import java.io.File;

/* Main class of teh Fusion Policy Learner module */
public class FPL
{
    public static void main (String args[])	throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("Error: wrong arguments; the only argument should be the path to an FPL specification.");
            return;
        }
        String input = args[0];
        // String input = "c:\\ldif-0.5.1\\examples\\dbpedia-multilang\\SieveFPL.xml";

        File f = new File(input);
        if(!f.exists())
        {
            System.out.println("Error: FPL specification file not found.");
            return;
        }

        FPLConfig is = new FPLConfig();
        if(!is.readSpec(input)) return; // if specification is not read correctly, then exit
        SimpleLearner learner = new SimpleLearner();
        learner.learn(is);
   }
}