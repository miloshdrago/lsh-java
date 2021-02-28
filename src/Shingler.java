import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A SimpleShingler constructs the shingle representations of documents.
 * It takes all substrings of length k of the document, and maps these
 * substrings to an integer value that is inserted into the documents shingle
 * set.
 */
public class Shingler {

    private int k;
    private int numShingles;

    /**
     * Construct a shingler.
     * @param k number of characters in one shingle
     */
    public Shingler(int k, int numShingles){
        this.k = k;
        this.numShingles = numShingles;
    }

    /**
     * Hash a k-shingle to an integer.
     * @param shingle shingle to hash
     * @return integer that the shingle maps to
     */
    private int hashShingle(String shingle){
        int hash = MurmurHash.hash32(shingle, 1234);
        return Math.abs(hash) % getNumShingles();
    }

    /**
     * Get the shingle set representation of a document.
     * @param post the document that should be shingled
     * @return set of integers being the hash maps of the shingles
     */
    public Set<Integer> shingle(Post post){
        Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

        Set<Integer> shingled = new HashSet<Integer>();

        String completeDocument = "";
        completeDocument += post.getTitle() + " ";
        completeDocument += post.getBody() + " ";
        completeDocument = completeDocument.toLowerCase().replace("\n", " ").replace("\r", " ");

        if (completeDocument != null && completeDocument.length() > 0) {
           //removing the text between script
            String re = "<pre>(.*)</pre>";

            Pattern pattern = Pattern.compile(re);
            Matcher matcher = pattern.matcher(completeDocument);
            if (matcher.find()) {
                completeDocument = completeDocument.replace(matcher.group(1), "");
            }
            Matcher m = REMOVE_TAGS.matcher(completeDocument);
            completeDocument = m.replaceAll("");
        }
        completeDocument = completeDocument.replaceAll("[^a-zA-Z -]", "").toLowerCase();
        completeDocument = completeDocument.trim().replaceAll(" +", " ");

        for (int i = 0; i < completeDocument.length() - k + 1; i++){
            String toHash = Character.toString(completeDocument.charAt(i));
            for (int j = 0; j < k - 1; j++){
                toHash += Character.toString(completeDocument.charAt(i+j+1));
            }
            shingled.add(hashShingle(toHash));
        }
        return shingled;
    }

    /**
     * Get the number of unique shingles this shingler has processed.
     * @return number of unique shingles
     */
    public int getNumShingles() {
        return this.numShingles;
    }

}
