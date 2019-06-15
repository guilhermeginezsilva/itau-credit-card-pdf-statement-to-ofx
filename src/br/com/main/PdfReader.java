package br.com.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class PdfReader {

	public String parseText(String filePath) throws IOException, SAXException, TikaException {

		BodyContentHandler handler = null;
		FileInputStream inputstream =  null;
		try {
			handler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			inputstream = new FileInputStream(new File(filePath));
			ParseContext pcontext = new ParseContext();

			PDFParser pdfparser = new PDFParser();
			pdfparser.parse(inputstream, handler, metadata, pcontext);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(inputstream != null) {
				inputstream.close();
			}
		}

		return handler.toString();
	}
}
