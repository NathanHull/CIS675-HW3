// Nathan Hull
// CIS675 HW3

// Input:
// digraph G {
//		main [shape=box]; /* this is a comment */
//		main -> parse [weight=8];
//		parse -> execute;
//		main -> init [style=dotted];
//		main -> cleanup;
//		execute -> make_string;
//		init -> make_string;
//		main -> printf [style=bold,label="100 times"];
//		make_string [label="make a\nstring"];
//		node [shape=box,style=filled,color=".7 .3 1.0"];
// 		execute -> compare;
// }

import java.util.*;
import java.io.*;

class Lexer {

	static List<String> keywords;
	static HashMap<String, String> ops;
	static StringBuilder buildingString;

	static boolean isComment;
	static boolean isString;
	static boolean wasString;

	static List<List<String>> lineTokens;
	static int lineNumber;

	public Lexer() {
		isComment = false;
		isString = false;
		wasString = false;

		lineNumber = 0;

		lineTokens = new ArrayList<List<String>>();
	}

	static List<List<String>> lexFile(String file) {

		FileReader fr;
		try {
			fr = new FileReader(file);
		} catch (Exception e) {
			System.out.println("Error finding or opening file");
			return null;
		}

		BufferedReader reader = new BufferedReader(fr);
		String line = "";

		// instantiate sets of keywords/ops
		keywords = Arrays.asList("digraph", "graph", "subgraph", "strict", "node", "edge", "shape", "box", "parse",
				"weight", "init", "style", "dotted", "cleanup", "make_string", "init", "printf", "bold", "label",
				"filled", "color", "execute", "compare");

		ops = new HashMap();
		ops.put("{", "LEFT-BRACE");
		ops.put("}", "RIGHT-BRACE");
		ops.put("[", "LEFT-BRACKET");
		ops.put("]", "RIGHT-BRACKET");
		ops.put("->", "EDGEOP");
		ops.put(";", "SEMICOLON");
		ops.put("=", "ASSIGNMENT");
		ops.put("\"", "QUOTATION-MARK");
		ops.put(",", "COMMA");
		ops.put("/*", "COMMENT");

		System.out.println("Running lexer:");
		try {
			while ((line = reader.readLine()) != null) {
				lineTokens.add(new ArrayList());
				String tokens[] = line.split(" ");
				System.out.println("\nLINE " + lineNumber + ": " + line);
				for (int x = 0; x < tokens.length; x++) {
					String token = tokens[x];
					token = token.trim();

					printToken(token);
				}

				lineNumber++;
			}
		} catch (IOException e) {
			System.out.println("IO Exception");
			try {
				reader.close();
			} catch (IOException f) {
				System.out.println("Error closing reader");
			}
			return null;
		}

		try {
			reader.close();
		} catch (IOException e) {
			System.out.println("Error closing reader");
		}

		System.out.println("File lexed");
		return lineTokens;
	}

	static void printToken(String token) {
		if (isString) {
			if (token.equals("\"")) {
				isString = false;
				wasString = true;
				// System.out.println("STRING: " + buildingString.toString());

				lineTokens.get(lineNumber).add("STRING");
				return;
			}
		} else if (isComment) {
			if (token.equals("*/")) {
				isComment = false;
				// System.out.println("END-COMMENT");

				lineTokens.get(lineNumber).add("COMMENT");
			}
			return;
		}

		for (char c : token.toCharArray()) {

			if (isString) {

				if (wasString) {
					isString = false;
					wasString = false;
				} else if (c == '\"') {
					isString = false;
					wasString = true;
					// System.out.println("STRING: " + buildingString.toString());

					lineTokens.get(lineNumber).add("STRING");
					continue;
				} else {
					buildingString.append(c);
				}
			} else {

				if (c == '\"') {
					isString = !isString;

					if (wasString) {
						isString = false;
						wasString = false;
					} else if (!isString) {
						wasString = true;
						// System.out.println("STRING: " + buildingString.toString());

						lineTokens.get(lineNumber).add("STRING");
					} else {
						buildingString = new StringBuilder();
					}
					return;
				}

				if (ops.containsKey(Character.toString(c))) {
					if (token.length() > 1) {
						String partsBefore = token.substring(0, token.indexOf(c)).trim();
						String partsAfter = token.substring(token.indexOf(c) + 1).trim();

						if (partsBefore.length() >= 1) {
							printToken(partsBefore);
						}
						// System.out.println(ops.get(Character.toString(c)));

						lineTokens.get(lineNumber).add(ops.get(Character.toString(c)));

						if (partsAfter.length() >= 1) {
							printToken(partsAfter);
						}
					} else {
						// System.out.println(ops.get(Character.toString(c)));

						lineTokens.get(lineNumber).add(ops.get(Character.toString(c)));
					}

					return;
				}
			}
		}

		if (keywords.contains(token)) {
			// System.out.println(token.toUpperCase());

			lineTokens.get(lineNumber).add("KEYWORD");
			return;
		} else if (ops.containsKey(token)) {

			if (token.equals("/*")) {
				isComment = true;
				// System.out.println("COMMENT");

				lineTokens.get(lineNumber).add("COMMENT");
			} else if (token.equals("*/")) {
				isComment = false;
				// System.out.println("END-COMMENT");

				lineTokens.get(lineNumber).add("END-COMMENT");
			} else if (token.equals("\"")) {
				isString = !isString;
				if (!isString) {
					wasString = true;
					// System.out.println("STRING: " + buildingString.toString());

					lineTokens.get(lineNumber).add("STRING");
				} else {
					buildingString = new StringBuilder();
				}
			} else {
				// System.out.println(ops.get(token));

				lineTokens.get(lineNumber).add(ops.get(token));
			}

			return;
		}

		// If it's a number...
		try {
			Double.parseDouble(token);
			// System.out.println("NUMERIC-VALUE");

			lineTokens.get(lineNumber).add("NUMERIC-VALUE");
			return;
		} catch (NumberFormatException nfe) {
		}

		// Otherwise, it's an ID
		// System.out.println("ID");

		lineTokens.get(lineNumber).add("ID");
	}
}
