package com.example;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {
  // To store document IDs and their respective word sets
  private final HashMap<String, HashSet<String>> docWordMap = new HashMap<>();

  @Override
  public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    String docId = key.toString(); // Get the document ID from the key
    HashSet<String> words = new HashSet<>();

    // Add all words from the current document's values into the word set
    for (Text val : values) {
      words.add(val.toString());
    }

    // Save the word set for each document
    docWordMap.put(docId, words);
  }

  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException {
    // Convert the document list to a sorted list (for consistent ordering)
    List<String> docList = new ArrayList<>(docWordMap.keySet());

    // Compare each document with every other document
    for (int i = 0; i < docList.size(); i++) {
      for (int j = i + 1; j < docList.size(); j++) {
        String doc1 = docList.get(i); // First document
        String doc2 = docList.get(j); // Second document

        HashSet<String> words1 = docWordMap.get(doc1); // Words in doc1
        HashSet<String> words2 = docWordMap.get(doc2); // Words in doc2

        // Compute Jaccard similarity (Intersection / Union)
        HashSet<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2); // Intersection of doc1 and doc2 words

        HashSet<String> union = new HashSet<>(words1);
        union.addAll(words2); // Union of doc1 and doc2 words

        double similarity = (double) intersection.size() / union.size(); // Jaccard similarity

        // Only output if similarity is greater than 0
        if (similarity > 0.0) {
          context.write(new Text("(" + doc1 + ", " + doc2 + ")"), new Text(String.format("%.2f", similarity)));
        }
      }
    }
  }
}
