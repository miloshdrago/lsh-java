# LSH Implementation
This assignment aimed at finding duplicate questions in a 1.5GB Stack Overflow Data File. 

Brute force is a method which calculates the similarity of an input by comparing it with all other inputs. This
requires high memory usage and computation time. Locality-sensitive hashing (LSH) is an alternative method
which hashes similar inputs into the same buckets. Only inputs ending in the same buckets will be compared to
calculate their similarity. This method could lead to a smaller memory use and a reduction of computation time.

The resulting paper was graded 18.0/20.0.
