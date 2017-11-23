import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Harshil on 22/11/17.
 */
public class MySimpleIndexer {

    public static void main(String[] args) throws IOException {

        final double k1 = 1.2;
        final double k2 = 100;
        final double b = 0.75;

        Map<String, Map<String, Integer>> oneGramIndexer = FileIO.readOneGramIndexer("./One_Gram_Indexer.txt");

        Map<String, Integer> documentLengthMap = FileIO.readDocumentLength("./DocumentsLength.txt");
        final double avgdl = getDocumentAverageLength(documentLengthMap);

        // =========================================================
        // Reading list of queries
        // =========================================================
        Map<String, String> queries = FileIO.readQueries("./QueryList.txt");

        File task2Dir = new File("./Task-2");
        if (!task2Dir.exists()) {
            task2Dir.mkdir();
        }

        for (String queryID : queries.keySet()) {
            Map<String, Double> documentScores = new HashMap<>();
            String[] queryTerms = queries.get(queryID).split(" ");

            for(String queryTerm : queryTerms){

                Map<String, Integer> indexData = oneGramIndexer.get(queryTerm);
                double idf = IDF(queryTerm, documentLengthMap.size(), indexData.size());

                for(String docId : indexData.keySet()){
                    int tf = indexData.get(docId);

                    double term1 = (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * (documentLengthMap.get(docId)/avgdl)));
                    double term2 = ((k2 + 1) * tf) / (k2 + tf);

                    double score = idf * term1 * term2;

                    if(documentScores.containsKey(docId)){
                        score = documentScores.get(docId) + score;
                    }
                    documentScores.put(docId, score);
                }
            }

            Map<String,Double> sortedDocumentScores = Common.sortByComparator(documentScores);

            List<String> docRankTableData = new ArrayList<>();

            int i = 0;
            for(String docId : sortedDocumentScores.keySet()){
                String docRank = queryID + " " + "Q0" + " " + docId + " " + ++i + " " +
                        sortedDocumentScores.get(docId) + " " + "My_BM25_Simple_Analyzer";
                docRankTableData.add(docRank);

                if(i == 100)
                    break;
            }

            FileIO.outputResultsToFile(docRankTableData, "./Task-2/Query-"+queryID+".txt");

        }

    }

    private static double IDF(String queryTerm, int totalDocumentsCount, int documentsWithQueryTerm){
        double idf = Math.log((totalDocumentsCount - documentsWithQueryTerm + 0.5)/(documentsWithQueryTerm + 0.5));

        return idf;
    }

    private static double getDocumentAverageLength(Map<String, Integer> documentLengthMap){

        double avgDocLength = 0;
        for(int docLength : documentLengthMap.values()){
            avgDocLength += docLength;
        }
        return avgDocLength / documentLengthMap.size();
    }
}
