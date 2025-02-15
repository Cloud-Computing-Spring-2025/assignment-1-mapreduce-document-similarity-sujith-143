package com.example;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

public class DocumentSimilarityMapper extends Mapper<Object, Text, Text, Text> {
  public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
    String line = value.toString();
    String[] parts = line.split("\\s+", 2); // Split into document ID and content

    if (parts.length < 2)
      return; // Skip malformed lines

    String docId = parts[0]; // First word is the document ID
    String content = parts[1]; // The rest is the document content

    // Set to hold words (removes duplicates)
    HashSet<String> words = new HashSet<>();
    StringTokenizer tokenizer = new StringTokenizer(content);

    while (tokenizer.hasMoreTokens()) {
      words.add(tokenizer.nextToken().toLowerCase()); // Normalize case to lowercase
    }

    // Emit (DocumentID, Word)
    for (String word : words) {
      context.write(new Text(docId), new Text(word)); // Emit pairs of (DocumentID, Word)
    }
  }
}
