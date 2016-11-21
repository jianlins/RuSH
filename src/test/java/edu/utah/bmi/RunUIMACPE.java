/*******************************************************************************
 * Copyright  2016  Department of Biomedical Informatics, University of Utah
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.utah.bmi;

/**
 * This java class is to use UIMA tools (CPE, CPE-GUI, or DocumentAnalyzer) conveniently.
 * - Use CpmFrame to modify the CPE descriptor.xml (In the menu, open file, locate your local FastContext_General_CPEdesc.xml, and load it)
 * - Use SimpleRunCPE to run CPE more quickly without any configuration
 * - Use DocumentAnalyzer to launch UIMA DocumentAnalyzer.
 * @Author Jianlin Shi
 */
public class RunUIMACPE {
    public static void main(String[] args) throws Exception{
//      org.apache.uima.tools.cpm.CpmFrame.main(args);
//      org.apache.uima.tools.docanalyzer.DocumentAnalyzer.main(args);
        org.apache.uima.examples.cpe.SimpleRunCPE.main(new String[]{"desc/FUSS_DiffColor_POET_CPEdesc.xml"});
    }
}
