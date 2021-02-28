/**
 * SimilarPair contains the ids of two objects and their similarity.
 */
public class SimilarPair implements Comparable<SimilarPair>{
    int id1;
    int id2;
    double sim;

    /**
     * Construct a SimilarPair object
     * @param id1 id of object 1
     * @param id2 id of object 2
     * @param sim their similarity
     */
    public SimilarPair(int id1, int id2, double sim){
        this.id1 = id1;
        this.id2 = id2;
        this.sim = sim;
    }

    /**
     * Comparing a SimilarPair object to another SimilarPair object.
     */
    @Override
    public int compareTo(SimilarPair c) {
        if (sim < c.getSimilarity()){
            return -1;
        }else if (sim == c.getSimilarity()){
            return 0;
        }else{
            return 1;
        }
    }

    /**
     * Returns the id of object 1.
     */
    public int getId1() {
        return id1;
    }

    /**
     * Returns the id of object 2.
     */
    public int getId2() {
        return id2;
    }

    /**
     * Returns the similarity between the objects.
     */
    public double getSimilarity(){
        return sim;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimilarPair other = (SimilarPair) obj;
        if (id1 == other.id1 && id2 == other.id2)
            return true;
        if (id1 == other.id2 && id2 == other.id1)
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return  prime * (1 + id1 * id2);
    }

    @Override
    public String toString() {
        return "SimilarPair [id1=" + id1 + ", id2=" + id2 + ", sim=" + sim + "]";
    }
}
