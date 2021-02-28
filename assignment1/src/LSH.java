/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved.
 * Proprietary, do not copy or distribute without permission.
 * Written by Pieter Robberechts, 2020
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Implementation of minhash and locality sensitive hashing (lsh) to find
 * similar objects.
 *
 */
public class LSH extends SimilaritySearcher {
    /* FILL IN HERE */
    private long[][] HashesParametersMatrix;
    private static ArrayList<int[]> signatureMatrix = new ArrayList<int[]>();
    private static List<Integer> documentIDs;
    private Map<Integer, List<Integer>> OneBandbuckets = new LinkedHashMap<Integer, List<Integer>>();
    private static int prime;
    private static int readDocCount; // real number of questions read
    private static int numHashes;
    private static int numBands;
    private static int numBuckets;
    private static Random rand;

    /**
     * Construct an LSH similarity searcher.
     *
     * @param reader a data Reader object
     * @param threshold the similarity threshold
     * @param numHashes number of hashes to use to construct the signature matrix
     * @param numBands number of bands to use during locality sensitive hashing
     * @param numBuckets number of buckets to use during locality sensitive hashing
     * @param numValues the number of unique values that occur in the objects'
     *                  set representations (i.e. the number of rows of the 
     *                  original characteristic matrix)
     * @param rand should be used to generate any random numbers needed
     */
    public LSH(
            Reader reader, double threshold, int numHashes, int numBands, 
            int numBuckets, int numValues, Random rand) throws IOException {
        super(reader, threshold);
        // initialise the private variables used in the functions of this class
        LSH.rand = rand;
        LSH.numHashes=numHashes;
        LSH.numBands=numBands;
        LSH.numBuckets=numBuckets;
        prime  = Primes.findLeastPrimeNumber(numValues);
        HashesParametersMatrix = new long[numHashes][2];
        // build the Hash functions parameters table
        constructHashParametersTable();
        // read the requested number of questions or the number of questions available in the file
        readDocCount = readAllQuestionsandComputeSignatureMatrix() + 1;
        documentIDs= reader.idToDoc;
        System.out.println("Number of documents really read : "  + readDocCount);
        //saveSignatureMatrix("C:\\TEMP\\AS1\\V1_signatures.CSV");

    }


    /**
     * Consruct the table of a and b values for the hash functions
     */
    private void constructHashParametersTable() {
        boolean notduplicate ;
        for (int j = 0; j < numHashes; j++) {
            do {
                // generate parameter a but should not have a value of 0
                do {
                    HashesParametersMatrix[j][0] = rand.nextInt(prime);
                } while (HashesParametersMatrix[j][0] == 0);
                HashesParametersMatrix[j][1] = rand.nextInt(prime);
                // check if the generated pair is a duplicate of already generated ones
                notduplicate=true;
                for (int z = 0; z<j;z++) {
                    if ((HashesParametersMatrix[j][0] == HashesParametersMatrix[z][0]) & (HashesParametersMatrix[j][1] == HashesParametersMatrix[z][1])) {
                        // this a & b pair already exist we need to generate another one
                        notduplicate = false;
                        break;
                    }
                }
            } while (!notduplicate);
        }
        System.out.println("Hashes Parameter Matrix is generated");
    }


    /**
     * Read all maxDocs documents one by one and compute their signatures.
     */
    private int readAllQuestionsandComputeSignatureMatrix(){
        reader.reset();
        //System.out.println("Curdoc =" + reader.curDoc + Runner.printmem());
        //  boolean variable to indicated that we reached the end of the questions file
        boolean endoffile = false;
        while (reader.hasNext() &(!endoffile)){
            Set<Integer> shingle = reader.next();
            // check if the returned value is null as it means that we reached the end of the questions file and we need to exit the while loop
            if (shingle == null) {
                System.out.println("Reached the end of the Questions file at : " + reader.curDoc);
                endoffile = true;
            } else {
                // add the signature of this shingle to the signature matrix
                AddDocumentSignature(shingle);
                //reader.idToShingle.add(shingle);
            }
        }
        return reader.curDoc;
    }


    /** Construct the document signature from its shingles and the hash functions
     * @param shingle the shingle set for which the signature is computed and added in the signature matrix
     */
    private void AddDocumentSignature(Set<Integer> shingle) {
        //System.out.println("Doc " + CurDoc + " shingle size " + shingle.size() + "signature ");
        // initiate an empty array of int
        int[] oneHashRow = new int[numHashes];
        // variable used for long to int conversions and comparisons
        long templong;
        int HashedMinValue;

        for (int hash_id = 0; hash_id < numHashes; hash_id++) {
           // initialize variable with maximum posible int value
            HashedMinValue = Integer.MAX_VALUE;
            for (int shingle_id : shingle ) {
                // compute in long type the value of the hash and they compare it with the current min value
                templong = (( (HashesParametersMatrix[hash_id][0] *  (long) shingle_id) +  HashesParametersMatrix[hash_id][1]) %  (long) prime);
                if ((int) templong < HashedMinValue) {
                    HashedMinValue = (int) templong;
                }
            }
            // add the minimum hash value for this hash_if function
            oneHashRow[hash_id]=HashedMinValue;
        }
        // add the row with the document signature to the signature matrix
        signatureMatrix.add(oneHashRow);
    }


    /**
     * Get the SET of integers from the signature matrix for one specific question
     * @param doc_id document in the signature matrix for which we want the signature
     */
    private Set<Integer> GetSignatureSet(int doc_id) {
        return Arrays.stream(signatureMatrix.get(doc_id)).boxed().collect(Collectors.toSet());
    }


    /**
     * Construct the buckets map for one specific band.
     * @param band_id Id of the band for which we build the buckets
     * @return the Map of List of Integers where the Map Key value is the Bucket number and the List of Integers represents the candidate pairs
     */
    private Map<Integer, List<Integer>> buildBucketMatrixForOneBand(int band_id ) {
        OneBandbuckets = new LinkedHashMap<Integer, List<Integer>>();
        int numRowsPerBand = numHashes / numBands;
        // for each band generate a different seed
        int seed = rand.nextInt();
        for (int doc_id=0 ; doc_id < readDocCount; doc_id++) {
            // for each question generate an empty string to store the hashes of this band and this document
            StringBuilder strSignature = new StringBuilder(15 * numRowsPerBand);
            // build the string by appending each hash function value
            for (int row_id=0 ; row_id < numRowsPerBand; row_id++){
                strSignature.append(signatureMatrix.get(doc_id)[band_id * numRowsPerBand + row_id]);
                strSignature.append(" ");
            }
            // hash this band_id string and put it in a bucket
            int hash = MurmurHash.hash32(strSignature.toString(), seed);
            int bucket = Math.abs(hash) % numBuckets;
            // check if we have already this bucket key value in this band, if not initialise an empty arraylist in the Map
            OneBandbuckets.computeIfAbsent(bucket, k -> new ArrayList<>());
            // add the document internal number (0 ... N questions) in the specific bucket
            OneBandbuckets.get(bucket).add(doc_id);
        }
        return OneBandbuckets;
    }

    /**
     * Save the Signatures Matrix to a CSV file
     * @param signaturesFile String representing the path and name of the CSV file
     */
    public static void saveSignatureMatrix(String signaturesFile) throws IOException {
        long startTimeSigCSV = System.currentTimeMillis();
        System.out.println("Saving signatures file");
        File file = new File(signaturesFile);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        StringBuilder str4csv = new StringBuilder(10 * readDocCount);
        int doc_id;
        // write in the first line the question IDS separated by commas
        for (doc_id=0 ; doc_id <readDocCount -1; doc_id++) {
            str4csv.append(documentIDs.get(doc_id));
            str4csv.append(",");
        }
        // write the last document ID and return to next line
        str4csv.append(documentIDs.get(doc_id));
        str4csv.append(System.lineSeparator());
        bw.write(str4csv.toString());

        // now run through the hash functions to export to CSV
        int hash_id;
        // loop through the hash functions
        for (hash_id=0 ; hash_id<numHashes; hash_id++) {
            // reset the string
            str4csv.setLength(0);
            // write on one line the hash function values for each of the questions separated by a comma
            for (doc_id=0 ; doc_id <readDocCount -1; doc_id++) {
                str4csv.append(LSH.signatureMatrix.get(doc_id)[hash_id]);
                str4csv.append(",");
            }
            str4csv.append(LSH.signatureMatrix.get(doc_id)[hash_id]);
            str4csv.append(System.lineSeparator());
            bw.write(str4csv.toString());
        }
        bw.close();
        System.out.println("Signatures File written Successfully");
        System.out.println("done saving signature file! Took " +  (System.currentTimeMillis() - startTimeSigCSV)/1000.0 + " seconds.");

    }

    /**
     * Returns the pairs with similarity above threshold (approximate).
     */
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold() {
        Set<SimilarPair> similarPairsAboveThreshold = new HashSet<SimilarPair>();
        int docs_in_bucketsize;
        int number_comparaisons =0;
        for (int band_id=0 ; band_id < numBands; band_id++){
             // Compute the buckets for this band
            OneBandbuckets = buildBucketMatrixForOneBand(band_id);
            // System.out.println("Key set " +thebuckets.get(band_id).keySet());
            for (int bucket: OneBandbuckets.keySet()) {
                docs_in_bucketsize = OneBandbuckets.get(bucket).size();
                //check if there is more than 1 document in the bucket
                if (docs_in_bucketsize>1) {
                    for (int i=0; i < docs_in_bucketsize - 1; i++) {
                        int doc1 = OneBandbuckets.get(bucket).get(i);
                        for (int j=i+1; j < docs_in_bucketsize ; j++) {
                            int doc2 = OneBandbuckets.get(bucket).get(j);
                            // Calculate similarity based on signatures hashes and not on shingles
                            double sim = jaccardSimilarity(GetSignatureSet(doc1), GetSignatureSet(doc2));
                            number_comparaisons++;
                            if (sim > threshold){
                                similarPairsAboveThreshold.add(new SimilarPair(reader.getExternalId(doc1), reader.getExternalId(doc2), sim));
                            }
                        }
                    }

                }
            }
            // kick the Garbage collector to clean up and reuse some of the free memory
            System.gc();
            System.out.println("Band " + band_id + " ==> number of candidate pairs: "+ similarPairsAboveThreshold.size()
                    + " comparisons:" +number_comparaisons );
        }
        return similarPairsAboveThreshold;
    }

}
