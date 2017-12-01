/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication18;

import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**
 *
 * @author abc
 */
public class BacaIndex {
	private static final String INDEX_DIR = "D:\\Kuliah\\rancangan document indexing\\dir-index\\";
 
    public static void main(String[] args) throws Exception
    {
		//Index reader - an interface for accessing a point-in-time view of a lucene index
		try ( 
			//Get directory reference
			Directory dir = FSDirectory.open(Paths.get(INDEX_DIR))) {
			//Index reader - an interface for accessing a point-in-time view of a lucene index
			IndexReader reader = DirectoryReader.open(dir);
			
			//Create lucene searcher. It search over a single IndexReader.
			IndexSearcher searcher = new IndexSearcher(reader);
			
			Analyzer analyzer = new IndonesianAnalyzer();
			
			QueryParser qp;
			qp = new QueryParser("contents",analyzer);
			
			//here the keyword you want to search
			Query query = qp.parse("menggunakan metode");
			
			
			//Search the lucene documents
			TopDocs hits = searcher.search(query, 100);
			
			
			//Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
			Formatter formatter = new SimpleHTMLFormatter();
			
			//It scores text fragments by the number of unique query terms found
			//Basically the matching score in layman terms
			QueryScorer scorer = new QueryScorer(query);
			
			//used to markup highlighted terms found in the best sections of a text
			Highlighter highlighter = new Highlighter(formatter, scorer);
			
			//It breaks text up into same-size texts but does not split up spans
			Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);
			
			//breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
			//Fragmenter fragmenter = new SimpleFragmenter(10);
			
			//set fragmenter to highlighter
			highlighter.setTextFragmenter(fragmenter);
			
			//Iterate over found results
			//for (int i = 0; i < hits.scoreDocs.length; i++)
			if(hits.scoreDocs.length > 0 )
			{
				System.out.println("Ditemukan : "+hits.totalHits);
				for (ScoreDoc scoreDoc : hits.scoreDocs) {
					int docid = scoreDoc.doc;
					Document doc = searcher.doc(docid);
					String path = doc.get("path");
					//Printing - to which document result belongs
					System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
					System.out.println("\nPath : " + path +" dengan score : "+scoreDoc.score);
					//Get stored text from found document
					//modified LUCENEPDFDocument
					String text = doc.get("contents");
//					String text = doc.getField("contents");
					//System.out.println(doc.getFields("contents"));
//					System.out.println("---------------------------");

					//Create token stream
					TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);
					String[] frags = highlighter.getBestFragments(stream, text,10);
					int nomor = 1;
					for (String frag : frags)
					{
						System.out.println("No."+nomor+" :");
						System.out.println(frag);
						nomor++;
					}
				}
			}
			else
			{
				System.out.println("coba kata kunci lain");
			}
		}
    }
}
