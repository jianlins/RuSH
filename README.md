# RuSH

RuSH (Rule-based Segmentation using Hashing) is an efficient, reliable, and easy adaptable rule-based sentence segmentation
solution that can be used in an NLP pipeline either in-line or to pre-annotate clinical texts. It leverages a nested
hash table data structure to execute simultaneous rule processing, which reduces the impact of the rule-base growth
on execution time and eliminates the effect of rule order on accuracy. 

If you wish to cite RuSH in a publication, please use:

>Jianlin Shi ; Danielle Mowery ; Kristina M. Doing-Harris ; John F. Hurdle.RuSH: a Rule-based Segmentation Tool Using Hashing for Extremely Accurate Sentence Segmentation of Clinical Text. AMIA Annu Symp Proc. 2016: 1587. 

The full text can be found at:

https://knowledge.amia.org/amia-63300-1.3360278/t005-1.3362920/f005-1.3362921/2495498-1.3363244/2495498-1.3363247?timeStamp=1479743941616


## How to use

A standalone RuSH class is available to be directly used in your code. 

```
RuSH segmenter = new RuSH("conf/rush_rules.csv");

String input = "The patient was admitted on 03/26/08\n and was started on IV antibiotics elevation" +
             ", was also counseled to minimizing the cigarette smoking. The patient had edema\n\n" +
             "\n of his bilateral lower extremities. The hospital consult was also obtained to " +
             "address edema issue question was related to his liver hepatitis C. Hospital consult" +
             " was obtained. This included an ultrasound of his abdomen, which showed just mild " +
             "cirrhosis. ";
                
ArrayList<Span> sentences = segmenter.segToSentenceSpans(input);
```

A UIMA analyses engine that wraps RuSH up is also
available. The type system in this AE is dynamically coded, so that you can directly plug the AE into your own UIMA pipeline.
If you are using native UIMA, the AE descripter can be found [here](https://github.com/jianlins/RuSH/blob/master/desc/RuSH_aeDescriptor.xml).
If you are using UIMAFit, please refer to the example [TestRuSH_AE.java](https://github.com/jianlins/RuSH/blob/master/src/test/java/edu/utah/bmi/RuSH/TestRuSH_AE.java).

Example codes can be found under src/test directory. When adapting RuSH to your local corpus, it is also an efficient way to make 
test cases while modifying RuSH rules. It will help you easily keep track of the errors that your new rules may introduce.


