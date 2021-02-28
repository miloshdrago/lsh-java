/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved.
 * Proprietary, do not copy or distribute without permission.
 * Written by Pieter Robberechts, 2020
 */
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Reads a set of documents and constructs shingle representations for these
 * documents.
 */
public abstract class Reader {

    // maps a doc id to its shingle representation
    protected List<Set<Integer>> idToShingle = new ArrayList<Set<Integer>>();
    // a shingler
    public Shingler shingler;
    // max number of docs to read
    protected int maxDocs;
    // maps each doc's internal id to its external id
    public List<Integer> idToDoc = new ArrayList<Integer>();
    // number of docs read
    protected int curDoc;

    /**
     * Construct a new document reader.
     * @param maxDocs maximal number of documents to read.
     * @param shingler a document shingler.
     */
    public Reader(int maxDocs, Shingler shingler){
        this.shingler = shingler;
        this.maxDocs = maxDocs;
        this.curDoc = 0;
    }

    /**
     * Read the next document.
     * @return the shingle representation for the next document.
     */
    abstract public Set<Integer> next();

    /**
     * Reset this reader.
     */
    abstract public void reset();

    /**
     * Check whether there are more documents.
     * @return True if there are more documents; otherwise False.
     */
    public boolean hasNext(){
        return this.curDoc < this.maxDocs - 1;
    }

    /**
     * Get the mapping of the object id to its set representation.
     * @return the mapping
     */
    public List<Set<Integer>> getObjectMapping() {
        return idToShingle;
    }

    /**
     * Read all maxDocs documents at once.
     */
    public void readAll(){
        reset();
        while (this.hasNext()){
            Set<Integer> shingle = this.next();
            this.idToShingle.add(shingle);
        }
    }

    /**
     * Get the number of unique shingles that were processed.
     * @return the number of unique shingles
     */
    public int getNumShingles(){
        return this.shingler.getNumShingles();
    }

    /**
     * Get the number of documents that were processed.
     * @return the number of documents.
     */
    public int getNumDocuments(){
        return this.maxDocs;
    }

    /**
     * Map an internal id to an external id.
     */
    public int getExternalId(int id) {
        return this.idToDoc.get(id);
    }

}
