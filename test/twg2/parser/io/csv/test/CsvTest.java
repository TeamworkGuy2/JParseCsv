package twg2.parser.io.csv.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import twg2.parser.io.csv.Csv;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.streams.EnhancedIterator;
import twg2.text.stringUtils.StringCheck;

/**
 * @author TeamworkGuy2
 * @since 2015-2-7
 */
public class CsvTest {

	private String[][] getCsvRawLines() {
		return new String[][] {
			{ "name", "\"description\"", "a,b,c", },
			{ "skyscraper", "a \"tall\" building", "123.95" },
			{ "submarine", "an aquatic, submersible, vehicle", "1, \"2\", 3" }
		};
	}


	private List<List<String>> toList(String[][] strsList) {
		List<List<String>> dst = new ArrayList<>(strsList.length);
		for(int i = 0, size = strsList.length; i < size; i++) {
			List<String> strs = new ArrayList<>(strsList[i].length);
			Collections.addAll(strs, strsList[i]);
			dst.add(strs);
		}
		return dst;
	}


	@Test
	public void csvWriteReadTest() throws IOException {
		String nullStr = "null";
		char quoteChar = '"';
		char fieldSeparatorChar = ',';
		List<List<String>> csvLines = toList(getCsvRawLines());
		
		StringWriter dst = new StringWriter();

		Csv.writeCsvCustom(dst, csvLines, nullStr, quoteChar, fieldSeparatorChar, true);

		TextParser in = TextParserImpl.of(dst.toString());

		List<List<String>> resLines = Csv.readCsv(in, quoteChar, fieldSeparatorChar, false, null);

		Assert.assertEquals(csvLines, resLines);
	}


	@Test
	public void csvTextParserWriteReadTest() throws IOException {
		String nullStr = "null";
		char quoteChar = '"';
		char fieldSeparatorChar = ',';
		List<List<String>> csvLines = toList(getCsvRawLines());
		
		StringWriter dst = new StringWriter();

		Csv.writeCsvCustom(dst, csvLines, nullStr, quoteChar, fieldSeparatorChar, true);

		TextParserImpl in = TextParserImpl.fromStrings(EnhancedIterator.fromReader(new BufferedReader(new StringReader(dst.toString())), true, null));

		List<List<String>> resLines = Csv.readCsv(in, quoteChar, fieldSeparatorChar, true, StringCheck.SIMPLE_WHITESPACE_NOT_NEWLINE);

		Assert.assertEquals(dst.toString(), csvLines, resLines);
	}

}
