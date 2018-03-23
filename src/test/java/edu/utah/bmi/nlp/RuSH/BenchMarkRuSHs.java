package edu.utah.bmi.nlp.RuSH;

import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.rush.core.RuSH;
import edu.utah.bmi.nlp.rush.core.RuSH2;
import edu.utah.bmi.nlp.rush.core.RuSH3;
import edu.utah.bmi.nlp.rush.core.RuSHInf;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import repeat.Repeat;
import repeat.RepeatRule;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CyclicBarrier;

public class BenchMarkRuSHs {
    protected static String ruleStr = "conf/rush_rules.tsv";
    protected static String dir = "../RuSHBenchmark/data";

    static int times = 50;

    public boolean tokenize = true;
    private boolean verbose = false;
    private final static int executeTimes = 50;
    final static int numImpls = 3;
    protected static ArrayList<ConcurrentSkipListMap<String, String>> datas = new ArrayList();

    private static float[] averages = new float[numImpls - 1];
    private static RuSHInf[] impls = new RuSHInf[numImpls];
    private static String[] names = new String[numImpls];
    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @BeforeClass
    public static void init() throws IOException {
        initData(dir);
        String tokenRule =
                "\\b(\\a\t0\tstbegin\n" +
                        "\\a\\e\t2\tstend\n" +
                        "\\C\t0\ttobegin\n" +
                        "\\C)\\w\t2\ttoend\n" +
                        "\\C)\\p\t2\ttoend\n" +
                        "\\C)\\d\t2\ttoend\n" +

                        "\\c\t0\ttobegin\n" +
                        "\\c)\\w\t2\ttoend\n" +
                        "\\c)\\p\t2\ttoend\n" +
                        "\\c)\\d\t2\ttoend\n" +

                        "\\d\t0\ttobegin\n" +
                        "\\d(\\d\t1\ttobegin\n" +

                        "\\d)\\c\t2\ttoend\n" +
                        "\\d)\\C\t2\ttoend\n" +
                        "\\d)\\w\t2\ttoend\n" +
                        "\\d)\\p\t2\ttoend\n" +
                        "\\d).\\d\t3\ttoend\n" +
                        "\\d)[| +].[| +]\\d\t3\ttoend\n" +
                        "\\d)[| +]+/[| +]\\d\t3\ttoend\n" +
                        "\\a\\e\t2\ttoend\n" +

                        "\\c\t0\ttobegin\n" +
                        "\\c(\\c\t1\ttobegin\n" +


                        "\\p\t0\ttobegin\n" +
                        "\\p)\\d\t2\ttoend\n" +
                        "\\d[| +](.)[| +]\\d\t3\ttoend\n" +
                        "\\d[| +](/)[| +]\\d\t3\ttoend\n" +
                        "\\p)\\c\t2\ttoend\n" +
                        "\\p)\\C\t2\ttoend\n" +
                        "\\p)\\w\t2\ttoend\n";

        impls[0] = new RuSH3(ruleStr);
        impls[1] = new RuSH2(ruleStr);
        impls[2] = new RuSH(ruleStr);
//        impls[0] = new RuSH3(FileUtils.readFileToString(new File(ruleStr)) + "\n" + tokenRule);
        for (int i = 0; i < numImpls; i++) {
            names[i] = impls[i].getClass().getSimpleName() + "_" + i;
        }
    }

    protected static void initData(String dir) throws IOException {
        ConcurrentSkipListMap<String, String> data = new ConcurrentSkipListMap<>();
        Collection<File> files = FileUtils.listFiles(new File(dir), new String[]{"txt"}, false);
        for (File file : files) {
            data.put(file.getName(), FileUtils.readFileToString(file));
        }
        datas.add(data);
        for (int i = 1; i < numImpls; i++) {
            datas.add(data.clone());
        }
    }

    @Test
    @Repeat(times = executeTimes, threads = 1)
    public void benchmark() {
        long[][] ts = new long[numImpls][1];
        Thread[] ths = new Thread[numImpls];

        Runnable barrierAction = () -> {
            if (verbose)
                System.out.println(Thread.currentThread().getName() + " execution finished. ");
            System.out.println("Improvement:");
            float[] improves = new float[numImpls - 1];
            for (int i = 0; i < numImpls - 1; i++) {
                improves[i] = 100.0f * (ts[0][0] - ts[i + 1][0]) / ts[0][0];
                averages[i] += improves[i];
            }
            TreeMap<Float, String> output = new TreeMap();
            for (int i = 0; i < improves.length; i++) {
                output.put(improves[i], names[i + 1]);
            }
            for (Map.Entry<Float, String> entry : output.entrySet()) {
                System.out.println("\t" + entry.getValue() + ": " + entry.getKey() + "%");
            }
        };
        CyclicBarrier gate = new CyclicBarrier(numImpls + 1);
        CyclicBarrier gate2 = new CyclicBarrier(numImpls + 1, barrierAction);


        ArrayList<Integer> startOrder = new ArrayList<>();
        System.out.print("Execute order: \t");
        for (int i = 0; i < numImpls; i++) {
            ths[i] = createThread(impls[i], names[i], datas.get(i), gate, gate2, ts[i]);
            startOrder.add(i);

        }

        Collections.shuffle(startOrder);
        for (int i = 0; i < numImpls; i++) {
            ths[startOrder.get(i)].start();
            System.out.print(names[startOrder.get(i)]+"\t");
        }
        System.out.println("");
        try {
            gate.await();
            gate2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private Thread createThread(RuSHInf rush, String name, ConcurrentSkipListMap<String, String> data, CyclicBarrier gate1, CyclicBarrier gate2, long[] t) {
        Thread th = new Thread(() -> {
            try {
                if (verbose)
                    System.out.println(name + " ready... waiting for others...");
                gate1.await();
                if (verbose)
                    System.out.println("Run " + name);
                long ts = System.currentTimeMillis();
                for (int i = 0; i < times; i++)
                    for (String fileName : data.keySet()) {
//                        System.err.println(fileName);
                        String text = data.get(fileName);
                        ArrayList<Span> sentences = rush.segToSentenceSpans(text);
                        if (tokenize)
                            rush.tokenize(sentences, text);
                    }
                t[0] = System.currentTimeMillis() - ts;
                if (verbose)
                    System.out.println(name + ":" + t[0]);
                gate2.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        });
        return th;
    }


    @AfterClass
    public static void report() {
        System.out.println("\nAveraged improvement:");
        TreeMap<Float, String> output = new TreeMap<>();
        for (int i = 0; i < averages.length; i++) {
            output.put(averages[i] / executeTimes, names[i + 1]);
        }
        for (Map.Entry<Float, String> entry : output.entrySet()) {
            System.out.println("\t" + entry.getValue() + ": " + entry.getKey() + "%");
        }
    }
}

