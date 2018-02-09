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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BenchMarkRuSHs {
    protected static ArrayList<String> data1, data3, data4;
    protected static String ruleStr = "conf/rush_rules_v3.xlsx";
    protected static String dir = "/home/brokenjade/IdeaProjects/RuSHBenchmark/data";

    static int times = 40;

    public boolean tokenize = true;
    private boolean verbose = false;
    private final static int executeTimes = 20;
    final static int numImpls = 3;
    protected static ArrayList<String>[] datas = new ArrayList[numImpls];

    private static float[] averages = new float[numImpls - 1];
    private static RuSHInf[] impls = new RuSHInf[numImpls];
    private static String[] names = new String[numImpls];
    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @BeforeClass
    public static void init() throws IOException {
        initData(dir);
        impls[0] = new RuSH3(ruleStr);
        impls[1] = new RuSH2(ruleStr);
        impls[2] = new RuSH(ruleStr);
        for (int i = 0; i < numImpls; i++) {
            names[i] = impls[i].getClass().getSimpleName();
        }
    }

    protected static void initData(String dir) throws IOException {
        datas[0] = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(new File(dir), new String[]{"txt"}, false);
        for (File file : files) {
            datas[0].add(FileUtils.readFileToString(file));
        }
        for (int i = 1; i < numImpls; i++) {
            datas[i] = (ArrayList<String>) datas[0].clone();
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


        for (int i = 0; i < numImpls; i++) {
            ths[i] = createThread(impls[i], names[i], datas[i], gate, gate2, ts[i]);
        }

        for (int i = 0; i < numImpls; i++) {
            ths[i].start();
        }

        try {
            gate.await();
            gate2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private Thread createThread(RuSHInf rush, String name, ArrayList<String> data, CyclicBarrier gate1, CyclicBarrier gate2, long[] t) {
        Thread th = new Thread(() -> {
            try {
                if (verbose)
                    System.out.println(name + " ready... waiting for others...");
                gate1.await();
                if (verbose)
                    System.out.println("Run " + name);
                long ts = System.currentTimeMillis();
                for (int i = 0; i < times; i++)
                    for (String text : data) {
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

