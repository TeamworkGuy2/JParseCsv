package twg2.parser.io.csv;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.parser.textParserUtils.ReadWhitespace;
import twg2.streams.EnhancedIterator;

/** Static and instance methods for reading and writing Comma Separated Values (CSV) files.
 * Thread safe.
 * @author TeamworkGuy2
 * @since 2014-6-8
 */
public class Csv {
	// package-private
	// Quote '"'
	static final char CSV_QUOTE = (char)34;
	static final String CSV_QUOTE_STR = new String(new char[] {CSV_QUOTE});
	static final String CSV_DOUBLE_QUOTE_STR = CSV_QUOTE_STR + CSV_QUOTE_STR;
	// CRLF
	static final char[] CSV_ENDLINE = new char[] {(char)13, (char)10};
	static final String CSV_ENDLINE_STR = new String(CSV_ENDLINE);
	// Comma ','
	static final char CSV_COMMA = (char)44;
	static final String CSV_COMMA_STR = new String(new char[] {CSV_COMMA});

	private final char csvQuote;
	private final char csvComma;


	/** Create a CSV parser instance with non customized CSV format characters
	 * @param quoteChar the character that starts and ends a quoted string. The CSV default is a quote ({@code "})
	 * @param fieldSeparatorChar the separator character that separates fields. The CSV default is a comma ({@code ,})
	 */
	public Csv(char quoteChar, char fieldSeparatorChar) {
		this.csvQuote = quoteChar;
		this.csvComma = fieldSeparatorChar;
	}


	public final void writeToCSV(final File dst, final boolean append, final Charset charset,
			final String[][] lines, String nullString) throws IOException {
		writeCsvCustom(dst, append, charset, lines, nullString, this.csvQuote, this.csvComma);
	}


	public final void writeToCSV(final File dst, final boolean append, final Charset charset,
			final Collection<? extends Collection<String>> lines, String nullString) throws IOException {
		writeCsvCustom(dst, append, charset, lines, nullString, this.csvQuote, this.csvComma, true);
	}


	public final List<List<String>> readFromCsv(final File src, final Charset charset, boolean readLeadingElementWhitespace, char[] whitespace) throws IOException {
		return readCsv(src, charset, csvQuote, csvComma, readLeadingElementWhitespace, whitespace);
	}

	// end instance convenience methods


	public static final void writeCsvLine(Appendable dst, String[] line, String nullString) throws IOException {
		List<List<String>> lineList = new ArrayList<>(1);
		List<String> lineStrs = new ArrayList<>(line.length);
		lineList.add(lineStrs);
		Collections.addAll(lineStrs, line);

		writeCsvCustom(dst, lineList, nullString, CSV_QUOTE, CSV_COMMA, false);
	}


	public static final void writeCsvLine(Appendable dst, Collection<String> line, String nullString) throws IOException {
		List<Collection<String>> lineList = new ArrayList<>(1);
		lineList.add(line);

		writeCsvCustom(dst, lineList, nullString, CSV_QUOTE, CSV_COMMA, false);
	}


	public static final void writeCsvLineCustom(Appendable dst, String[] line, String nullString,
			char quoteChar, char fieldSeparatorChar) throws IOException {
		List<List<String>> lineList = new ArrayList<>(1);
		List<String> lineStrs = new ArrayList<>(line.length);
		lineList.add(lineStrs);
		Collections.addAll(lineStrs, line);

		writeCsvCustom(dst, lineList, nullString, quoteChar, fieldSeparatorChar, false);
	}


	public static final void writeCsvLine(File dst, boolean append, Charset charset,
			String[] line, String nullString) throws IOException {

		writeCsvLineCustom(dst, append, charset, line, nullString, CSV_QUOTE, CSV_COMMA);
	}


	public static final void writeCsvLine(File dst, boolean append, Charset charset,
			Collection<String> line, String nullString) throws IOException {
		List<Collection<String>> lineList = new ArrayList<>(1);
		lineList.add(line);

		writeCsvCustom(dst, append, charset, lineList, nullString, CSV_QUOTE, CSV_COMMA, false);
	}


	public static final void writeCsvLineCustom(File dst, boolean append, Charset charset,
			String[] line, String nullString, char quoteChar, char fieldSeparatorChar) throws IOException {
		List<List<String>> lineList = new ArrayList<>(1);
		List<String> columns = new ArrayList<String>(line.length);
		lineList.add(columns);
		Collections.addAll(columns, line);

		writeCsvCustom(dst, append, charset, lineList, nullString, quoteChar, fieldSeparatorChar, false);
	}


	public static final void writeCsv(File dst, boolean append, Charset charset,
			String[][] lines, String nullString) throws IOException {
		writeCsvCustom(dst, append, charset, lines, nullString, CSV_QUOTE, CSV_COMMA);
	}


	public static final void writeCsv(Appendable dst, String[][] lines, String nullString) throws IOException {
		List<List<String>> lineList = new ArrayList<>(lines.length);
		for(String[] line : lines) {
			List<String> columns = new ArrayList<String>(line.length);
			lineList.add(columns);
			Collections.addAll(columns, line);
		}

		writeCsvCustom(dst, lineList, nullString, CSV_QUOTE, CSV_COMMA, true);
	}


	public static final void writeCsvCustom(File dst, boolean append, Charset charset,
			String[][] lines, String nullString, char quoteChar, char fieldSeparatorChar) throws IOException {
		List<List<String>> lineList = new ArrayList<>(lines.length);
		for(String[] line : lines) {
			List<String> columns = new ArrayList<String>(line.length);
			lineList.add(columns);
			Collections.addAll(columns, line);
		}

		writeCsvCustom(dst, append, charset, lineList, nullString, quoteChar, fieldSeparatorChar, true);
	}


	/** Write data to a comma separated values (CSV) file.
	 * @see #writeCsvCustom(Appendable, Collection, String, char, char, boolean)
	 */
	public static final void writeCsv(File dst, boolean append, Charset charset,
			Collection<? extends Collection<String>> lines, String nullString) throws IOException {
		writeCsvCustom(dst, append, charset, lines, nullString, CSV_QUOTE, CSV_COMMA, true);
	}



	/** Write data to a comma separated values (CSV) file.
	 * @see #writeCsvCustom(Appendable, Collection, String, char, char, boolean)
	 */
	public static final void writeCsv(Appendable dst, Collection<? extends Collection<String>> lines,
			String nullString) throws IOException {
		writeCsvCustom(dst, lines, nullString, CSV_QUOTE, CSV_COMMA, true);
	}


	/** Write data to a comma separated values (CSV) file.
	 * @param charset the charset to use when writing text to the CSV file.
	 * If the charset is null, the default charset used will be {@code UTF-8}.
	 * @see #writeCsvCustom(Appendable, Collection, String, char, char, boolean)
	 */
	public static final void writeCsvCustom(File dst, boolean append, Charset charset, Collection<? extends Collection<String>> lines,
			String nullString, char quoteChar, char fieldSeparatorChar, boolean includeLastNewline) throws IOException {
		if(charset == null) { charset = Charset.forName("UTF-8"); }
		if(nullString == null) { nullString = ""; }
		Writer out = null;
		try {
			out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(dst, append)), charset);
			writeCsvCustom(out, lines, nullString, quoteChar, fieldSeparatorChar, true);
		}
		finally {
			if(out != null) {
				out.close();
			}
		}
	}


	/** Write data to a comma separated values (CSV) file. This method tries to follow
	 * RFC 4180: <a href="http://tools.ietf.org/html/rfc4180">http://tools.ietf.org/html/rfc4180</a>
	 * @param dst the write to write the data to
	 * @param lines the values to write to the file. The first dimension
	 * of the list represents lines, the second dimension represents column values of each line.
	 * These values will automatically be quoted and commas ',' will be escaped.
	 * @param nullString the string to write in replacement of null strings in
	 * the {@code lines} array. If null, this value will default to {@code ""}.
	 * @param quoteChar the character that starts and ends a quoted string. The CSV default is a quote ({@code "})
	 * @param fieldSeparatorChar the separator character that separates fields. The CSV default is a comma ({@code ,})
	 * @param includeLastNewline true to include a newline after the lastline (following the spec),
	 * false to not have an ending newline (for example when writing creating a single CSV line)
	 * @throws IOException if there is an error opening or writing to the destination file
	 */
	public static final void writeCsvCustom(Appendable dst, Collection<? extends Collection<String>> lines,
			String nullString, char quoteChar, char fieldSeparatorChar, boolean includeLastNewline) throws IOException {
		if(nullString == null) { nullString = ""; }

		int row = 0;
		for(Collection<String> line : lines) {
			// allows us to programmatically writing the last line
			if(row > 0) {
				dst.append(CSV_ENDLINE_STR);
			}
			final int columnCount = line.size();
			int columnC = 0;
			for(String value : line) {
				if(value == null) {
					value = nullString;
				}
				encodeCsvValueThrows(value, dst);
				// Write a comma after each column except the last column
				if(columnC < columnCount-1) {
					dst.append(CSV_COMMA);
				}
				columnC++;
			}
			row++;
		}
		if(includeLastNewline) {
			dst.append(CSV_ENDLINE_STR);
		}
	}


	public static final void encodeCsvValueThrows(String columnValue, Appendable dst) throws IOException {
		encodeCsvValueCustom(columnValue, CSV_QUOTE_STR, CSV_ENDLINE_STR, CSV_COMMA_STR, CSV_DOUBLE_QUOTE_STR, dst);
	}


	public static final void encodeCsvValue(String columnValue, Appendable dst) {
		try {
			encodeCsvValueThrows(columnValue, dst);
		} catch (IOException e) {
			throw new UncheckedIOException("writing encoded CSV value to output", e);
		}
	}


	public static final void encodeCsvValueCustom(String columnValue, String escapeSeqPrimary, String escapeSeq2, String escapeSeq3,
			String escapeSeqPrimaryReplacement, Appendable dst) throws IOException {
		boolean shouldQuote = false;
		boolean containsQuote = columnValue.contains(escapeSeqPrimary);
		boolean containsEndline = columnValue.contains(escapeSeq2);
		boolean containsComma = columnValue.contains(escapeSeq3);
		// If the string contains quotes, double quote the quotes to escape them
		if(containsQuote || containsEndline || containsComma) {
			shouldQuote = true;
			if(containsQuote) {
				columnValue = columnValue.replace(escapeSeqPrimary, escapeSeqPrimaryReplacement);
			}
		}
		// Write the column value
		if(shouldQuote) {
			dst.append(escapeSeqPrimary);
			dst.append(columnValue);
			dst.append(escapeSeqPrimary);
		}
		else {
			dst.append(columnValue);
		}
	}


	public static final List<List<String>> readCsv(final File src, final Charset charset) throws IOException {
		return readCsv(src, charset, CSV_QUOTE, CSV_COMMA, false, null);
	}


	public static final List<List<String>> readCsv(final File src, Charset charset, char quoteChar, char fieldSeparatorChar,
			boolean readLeadingElementWhitespace, char[] whitespace) throws IOException {
		if(charset == null) { charset = Charset.forName("UTF-8"); }
		InputStreamReader in = new InputStreamReader(new FileInputStream(src), charset);
		return readCsv(in, quoteChar, fieldSeparatorChar, readLeadingElementWhitespace, whitespace);
	}


	public static final List<List<String>> readCsv(final Reader src, char quoteChar, char fieldSeparatorChar,
			boolean readLeadingElementWhitespace, char[] whitespace) throws IOException {
		List<List<String>> csvValues = null;
		TextParserImpl in = null;
		try {
			BufferedReader bufSrc = src instanceof BufferedReader ? (BufferedReader)src : new BufferedReader(src);
			in = TextParserImpl.fromStrings(EnhancedIterator.fromReader(bufSrc, true, null));
			csvValues = readCsv(in, quoteChar, fieldSeparatorChar, readLeadingElementWhitespace, whitespace);
		} finally {
			if(in != null) {
				in.close();
			}
		}
		if(csvValues == null) {
			csvValues = new ArrayList<List<String>>();
		}
		return csvValues;
	}


	public static final List<List<String>> readCsv(final TextParser in, char quoteChar, char fieldSeparatorChar,
			boolean readLeadingElementWhitespace, char[] whitespace) throws IOException {
		ArrayList<List<String>> csvValues = new ArrayList<List<String>>();
		StringBuilder strB = new StringBuilder();

		for( @SuppressWarnings("unused")int rowI = 0; in.hasNext(); rowI++) {
			ArrayList<String> currentRow = new ArrayList<String>();
			csvValues.add(currentRow);

			while(in.hasNext()) {
				strB.setLength(0);
				decodeCsvValue(in, readLeadingElementWhitespace, whitespace, CSV_ENDLINE_STR, strB);
				currentRow.add(strB.toString());
				if(in.nextIf('\n')) {
					break;
				}
			}
		}
		return csvValues;
	}


	/** Convert an encoded (normally a value read from a file) CSV value string to it's unencoded form
	 * @param in the {@link TextParser} to read input text from
	 * @param newline the line to replace for each quoted newline found (normally CRLF)
	 * @param dst the string builder to append the decoded string to
	 * @throws IOException 
	 */
	public static final void decodeCsvValue(TextParser in, boolean readLeadingElementWhitespace, char[] whitespace, String newline, Appendable dst)
			throws IOException {
		if(readLeadingElementWhitespace) {
			ReadWhitespace.readWhitespaceCustom(in, whitespace);
		}

		// Common for CSV files to contain empty columns (looks like "a,,c,...") in which case the length would be 0

		// If the value is quoted, find the end of it by searching for a quote without an adjacent unpaired quote
		if(in.nextIf(CSV_QUOTE)) {
			while(in.hasNext()) {
				char ch = in.nextChar();
				if(ch == CSV_QUOTE) {
					// skip pairs of quotes
					if(in.hasNext() && in.nextIf(CSV_QUOTE)) {
						// double quote
					}
					else {
						break;
					}
				}
				if(dst != null) { dst.append(ch); }
			}
			if(in.nextIf(CSV_QUOTE)) {
				if(dst != null) { dst.append(CSV_QUOTE); }
			}
		}
		else {
			in.nextIfNot(CSV_COMMA, CSV_QUOTE, '\n', 0, dst);
		}
		in.nextIf(CSV_COMMA);
	}

}
