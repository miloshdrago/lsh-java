import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

/**
 * The Runner can be ran from the commandline to find the most similar pairs
 * of StackOverflow questions.
 */
public class Runner {

    public static void main(String[] args) throws IOException {
        // Start a separate thread to monitor memory usage every 5 seconds
        CheckMem CheckMemLoop = new CheckMem(5);
        // print out the memory statistics
        CheckMemLoop.printCheckMem(false);
        Thread CheckMemThread = new Thread(CheckMemLoop);
        // start the separat thread
        CheckMemThread.start();


        // Default parameters
        String dataFile = "";
        //String dataFile = strDataPath + "Questions.xml";
        String testFile = null;
        //String testFile = strDataPath + "Duplicates.xml";
        String outputFile = "";
        //String outputFile = strDataPath + "Output_" + strDate +".xml";
        String signaturesFile = null;
        String method = "";
        int numHashes = -1;
        int numShingles = 1000;
        int numBands = -1;
        int numBuckets = 2000;
        int seed = 7825942;
        int maxQuestions = -1;
        int shingleLength = -1;
        float threshold = -1;

        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            String arg = args[i];
            if (arg.equals("-method")) {
                if (!args[i+1].equals("bf") && !args[i+1].equals("lsh")){
                    System.err.println("The search method should either be brute force (bf) or minhash and locality sensitive hashing (lsh)");
                }
                method = args[i+1];
            }else if(arg.equals("-numHashes")){
                numHashes = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-numBands")){
                numBands = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-numBuckets")){
                numBuckets = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-numShingles")){
                numShingles = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-seed")){
                seed = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-dataFile")){
                dataFile = args[i+1];
            }else if(arg.equals("-testFile")){
                testFile = args[i+1];
            }else if(arg.equals("-signaturesFile")){
                signaturesFile = args[i+1];
            }else if(arg.equals("-outputFile")){
                outputFile = args[i+1];
            }else if(arg.equals("-maxQuestions")){
                maxQuestions = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-shingleLength")){
                shingleLength = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-threshold")){
                threshold = Float.parseFloat(args[i+1]);
            }
            i += 2;
        }

        Shingler shingler = new Shingler(shingleLength, numShingles);

        DataHandler dh = new DataHandler(maxQuestions, shingler, dataFile);
        SimilaritySearcher searcher = null;

        if (method.equals("bf")){
            searcher = new BruteForceSearch(dh, threshold);
        }else if(method.equals("lsh")){
            if(numHashes == -1 || numBands == -1){
                throw new Error("Both -numHashes and -numBands are mandatory arguments for the LSH method");
            }
            // control that the numHashes parameter is a multiple of numBands parameter
            // if not then change numHashes to the next multiple of numBands
            int row_per_band = numHashes / numBands;
            int remainder = numHashes % numBands;
            if (remainder > 0) {
                System.out.print("numHashes changed from " + numHashes + " to ");
                numHashes = (row_per_band + 1) * numBands;
                System.out.println(numHashes + " for all bands to be equal");
            }
            Random rand = new Random(seed);
            searcher = new LSH(dh, threshold, numHashes, numBands, numBuckets, numShingles, rand);
        }

        long startTime = System.currentTimeMillis();

        System.out.println("Searching items more similar than " + threshold + " ... ");
        Set<SimilarPair> similarItems = searcher.getSimilarPairsAboveThreshold();
        System.out.println("done! Took " +  (System.currentTimeMillis() - startTime)/1000.0 + " seconds.");
        System.out.println("--------------");

        if(method.equals("lsh") && signaturesFile != null){
            /* FILL IN HERE */
            LSH.saveSignatureMatrix(signaturesFile);
        }
        savePairs(outputFile, similarItems);
        if (testFile != null)
            testPairs(similarItems, SimilarPairParser.read(testFile), dh.idToDoc);
        // print out the maximum memory used
        System.out.println("Maximum memory used " + CheckMemLoop.getMaxmem() + " MB");
        // set flag to false for separate thread to stop running
        CheckMemLoop.stopCheckMem();
    }



    /**
     * Save pairs and their similarity.
     * @param similarItems
     */
    public static void savePairs(String outputFile, Set<SimilarPair> similarItems){
        try {
            SimilarPairWriter.save(outputFile, similarItems);
            System.out.println("Found " + similarItems.size() + " similar pairs, saved to '" + outputFile + "'");
            System.out.println("--------------");
        } catch (FileNotFoundException e) {
            System.err.println("The file '" + outputFile + "' does not exist!");
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public static void testPairs(Set<SimilarPair> results, Set<SimilarPair> references, List<Integer> processedDocs) {
        int truePosCount = 0;
        int falsePosCount = 0;
        int falseNegCount = 0;
        for (SimilarPair result : results) {
            if (references.contains(result)) {
                truePosCount++;
            } else {
                falsePosCount++;
            }
        }
        for (SimilarPair reference : references) {
            if (processedDocs.contains(reference.id1)
                    && processedDocs.contains(reference.id2)
                    && !results.contains(reference)) {
                falseNegCount++;
            }
        }
        double precision = (double) truePosCount / (truePosCount + falsePosCount);
        double recall = (double) truePosCount / (truePosCount + falseNegCount);
        double f1 = 2d*truePosCount / (2*truePosCount + falsePosCount + falseNegCount);
        System.out.println("Test results:");
        System.out.println("TP: " + truePosCount
                + ", FP: " + falsePosCount
                + ", FN: " + falseNegCount
                + ", F1: " + f1
                );
    }

}
