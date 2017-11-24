import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * To create Apache Lucene index in a folder and add files into this index based
 * on the input of the user.
 */
public class HW4 {
//    private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
    private static Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();

    public static void main(String[] args) throws IOException {

        String indexLocation = null;
        File indexDir = new File("./Index");
        if (!indexDir.exists()) {
            indexDir.mkdir();
        }
        String s = "./Index";

        HW4 indexer = null;
        try {
            indexLocation = s;
            indexer = new HW4(s);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        // ===================================================
        // create index on the given path for the corpus
        // ===================================================
        s = "./Corpus";
        indexer.indexFileOrDirectory(s);


        // ===================================================
        // after adding, we always have to call the
        // closeIndex, otherwise the index is not created
        // ===================================================
        indexer.closeIndex();

        // =========================================================
        // Now search
        // =========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
                indexLocation)));
        IndexSearcher searcher = new IndexSearcher(reader);

        // =========================================================
        // Reading list of queries
        // =========================================================
        Map<String, String> queries = FileIO.readQueries("./QueryList.txt");

        // =========================================================
        // Reading document Id mapping
        // =========================================================
        Map<String, String> docIdMap = FileIO.readDocIdMap("./DocId-Mapping.txt");

        // =========================================================
        // Searching documents for each query
        // =========================================================
        for (String queryID : queries.keySet()) {
            try {
                s = queries.get(queryID);
                TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
                Query q = new QueryParser(Version.LUCENE_47, "contents",
                        sAnalyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Found " + hits.length + " hits.");

                List<String> docRankTable = new ArrayList<>();

                // =========================================================
                // Storing top 100 documents
                // =========================================================
                for (int i = 0; i < hits.length; ++i) {

                    Document d = searcher.doc(hits[i].doc);
                    String [] path = d.get("path").split("/");
                    String docName = path[path.length-1].replace(".txt", "");
                    String docId = docIdMap.get(docName);
                    String docRank = queryID+" "+ "Q0" + " " + docId+ " "+ (i+1) + " " +
                            hits[i].score+" " +"Lucene_simple_analyzer";
                    docRankTable.add(docRank);
                }

                // =========================================================
                // Writing top 100 docs to file "./Task-1/Query-queryID.txt"
                // =========================================================
                File task2Dir = new File("./Task-1");
                if (!task2Dir.exists()) {
                    task2Dir.mkdir();
                }
                FileIO.outputResultsToFile(docRankTable, "./Task-1/Query-"+queryID+".txt");

                // 5. term stats --> watch out for which "version" of the term
                // must be checked here instead!
//                Term termInstance = new Term("contents", s);
//                long termFreq = reader.totalTermFreq(termInstance);
//                long docCount = reader.docFreq(termInstance);
//                System.out.println(s + " Term Frequency " + termFreq
//                        + " - Document Frequency " + docCount);

            } catch (Exception e) {
                System.out.println("Error searching " + s + " : "
                        + e.getMessage());
                break;
            }

        }

    }

    /**
     * Constructor
     *
     * @param indexDir
     *            the name of the folder in which the index should be created
     * @throws java.io.IOException
     *             when exception creating index.
     */
    HW4(String indexDir) throws IOException {

        FSDirectory dir = FSDirectory.open(new File(indexDir));

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
                sAnalyzer);

        writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     *
     * @param fileName
     *            the name of a text file or a folder we wish to add to the
     *            index
     * @throws java.io.IOException
     *             when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        // ===================================================
        // gets the list of files in a folder (if user has submitted
        // the name of a folder) or gets a single file name (is user
        // has submitted only the file name)
        // ===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                // ===================================================
                // add contents of file
                // ===================================================
                fr = new FileReader(f);
                doc.add(new TextField("contents", fr));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(),
                        Field.Store.YES));

                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out
                .println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            // ===================================================
            // Only index text files
            // ===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html")
                    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     *
     * @throws java.io.IOException
     *             when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}