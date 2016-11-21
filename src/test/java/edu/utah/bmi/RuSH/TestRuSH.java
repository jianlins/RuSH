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
package edu.utah.bmi.RuSH;

import edu.utah.bmi.nlp.Span;
import edu.utah.bmi.rush.core.RuSH;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * This rule set are more inclusive that is trying to include all non-whitespace characters in a sentence.
 * @Author Jianlin Shi
 */
public class TestRuSH {
    private RuSH seg;
    private boolean debug = true;

    public static void printDetails(ArrayList<Span> sentences, String input, boolean debug) {
        if (debug) {
            for (Span sentence : sentences) {
                System.out.println(sentence.begin + "-" + sentence.end + "\t" + ">" + input.substring(sentence.begin, sentence.end) + "<");
            }
            for (int i = 0; i < sentences.size(); i++) {
                Span sentence = sentences.get(i);
                System.out.println("assert (sentences.get(" + i + ").begin == " + sentence.begin + " &&" +
                        " sentences.get(" + i + ").end == " + sentence.end + ");");
            }
        }
    }
    @Before
    public void initiate() {

        seg = new RuSH("conf/crule_sem_inclusive.csv");


        seg.setDebug(debug);
        seg.setSpecialCharacterSupport(true);
    }
    

    @Test
    public void test1() throws Exception {
        String input = "In is a unit. Can Mrs. G check it. Look\n Good.\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 13);
        assert (sentences.get(1).begin == 14 && sentences.get(1).end == 34);
        assert (sentences.get(2).begin == 35 && sentences.get(2).end == 39);

    }

    @Test
    public void test2() {
        String input = "S/p C5-7 ACDF. No acute events overnight. Pain control and issue ON. " +
                "Now better controlled. Walked 300 ft with PT. Discharge today vs tomorrow. \n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 14);
        assert (sentences.get(1).begin == 15 && sentences.get(1).end == 41);
        assert (sentences.get(2).begin == 42 && sentences.get(2).end == 68);
        assert (sentences.get(3).begin == 69 && sentences.get(3).end == 91);
        assert (sentences.get(4).begin == 92 && sentences.get(4).end == 114);
        assert (sentences.get(5).begin == 115 && sentences.get(5).end == 143);

    }

    @Test
    public void test3() {
        String input = "X-rays had shown degenerative spondylolisthesis, L4-L5. " +
                "MRI today was compared to prior MRIs. This shows that the synovial extradural " +
                "cyst at L4-L5 right, has diminished in size, but she has evidence of synovial " +
                "cysts external to the spinal canal bilaterally. There is still some cyst on the" +
                " right. She has some but not severe stenosis as a result. She has arthritic facets " +
                "at L3-L4 and L5-S1 as well, but not as large as at L4-L5.\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);

        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 55);
        assert (sentences.get(1).begin == 56 && sentences.get(1).end == 93);
        assert (sentences.get(2).begin == 94 && sentences.get(2).end == 259);
        assert (sentences.get(3).begin == 260 && sentences.get(3).end == 298);
        assert (sentences.get(4).begin == 299 && sentences.get(4).end == 348);

    }

    @Test
    public void test4() {
        String input = "\n" +
                "\n" +
                "1. Minimally invasive partial hemilaminotomy, exploration, decompression and " +
                "foraminotomy, L4-L5, bilateral.\n" +
                "\n" +
                "\n" +
                "\n" +
                "2. Microsurgical excision of synovial extradural cyst, L4-L5, bilateral.\n" +
                "\n" +
                "\n" +
                "\n" +
                "3. A 360-degree fusion, L4-L5 (transfacet lumbar interbody fusion using allograft, " +
                "autograft and PEEK; left posterolateral fusion using allograft and autograft).\n" +
                "\n" +
                "\n" +
                "\n" +
                "4. Pedicle screw fixation, L4-L5 bilateral (Danek Legacy 5.5) using image-guided " +
                "navigation, Sextant on the right.\n" +
                "\n" +
                "\n" +
                "\n" +
                "SURGEON: Rot S. Woud, M.D.\n" +
                "\n" +
                "\n" +
                "\n" +
                "ASSISTANT: Havan Vakumar, MD.\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 2 && sentences.get(0).end == 110);
        assert (sentences.get(1).begin == 114 && sentences.get(1).end == 186);
        assert (sentences.get(2).begin == 190 && sentences.get(2).end == 351);
        assert (sentences.get(3).begin == 355 && sentences.get(3).end == 469);
        assert (sentences.get(4).begin == 473 && sentences.get(4).end == 499);
        assert (sentences.get(5).begin == 503 && sentences.get(5).end == 532);
    }

    @Test
    public void test5() {
        String input = "   Service: (none)     Author Type: Physician    \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Filed: 11/2/2001 9:12 AM     Note Time: 11/2/2001 2:00 AM     Note Type: Op Note    \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Status: Signed     Editor: Read, MD (Physician)          \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Trans ID: 962048      Trans Status: Available          \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Dictation Time:  11/2/2001 12:07 PM  Trans Time:  11/2/2001 7:25 PM  Trans Doc Type:  POST-OP NOT";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 3 && sentences.get(0).end == 45);
        assert (sentences.get(1).begin == 57 && sentences.get(1).end == 137);
        assert (sentences.get(2).begin == 149 && sentences.get(2).end == 196);
        assert (sentences.get(3).begin == 214 && sentences.get(3).end == 259);
        assert (sentences.get(4).begin == 277 && sentences.get(4).end == 374);
    }

    @Test
    public void test6() {
        String input = "\n" +
                " Op Note signed by Lort, MD at 12/11/2014 11:21 AM  \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "    Author: Lort Sidney Woud, MD  Service: (none)  Author Type: Physician    \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Filed: 12/11/2014 11:21 AM  Note Time: 12/3/2014 2:00 AM  Note Type: Op Note    \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Status: Signed  Editor: Lort Sidney Woud, MD (Physician)       \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Trans ID: 994037_escript  Trans Status: Available  Dictation Time: 10/06/2014 1:21 PM    \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Trans Time: 12/11/2014 12:18 AM  Trans Doc Type: POST-OP NOTE       \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "University        Health Care";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);

        assert (sentences.get(0).begin == 2 && sentences.get(0).end == 51);
        assert (sentences.get(1).begin == 65 && sentences.get(1).end == 134);
        assert (sentences.get(2).begin == 146 && sentences.get(2).end == 222);
        assert (sentences.get(3).begin == 234 && sentences.get(3).end == 290);
        assert (sentences.get(4).begin == 305 && sentences.get(4).end == 390);
        assert (sentences.get(5).begin == 402 && sentences.get(5).end == 463);
        assert (sentences.get(6).begin == 476 && sentences.get(6).end == 505);

    }

    /**
     * Exclude list numbers
     */
    @Test
    public void test7() {
        String input = "39 year old female with history of IDDM type 1, ESRD " +
                "from diabetic nephropathy, s/p kidney and pancreas transplant in 5/2011 (CMV D+/R-), " +
                "delayed healing of post-op abdominal wall incision, intra-abdominal sepsis / abscess due " +
                "to Lactobacillus, Candida glabrata and E. Faecalis, failed pancreas transplant, s/p " +
                "transplant pancreatectomy on 1/15/11. \n" +
                "\n" +
                "\n" +
                "\n" +
                "1. UTI: \n\n Eodn 2. IID \n\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 348);
        assert (sentences.get(1).begin == 353 && sentences.get(1).end == 375);
    }

    @Test
    public void test8() {
        String input = "Skin: Positive for color change and wound********\n" +
                "\n" +
                "\n" +
                "\n" +
                "Neurological: Positive for headaches ********\n" +
                "\n" +
                "\n" +
                "\n" +
                "Psychiatric/Behavioral: Negative.****\n" +
                "\n" +
                "\n" +
                "\n" +
                "All other systems reviewed and are negative.";

        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 41);
        assert (sentences.get(1).begin == 53 && sentences.get(1).end == 89);
        assert (sentences.get(2).begin == 102 && sentences.get(2).end == 135);
        assert (sentences.get(3).begin == 143 && sentences.get(3).end == 187);

    }

    @Test
    public void test9() {
        String input = "Midline structures including the corpus callosum, pituitary and pineal gland " +
                "demonstrate normal appearance.\n" +
                "\n" +
                "\n" +
                "\n" +
                "2.2x3.9x2.7 cm T1 hypointense, T2 hyperintense cystic space within the anterior right" +
                " temporal lobe is in keeping with a benign arachnoid cyst. \n";


        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 107);
        assert (sentences.get(1).begin == 111 && sentences.get(1).end == 254);

    }

    @Test
    public void test10() {
        String input = "\n" +
                "    Details \n" +
                "\n" +
                "\n" +
                "\n" +
                " memantine (NAMENDA) 5 MG Tab  Take 5 mg by mouth 2 times a day. \n" +
                "\n" +
                "\n" +
                "\n" +
                "   \n" +
                "\n" +
                "\n" +
                "\n" +
                " gabapentin (NEURONTIN) 300 MG Cap  Take 300 mg by mouth 3 times daily. \n" +
                "\n" +
                "\n" +
                "\n" +
                "   \n" +
                "\n" +
                "\n" +
                "\n" +
                " levothyroxine (SYNTHROID, LEVOTHROID) 100 MCG Tab  Take 100 mcg by mouth Daily. \n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 5 && sentences.get(0).end == 81);
        assert (sentences.get(1).begin == 94 && sentences.get(1).end == 164);
        assert (sentences.get(2).begin == 177 && sentences.get(2).end == 256);

    }

    @Test
    public void test11() {
        String input = "\n" +
                "The left upper quadrant coronal plane evaluating the inferior left thoracic cavity, the" +
                " splenorenal space, and left paracolic gutter for anechoic free fluid was: \n" +
                "\n" +
                "\n" +
                "\n" +
                "negative FOR FREE FLUID IN INTRAPERITONEAL SPACE \n" +
                "\n" +
                "\n" +
                "\n" +
                "A suprapubic window evaluating posterior and lateral to the bladder for anechoic free fluid was: ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 1 && sentences.get(0).end == 215);
        assert (sentences.get(1).begin == 220 && sentences.get(1).end == 316);
    }

    @Test
    public void test12() {
        String input =
                "He exhibits R sided chest wall tenderness). He exhibits no edema. ******** \n" +
                        "Neurological: He is alert and oriented to person, place, and time. " +
                        "He has normal reflexes. No cranial nerve deficit. **\n\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 43);
        assert (sentences.get(1).begin == 44 && sentences.get(1).end == 65);
        assert (sentences.get(2).begin == 76 && sentences.get(2).end == 142);
        assert (sentences.get(3).begin == 143 && sentences.get(3).end == 166);
        assert (sentences.get(4).begin == 167 && sentences.get(4).end == 192);


    }

    @Test
    public void test13() {
        String input =
                "    Impression:       \n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "    1.Disrupted right acromioclavicular joint with likely involvement of the" +
                        " coracoclavicular ligament. \n" +
                        "\n" +
                        "2. Multiple right lateral rib fractures.";


        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 4 && sentences.get(0).end == 15);
        assert (sentences.get(1).begin == 32 && sentences.get(1).end == 129);
        assert (sentences.get(2).begin == 132 && sentences.get(2).end == 172);


    }

    @Test
    public void test14() {
        String input =
                "\n**Number of stimulations: 1.**\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "**Stimulation #1 Details\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Bifrontal stimulation for 8.0 seconds, at 120 Hz, 0.8 amps, 0.37 msec.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Hyperventilation.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "****Augmentation with IV Caffeine (mg): 1250.** ****\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "****\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "**Peripheral seizure at 47 seconds. Central seizure at 315 seconds.**\n" +
                        "\n";


        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 3 && sentences.get(0).end == 29);
        assert (sentences.get(1).begin == 37 && sentences.get(1).end == 59);
        assert (sentences.get(2).begin == 63 && sentences.get(2).end == 133);
        assert (sentences.get(3).begin == 137 && sentences.get(3).end == 154);
        assert (sentences.get(4).begin == 162 && sentences.get(4).end == 203);
        assert (sentences.get(5).begin == 224 && sentences.get(5).end == 257);
        assert (sentences.get(6).begin == 258 && sentences.get(6).end == 289);


    }

    @Test
    public void test15() {
        String input =
                " \n" +
                        " Reason for Hospitalization:  \n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "- Inability to care for self in Less Restrictive Environment\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "- Risk of Harm to Self\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        " History of Present Illness:  ";


        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 3 && sentences.get(0).end == 30);
        assert (sentences.get(1).begin == 38 && sentences.get(1).end == 98);
        assert (sentences.get(2).begin == 102 && sentences.get(2).end == 124);
        assert (sentences.get(3).begin == 131 && sentences.get(3).end == 158);


    }

    @Test
    public void test16() {
        String input =
                "Fat (series 2, images 30-37). Air-containing nephrectomy bed sinus cavity extends from the left" +
                        " adrenal gland at its most superomedial extent (series 2, image 25; series 3, image 53), extending" +
                        " inferolaterally over the psoas and along the left paracolic gutter, just inferior to the level of " +
                        "the left iliac crest (series 2, image 48; series 3, image 36). Right kidney is normal in size, " +
                        "enhancement, and contour. \n";

        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 29);
        assert (sentences.get(1).begin == 30 && sentences.get(1).end == 354);
        assert (sentences.get(2).begin == 355 && sentences.get(2).end == 412);

    }


    @Test
    public void test17() {
        String input =
                "Fekri J Kaman is an 33 year old female with colocutaneous fistula, POD2 from ex lap with " +
                        "takedown of colocutaneou fistula.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "* No surgery date entered * s/p Procedure(s) (LRB):\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "ex-lap repair of colocutaneous fistula and packing of fistula tract on back (N/A)\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        " Plan: \n";


        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 122);
        assert (sentences.get(1).begin == 126 && sentences.get(1).end == 177);
        assert (sentences.get(2).begin == 181 && sentences.get(2).end == 262);
        assert (sentences.get(3).begin == 269 && sentences.get(3).end == 274);


    }

    @Test
    public void test18() {
        String input =
                "\n" +
                        " Component \n" +
                        "    Latest Ref Rng   6/02/2003   6/02/2003   6/02/2003   6/02/2003   6/02/2003  \n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "  \n" +
                        "        10:40 PM   9:15 PM   8:10 PM   7:15 PM   6:00 PM  \n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        " Glucose, Finger stick \n" +
                        "    64 - 128   108   147 (A)   175 (A)   211 (A)   265 (A)  ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 2 && sentences.get(0).end == 91);
        assert (sentences.get(1).begin == 108 && sentences.get(1).end == 156);
    }

    @Test
    public void test19() {
        String input =
                "Rupture Time: 0037\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Fluid Color: Meconium\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "5.5 cm dilated, complete effacement, 0 station\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Baseline FHR: 145 per minute\n" +
                        "\n" +
                        "\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 18);
        assert (sentences.get(1).begin == 22 && sentences.get(1).end == 93);
        assert (sentences.get(2).begin == 97 && sentences.get(2).end == 125);

    }


    @Test
    public void test20() {
        String input = " Medication     Dose  Route  Frequency  Provider  Last Rate  Last Dose \n" +
                "\n" +
                "\n" +
                "\n" +
                " •  oxytocin (PITOCIN) 30 units/500 mL infusion  0-900 mL/hr  Intravenous  Continuous  Grogrey A Saird, " +
                "MD  60 mL/hr at 01/01/11 0247  60 mL/hr at 01/01/11 0247 \n" +
                "\n" +
                "\n" +
                "\n" +
                " •  fentaNYL 2 mcg/mL+bupivacaine 1/12% epidural                      \n" +
                "\n" +
                "\n" +
                "\n" +
                " •  lactated ringers infusion  125 mL/hr  Intravenous  Continuous  Grakkey A Koird, MD  125 mL/hr at " +
                "01/01/11 0011  125 mL/hr at 01/01/11 0011 \n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);

        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 1 && sentences.get(0).end == 70);
        assert (sentences.get(1).begin == 76 && sentences.get(1).end == 235);
        assert (sentences.get(2).begin == 241 && sentences.get(2).end == 288);
        assert (sentences.get(3).begin == 315 && sentences.get(3).end == 456);
    }

    @Test
    public void test21() {
        String input = "POSTOPERATIVE DIAGNOSES:\n" +
                "\n" +
                "\n" +
                "\n" +
                "1. A 39-year-old G1-P0, status post normal spontaneous vaginal delivery at 37\n" +
                "\n" +
                "\n" +
                "\n" +
                "weeks 0 day's gestation.\n" +
                "\n" +
                "\n" +
                "\n" +
                "2. Postpartum hemorrhage after delivery with continued bleeding and clots ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 24);
        assert (sentences.get(1).begin == 28 && sentences.get(1).end == 133);
        assert (sentences.get(2).begin == 137 && sentences.get(2).end == 210);
    }

    @Test
    public void test22() {
        String input = "Hemabate was given and anesthesia was redosed for an adequate. With exam, \n" +
                "\n" +
                "\n" +
                "\n" +
                "additional blood was evacuated from the posterior fornix; however, both the \n" +
                "\n" +
                "\n" +
                "\n" +
                "lower uterine segment, as well as the fundus was noted to be firm, and there \n" +
                "\n" +
                "\n" +
                "\n" +
                "was no evidence of bleeding from lacerations. There were no cervical \n" +
                "\n" +
                "\n" +
                "\n" +
                "lacerations. ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 62);
        assert (sentences.get(1).begin == 63 && sentences.get(1).end == 284);
        assert (sentences.get(2).begin == 285 && sentences.get(2).end == 324);

    }

    @Test
    public void test23() {
        String input = "Total EBL of the procedure was 100 mL plus the 100 mL \n" +
                "\n" +
                "\n" +
                "\n" +
                "that was expressed on initial Crede, with a total of 200 mL. The patient \n" +
                "\n" +
                "\n" +
                "\n" +
                "tolerated the procedure well and she was transferred back to the recovery \n" +
                "\n" +
                "\n" +
                "\n" +
                "room.\n" +
                "\n" +
                "\n" +
                "\n" +
                "Dr. Caustney Kallean was scrubbed and present for the entire procedure.";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 118);
        assert (sentences.get(1).begin == 119 && sentences.get(1).end == 218);
        assert (sentences.get(2).begin == 222 && sentences.get(2).end == 293);
    }


    @Test
    public void test24() {
        String input = " Current Facility-Administered Medications                      \n" +
                "\n" +
                "\n" +
                "\n" +
                " Medication     Dose  Route  Frequency  Provider  Last Rate  Last Dose \n" +
                "\n" +
                "\n" +
                "\n" +
                " •  sennosides-docusate sodium (PERICOLACE) 8.6-50 MG 1 tablet  1 tablet  Oral  BID  Kaveria Tias, MD      1 tablet at 12/17/14 2021 \n" +
                "\n" +
                "\n" +
                "\n" +
                " •  alum & mag hydroxide-simeth (MAALOX MAX) 400-400-40 MG/5ML suspension 30 mL  30 mL  Oral  Q4H PRN  Gerija Kum, MD   ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 1 && sentences.get(0).end == 42);
        assert (sentences.get(1).begin == 69 && sentences.get(1).end == 138);
        assert (sentences.get(2).begin == 144 && sentences.get(2).end == 275);
        assert (sentences.get(3).begin == 281 && sentences.get(3).end == 397);
    }

    @Test
    public void test25() {
        String input = "The patient was on magnesium, which was then discontinued with \n" +
                "\n" +
                "\n" +
                "\n" +
                "concerns that it may be contributing to intermittent atony. She received 3 \n" +
                "\n" +
                "\n" +
                "\n" +
                "total doses of Hemabate and would remain stable for a short period of time; \n" +
                "\n" +
                "\n" +
                "\n" +
                "however, with Crede, would express a respectable amount of blood. Given that \n" +
                "\n" +
                "\n" +
                "\n" +
                "she had continued to bleed, decision was made to move back to the operating \n" +
                "\n" +
                "\n" +
                "\n" +
                "room for thorough examination under anesthesia, as well as for dilation and \n" +
                "\n" +
                "\n" +
                "\n" +
                "curettage.";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 126);
        assert (sentences.get(1).begin == 127 && sentences.get(1).end == 291);
        assert (sentences.get(2).begin == 292 && sentences.get(2).end == 477);
    }

    @Test
    public void test26() {
        String input = "Sonographic dating in the third trimester is limited. The distal femoral \n" +
                "\n" +
                "\n" +
                "\n" +
                "epiphysis (DFE) is usually seen by 32 weeks gestation, the proximal tibial (PTE) \n" +
                "\n" +
                "\n" +
                "\n" +
                "by 35 weeks and the proximal humeral (PHE) by 38 weeks gestation. Today the DFE \n" +
                "\n" +
                "\n" +
                "\n" +
                "and a faint PTE are seen, but the PHE is not, consistent with a gestational age \n" +
                "\n" +
                "\n" +
                "\n" +
                "of at least 35 weeks but less than 38 weeks.\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Diagnostic Imaging ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 53);
        assert (sentences.get(1).begin == 54 && sentences.get(1).end == 227);
        assert (sentences.get(2).begin == 228 && sentences.get(2).end == 374);
        assert (sentences.get(3).begin == 381 && sentences.get(3).end == 399);

    }


    @Test
    public void test27() {
        String input = "TTE 10/19: EF 57%, normal LV function. \n" +
                "\n" +
                "\n" +
                "\n" +
                "-stable this pregnancy with mild intermittent chest pain and SOB, nonpleuritic\n" +
                "\n" +
                "\n" +
                "\n" +
                "- tele during labor \n" +
                "\n" +
                "\n" +
                "\n" +
                "Substance abuse - pt with history of heroin use early in pregnancy, was then on methadone. Not currently receiving any opioids.  \n" +
                "-Utox 10/25: pos for caffeine and promethazine \n" +
                "\n" +
                "\n" +
                "\n" +
                "-Utox pending \n" +
                "\n" +
                "\n" +
                "\n" +
                "Rubella immune, Rh positive \n" +
                "\n" +
                "\n" +
                "\n" +
                "Delivery planning:  \n" +
                "-anticipate SVD \n" +
                "-baby will be up for adoption. Has adoptive parents already. ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 38);
        assert (sentences.get(1).begin == 43 && sentences.get(1).end == 121);
        assert (sentences.get(2).begin == 125 && sentences.get(2).end == 144);
        assert (sentences.get(3).begin == 149 && sentences.get(3).end == 239);
        assert (sentences.get(4).begin == 240 && sentences.get(4).end == 276);
        assert (sentences.get(5).begin == 279 && sentences.get(5).end == 325);
        assert (sentences.get(6).begin == 330 && sentences.get(6).end == 343);
        assert (sentences.get(7).begin == 348 && sentences.get(7).end == 375);
        assert (sentences.get(8).begin == 380 && sentences.get(8).end == 398);
        assert (sentences.get(9).begin == 401 && sentences.get(9).end == 416);
        assert (sentences.get(10).begin == 418 && sentences.get(10).end == 448);
        assert (sentences.get(11).begin == 449 && sentences.get(11).end == 478);
    }

    /**
     * \c.\n+-(\d)	0	stbegin
     * \c.\s+\n+-(\d)	0	stbegin
     * \c.\s+\n+\s+-(\d)	0	stbegin
     */
    @Test
    public void test28() {
        String input = "Breastfeeding is okay.  \n" +
                "-10/25: HCV quant undetectable, HIV negative \n" +
                "\n" +
                "\n" +
                "\n" +
                "H/O cardiomyopathy \n" +
                "Pt with cardiomyopathy in the setting of ritalin use in 2014. ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 22);
        assert (sentences.get(1).begin == 25 && sentences.get(1).end == 69);
        assert (sentences.get(2).begin == 74 && sentences.get(2).end == 92);
        assert (sentences.get(3).begin == 94 && sentences.get(3).end == 155);

    }


    @Test
    public void test29() {
        String input = " When do I stop eating and drinking?  \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "·     Please do not eat or drink anything after midnight the night before your surgery.\n" +
                "\n" +
                "\n" +
                "\n" +
                "·     You may drink sips of water until 4 hours before your scheduled surgery time.\n" +
                "\n" +
                "\n" +
                "\n" +
                "·     If you eat or drink after these times, your surgery will be postponed. " +
                "This is to minimize the danger from inhaling stomach contents during your surgery while under general anesthesia, which can be life threatening.";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 1 && sentences.get(0).end == 36);
        assert (sentences.get(1).begin == 44 && sentences.get(1).end == 131);
        assert (sentences.get(2).begin == 135 && sentences.get(2).end == 218);
        assert (sentences.get(3).begin == 222 && sentences.get(3).end == 298);
        assert (sentences.get(4).begin == 299 && sentences.get(4).end == 443);
    }


    @Test
    public void test30() {
        String input = "Airway \n" +
                " \n" +
                "**11/11/2003 12:30 PM \n" +
                " \n" +
                "****Staff: NEKKER, BRIEGET KATALEEN \n" +
                "****Airway: ETT \n" +
                "********Airway positioning: alignment of airway axes optimized using a ramp of blankets \n" +
                "****Mask ventilation: Grade 0 - ventilation by mask not attempted \n" +
                "****Oxygen delivery: mask \n" +
                "****EtCO2 sampling: yes \n" +
                "****Intubation type: oral ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 6);
        assert (sentences.get(1).begin == 12 && sentences.get(1).end == 31);
        assert (sentences.get(2).begin == 39 && sentences.get(2).end == 70);
        assert (sentences.get(3).begin == 76 && sentences.get(3).end == 87);
        assert (sentences.get(4).begin == 97 && sentences.get(4).end == 176);
        assert (sentences.get(5).begin == 182 && sentences.get(5).end == 243);
        assert (sentences.get(6).begin == 249 && sentences.get(6).end == 270);
        assert (sentences.get(7).begin == 276 && sentences.get(7).end == 295);
        assert (sentences.get(8).begin == 301 && sentences.get(8).end == 322);
    }

    @Test
    public void test31() {
        String input = " GI/Hepatic/Renal:** \n" +
                " \n" +
                "Comments: High cholestrol**************** \n" +
                "(-) GERD \n" +
                "**************** \n" +
                "(-) Denies History of Hepatitis \n" +
                "**** \n" +
                "(-) Denies History of Liver Disease \n" +
                "**** \n" +
                "(+) History of Renal Disease: ESRD and Dialysis \n" +
                "****** \n" +
                "\n" +
                "\n" +
                "\n" +
                " Endo/Other:** \n" +
                " \n" +
                "Comments: Spinal stenosis- uses power chair******** \n" +
                "(+) History of Diabetes (Recently stopped taking insulin.); type 2.  \n" +
                " \n" +
                " \n" +
                "******** \n" +
                "(+) History of Thyroid Disorder; Hypothyroidism \n" +
                " \n" +
                "************ \n" +
                "(+) History of Arthritis and Osteoarthritis \n" +
                " \n" +
                "** ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 1 && sentences.get(0).end == 18);
        assert (sentences.get(1).begin == 24 && sentences.get(1).end == 49);
        assert (sentences.get(2).begin == 67 && sentences.get(2).end == 75);
        assert (sentences.get(3).begin == 95 && sentences.get(3).end == 126);
        assert (sentences.get(4).begin == 134 && sentences.get(4).end == 169);
        assert (sentences.get(5).begin == 177 && sentences.get(5).end == 224);
        assert (sentences.get(6).begin == 238 && sentences.get(6).end == 249);
        assert (sentences.get(7).begin == 255 && sentences.get(7).end == 298);
        assert (sentences.get(8).begin == 308 && sentences.get(8).end == 375);
        assert (sentences.get(9).begin == 392 && sentences.get(9).end == 439);
        assert (sentences.get(10).begin == 457 && sentences.get(10).end == 500);
    }


    @Test
    public void test315() {
        String input =  "Comments: High cholestrol**************** \n" +
                "(-) GERD \n" ;
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 25);
        assert (sentences.get(1).begin == 43 && sentences.get(1).end == 51);
    }
    @Test
    public void test32() {
        String input = "****** \n" +
                " \n" +
                "(+) History of TIA/CVA (2000) \n" +
                "****Type: TIA******** Deficits include: right hand, resolved. ****Has no current symptoms, " +
                "****treated with Plavix. ********Patient is not anticoagulated. **** \n" +
                "(-) History of Neuromuscular disease  \n" +
                "******************** \n" +
                " \n" +
                "(+) Psychiatric History.  \n" +
                "****Diagnosis: depression. ********Treated with lexapro. ******";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 10 && sentences.get(0).end == 39);
        assert (sentences.get(1).begin == 45 && sentences.get(1).end == 102);
        assert (sentences.get(2).begin == 107 && sentences.get(2).end == 156);
        assert (sentences.get(3).begin == 165 && sentences.get(3).end == 195);
        assert (sentences.get(4).begin == 202 && sentences.get(4).end == 238);
        assert (sentences.get(5).begin == 265 && sentences.get(5).end == 289);
        assert (sentences.get(6).begin == 296 && sentences.get(6).end == 318);
        assert (sentences.get(7).begin == 327 && sentences.get(7).end == 355);
    }

    @Test
    public void test33() {
        String input = "Cardiovascular:  ** \n" +
                "ROS comment: ECG 8/11/16- Sinus tachycardia, 104, incomplete RBBB, left anterior " +
                "fascicular block, nonspecific ST changed, T waves inverted in lateral leads- per ED note- no tracing sent. \n" +
                " \n" +
                "CHF exacerbation in 5/14************ \n" +
                "(+) History of Congestive Heart Failure: Stage.  \n" +
                "********Date of last echo: 8/11/2014. ****EF: 73%****, LAE, mod LVH, preserved " +
                "systolic function, mod diastolic dysfunction. ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 15);
        assert (sentences.get(1).begin == 21 && sentences.get(1).end == 208);
        assert (sentences.get(2).begin == 212 && sentences.get(2).end == 236);
        assert (sentences.get(3).begin == 250 && sentences.get(3).end == 297);
        assert (sentences.get(4).begin == 308 && sentences.get(4).end == 424);
    }

    @Test
    public void test34() {
        String input = "\n · Influenza Vaccine Ordered (September - March only): No:  Not currently flu season" +
                "    · Pneumococcal Vaccine Ordered: Yes:  Patient age 2-64 years who has asthma OR is a smoker" +
                "    · Immunization Comments:     · Immunization History from Electronic Medical Record:" +
                "    Immunization History   Administered Date(s) Administered   ¿ Pneumovax 23 (Pneumococcal) 05/07/2014\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 4 && sentences.get(0).end == 85);
        assert (sentences.get(1).begin == 89 && sentences.get(1).end == 179);
        assert (sentences.get(2).begin == 183 && sentences.get(2).end == 207);
        assert (sentences.get(3).begin == 212 && sentences.get(3).end == 369);
    }

    @Test
    public void test35() {
        String input = "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Progress Notes by Keminder Qerala, MD at 11/11/2014 9:10 AM (continued) \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Progress Notes by Keminder Qerala, MD at 11/11/2014 9:10 AM (continued) \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Progress Notes by Keminder Qerala, MD at 11/11/2014 9:10 AM \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "    Author: Keminder Qerala, MD     Service: General Surgery  Author Type: Physician \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Filed: 11/15/2014 1:53 PM     Note Time: 11/11/2014 9:10 AM  Note Type: Progress Notes \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Status: Signed     Editor: Keminder Qerala, MD (Physician)    \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Related Notes:  Original Note by Phoelat Gae, MD (Resident) filed at 11/11/2014 9:24 AM       \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "Surgery Progress Note\n" +
                "\n" +
                "\n" +
                "\n" +
                "Hospital Day: 2\n" +
                "\n" +
                "\n" +
                "\n" +
                "Current Unit: UH (NAC) NEURO ACUTE CARE \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Subjective: ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 8 && sentences.get(0).end == 79);
        assert (sentences.get(1).begin == 90 && sentences.get(1).end == 161);
        assert (sentences.get(2).begin == 178 && sentences.get(2).end == 237);
        assert (sentences.get(3).begin == 250 && sentences.get(3).end == 330);
        assert (sentences.get(4).begin == 339 && sentences.get(4).end == 425);
        assert (sentences.get(5).begin == 434 && sentences.get(5).end == 492);
        assert (sentences.get(6).begin == 504 && sentences.get(6).end == 591);
        assert (sentences.get(7).begin == 604 && sentences.get(7).end == 625);
        assert (sentences.get(8).begin == 629 && sentences.get(8).end == 644);
        assert (sentences.get(9).begin == 648 && sentences.get(9).end == 687);
        assert (sentences.get(10).begin == 695 && sentences.get(10).end == 706);
    }

    @Test
    public void test36() throws Exception {
        String input = "University        - Hospital  Gastroenterology Endoscopy Center" +
                "  ______________________________________________________________________________  _  " +
                "Patient Name: xxxxx           Procedure Date: 3/6/2014 11:21 AM  MRN: 113333333    " +
                "                    Date of Birth: 10/06/1964  Admit Type: Inpatient                 " +
                "Age: 33  Gender: Female                        Note Status: Finalized  Attending MD: Jon C Kang, MD           ______________________________________________________________________________  _     Procedure:           Upper GI endoscopy  Indications:  ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\n+", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 0 && sentences.get(0).end == 63);
        assert (sentences.get(1).begin == 148 && sentences.get(1).end == 167);
        assert (sentences.get(2).begin == 178 && sentences.get(2).end == 227);
        assert (sentences.get(3).begin == 251 && sentences.get(3).end == 299);
        assert (sentences.get(4).begin == 316 && sentences.get(4).end == 339);
        assert (sentences.get(5).begin == 363 && sentences.get(5).end == 415);
    }

    @Test
    public void test37() throws Exception {
        String input = "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Progress Notes signed by Gerkareg Rose Runtine, LCSW at 6/5/2014 12:20 PM \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "    Author:  Gerkareg Rose Runtine, LCSW  Service:  (none)  Author Type:  Licensed Clinical Social Worker \n" +
                "\n" +
                "\n" +
                "\n" +
                "    Filed:  6/5/2014 12:20 PM  Note Time:  6/5/2014 12:19 PM  Note Type:  Progress Notes \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "   \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "The patient would like a fax sheet at discharge so he can fax his Advanced Directive to us once he returns home. \n" +
                "\n" +
                "\n" +
                "\n" +
                "Gerkareg R. Runtine, LCSW\n" +
                "\n" +
                "\n" +
                "\n" +
                "(812)233-3316\n" +
                "\n" +
                "\n" +
                "\n" +
                " \n" +
                "\n" +
                "\n" +
                "\n" +
                " \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Progress Notes signed by Gerkareg Rose Runtine, LCSW at 6/5/2014 12:20 PM (continued) \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " Progress Notes signed by Gerkareg Rose Runtine, LCSW at 6/5/2014 12:20 PM (continued) \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                " \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "      \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "      \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\\n", " ");
        printDetails(sentences, input,debug);
        assert (sentences.get(0).begin == 7 && sentences.get(0).end == 80);
        assert (sentences.get(1).begin == 93 && sentences.get(1).end == 194);
        assert (sentences.get(2).begin == 203 && sentences.get(2).end == 287);
        assert (sentences.get(3).begin == 305 && sentences.get(3).end == 417);
        assert (sentences.get(4).begin == 422 && sentences.get(4).end == 447);
        assert (sentences.get(5).begin == 451 && sentences.get(5).end == 464);
        assert (sentences.get(6).begin == 482 && sentences.get(6).end == 567);
        assert (sentences.get(7).begin == 578 && sentences.get(7).end == 663);
    }

    @Test
    public void test38() throws Exception {
        String input = "D: bad:530";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\\n", " ");
        printDetails(sentences, input,debug);
    }

    @Test
    public void test39() {
        String input = "Campbell Orthopedic Associates\n" +
                "4 Madera Circle\n" +
                "Omak, GA 28172\n" +
                " \n" +
                "Habib Valenzuela, M.D.\n" +
                " \n" +
                " \n" +
                "                                             Valdez, Harlan Jr.  \n" +
                "                                           845-41-54-4\n" +
                "                                             February 12, 2106 \n" +
                "Har is a 43 year old 6' 214 pound gentleman who is referred for\n" +
                "consultation by Dr. Harlan Oneil.  About a week ago he slipped on\n" +
                "the driveway at home and sustained an injury to his left ankle. ";
        ArrayList<Span> sentences = seg.segToSentenceSpans(input);
        input = input.replaceAll("\\n", " ");
        printDetails(sentences, input, debug);
    }
}