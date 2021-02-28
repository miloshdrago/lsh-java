import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Brute force implementation of the similarity searcher. The Jaccard
 * similarity is computed for all pairs and the pairs that are more similar
 * than a given threshold are returned.
 */
public class BruteForceSearch extends SimilaritySearcher{

    /**
     * Construct a BruteForceSearch object.
     * @param reader a data Reader object
     * @param threshold the similarity threshold
     */
    public BruteForceSearch(Reader reader, double threshold){
        super(reader, threshold);
    }

    /**
     * Get pairs of objects with similarity above threshold.
     * @return the pairs
     */
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold() {
        reader.readAll();
        List<Set<Integer>> docToShingle = reader.getObjectMapping();
        Set<SimilarPair> cands = new HashSet<SimilarPair>();
        for (int obj1 = 0; obj1 < docToShingle.size(); obj1++){
            for (int obj2 = 0; obj2 < obj1; obj2++){
                double sim = jaccardSimilarity(docToShingle.get(obj1), docToShingle.get(obj2));
                if (sim > threshold){
                    cands.add(new SimilarPair(reader.getExternalId(obj2), reader.getExternalId(obj1), sim));
                }
            }
        }
        return cands;
    }

}
