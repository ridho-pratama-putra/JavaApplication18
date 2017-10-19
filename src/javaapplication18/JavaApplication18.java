/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication18;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.examples.lucene.LucenePDFDocument;

/**
 *
 * @author abc
 */
public class JavaApplication18 {

	/**
	 * @param args the command line arguments
	 */
	
	public static void main(String[] args) {
		String pdfPath= "D:\\Kuliah\\rancangan document indexing\\dir-pdf\\";
		String indexPath= "D:\\Kuliah\\rancangan document indexing\\dir-index\\";
		final Path indexDir = Paths.get(indexPath);
        boolean create = true;
		final File docDir = new File(pdfPath);
        
		if (!docDir.exists() || !docDir.canRead())
        {
            System.out.println("direktori pdf '" + docDir.getAbsolutePath()+ "' tidak ada atau tidak bisa dibaca");
            System.exit(1);
        }else
		{
			Date start = new Date();
			try
			{
				System.out.println("Indexing ke folder: '" + indexPath + "'...");
				Directory dir ;
				dir = FSDirectory.open(indexDir);
				
				Analyzer analyzer = new StandardAnalyzer();
				IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
				if (create)
				{
					iwc.setOpenMode(OpenMode.CREATE);
				}
				else
				{
					iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
				}
				try ( 
					IndexWriter writer = new IndexWriter(dir, iwc)) {
					indexDocs(writer, docDir);
				}
				Date end = new Date();
				System.out.println(end.getTime() - start.getTime() + " total milliseconds");
			}
			catch (IOException e)
			{
				System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
			}
		}
	}

	private static void indexDocs(IndexWriter writer, File file) throws IOException {
		if (file.canRead())
        {
			if (file.isDirectory())
            {
                String[] files = file.list();
                // an IO error could occur
                if (files != null)
                {
                    for (String fileName : files)
                    {
                        indexDocs(writer, new File(file, fileName));
                    }
                }
            }
            else
            {
                FileInputStream fis;
                try
                {
                    fis = new FileInputStream(file);
                }
                catch (FileNotFoundException fnfe)
                {
                    return;
                }
                try
                {
                    String path = file.getName().toUpperCase();
                    Document doc = null;
                    
					if (path.toLowerCase().endsWith(".pdf"))
                    {
                        System.out.println("Indexing PDF document: " + file);
                        doc = LucenePDFDocument.getDocument(file);
						//doc.add(new TextField("contents", new String(Files.readAllBytes(Paths.get(pdfPath))), Store.YES));
//						System.out.println("field list: \n" + doc.getFields());
//						System.exit(0);
                    }
                    else
                    {
                        System.out.println("Skipping " + file);
                        return;
                    }
                    
					if (writer.getConfig().getOpenMode() == OpenMode.CREATE)
                    {
                        System.out.println("adding " + file);
						
                        writer.addDocument(doc);
//						System.out.println("summary: " + doc.getField("summary")+"\n");
//						System.out.println("producer: " + doc.getField("Producer")+"\n");
//						System.out.println("contents: " + doc.getField("contents")+"\n");
                    }
                    else
                    {
                        System.out.println("updating " + file);
                        writer.updateDocument(new Term("uid", LucenePDFDocument.createUID(file)), doc);
                    }
                }
                finally
                {
                    fis.close();
                }
            }
		}
		// throw new UnsupportedOperationException("Not supported yet.");
	}
}