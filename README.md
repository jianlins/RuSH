# RuSH (*R*ule-based sentence *S*egmenter using *H*ashing)

RuSH is an efficient, reliable, and easy adaptable rule-based sentence segmentation
solution. It is specifically designed to handle the telegraphic written text in clinical note. It leverages a nested
hash table to execute simultaneous rule processing, which reduces the impact of the rule-base growth
on execution time and eliminates the effect of rule order on accuracy. 

A python version is also available: [PyRuSh](https://github.com/jianlins/PyRuSH).

If you wish to cite RuSH in a publication, please use:

>Jianlin Shi ; Danielle Mowery ; Kristina M. Doing-Harris ; John F. Hurdle.RuSH: a Rule-based Segmentation Tool Using Hashing for Extremely Accurate Sentence Segmentation of Clinical Text. AMIA Annu Symp Proc. 2016: 1587. 

The full text can be found [here](https://knowledge.amia.org/amia-63300-1.3360278/t005-1.3362920/f005-1.3362921/2495498-1.3363244/2495498-1.3363247?timeStamp=1479743941616):




## How to use

A standalone RuSH class is available to be directly used in your code. 

```java
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
- If you use native UIMA, the AE descripter can be found [here](https://github.com/jianlins/RuSH/blob/master/desc/RuSH_aeDescriptor.xml).
- If you use UIMAFit, please refer to the example [TestRuSH_AE.java](https://github.com/jianlins/RuSH/blob/master/src/test/java/edu/utah/bmi/RuSH/TestRuSH_AE.java).

Example codes can be found under [src/test](https://github.com/jianlins/RuSH/tree/master/src/test/java/edu/utah/bmi) directory. When adapting RuSH to your local corpus, it is also an efficient way to make 
test cases while modifying RuSH rules. It will help you easily keep track of the errors that your new rules may introduce.


## Maven set up:

If you prefer to use the published version on maven central, you can configure your maven dependency in pom as following:
```
<dependency>
  <groupId>edu.utah.bmi.nlp</groupId>
  <artifactId>rush</artifactId>
  <version>3.0</version>
</dependency>
```
Note: if this maven distribution does not include the RuSH rule file, you will need to download it from [here](https://github.com/jianlins/RuSH/blob/master/conf/rush_rules.csv). 


## What's new in RuSH 3.0
RuSH 3.0 use a slight different implementation to segment sentences, once sentence boundaries are recognized by FastNER. This newer version has 15~20% speed improvement based on a [benchmark test](src/test/java/edu/utah/bmi/nlp/RuSH/BenchMarkRuSHs.java).