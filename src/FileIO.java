import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import java.io.*;
import java.util.*;

/**
 * Created by Harshil on 22/11/17.
 */
public class FileIO {

    public static Map<String, String> readQueries(String filePath){
        Map<String, String> queries = new HashMap<>();
        try {
            BufferedReader bfReader = new BufferedReader(new FileReader(filePath));
            String line = null;

            while( (line = bfReader.readLine()) != null) {

                String[] queryArr = line.split(" ");

                queries.put(queryArr[0], String.join(" ", Arrays.copyOfRange(queryArr,1, queryArr.length)));
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + " error at line 14 method readQueries");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage() + " error at line 19 method readQueries");
            e.printStackTrace();
        }

        return queries;

    }

    public static Map<String, String> readDocIdMap(String filePath) {
        Map<String, String> docIdMap = new HashMap<>();
        try {
            BufferedReader bfReader = new BufferedReader(new FileReader(filePath));
            String line = null;

            while( (line = bfReader.readLine()) != null) {

                String[] docIdArr = line.split("->");

                docIdMap.put(docIdArr[1].trim(), docIdArr[0].trim());
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + " error at line 14 method readQueries");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage() + " error at line 19 method readQueries");
            e.printStackTrace();
        }

        return docIdMap;

    }

    public static Map<String, Integer> readDocumentLength(String filePath) {

        Map<String, Integer> documentLengthMap = new HashMap<>();

        try {
            BufferedReader bfReader = new BufferedReader(new FileReader(filePath));
            String line = null;

            while( (line = bfReader.readLine()) != null) {

                String[] docLengthArr = line.split("->");

                documentLengthMap.put(docLengthArr[0].trim(), Integer.parseInt(docLengthArr[1].trim()));
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + " error at line 14 method readQueries");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage() + " error at line 19 method readQueries");
            e.printStackTrace();
        }

        return documentLengthMap;

    }

    public static Map<String, Map<String, Integer>> readOneGramIndexer(String filePath) {

        Map<String, Map<String, Integer>> oneGramIndexer = new HashMap<>();

        try {
            BufferedReader bfReader = new BufferedReader(new FileReader(filePath));
            String termIndex = null;

            while( (termIndex = bfReader.readLine()) != null) {

//                for termIndex in IndexerData:
//                term,indexData = termIndex.split(' -> ')
//                indexData = re.sub(r"[()\n]+", "", indexData).split(', ')
//                freq = reduce(lambda x,y: int(x) + int(y), indexData[1::2])#list[1::2] gets odd positions
//                TermFrequency[term] = int(freq)
                termIndex = termIndex.replaceAll("[()\\n]+", "");
                String[] term_IndexData = termIndex.split(" -> ");

                String[] indexData = term_IndexData[1].split(", ");
                //System.out.println(term_IndexData[0]);

                Map<String, Integer> indexDataMap = new HashMap<>();
                for(int i = 0; i< indexData.length; i = i+2){
                    indexDataMap.put(indexData[i], Integer.parseInt(indexData[i+1]));
                }
                oneGramIndexer.put(term_IndexData[0], indexDataMap);
            }

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + " error at line 14 method readQueries");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage() + " error at line 19 method readQueries");
            e.printStackTrace();
        }

        return oneGramIndexer;

    }

    public static void outputResultsToFile(List<String> docRankTable, String filePath) throws IOException {

        File outputFile = new File(filePath);

        if(!outputFile.exists())
            outputFile.createNewFile();

        FileWriter fw = new FileWriter(outputFile);

        for (String docRank : docRankTable) {
            fw.write(docRank);
            fw.append(System.getProperty("line.separator"));
        }
        fw.flush();
        fw.close();


    }

}
