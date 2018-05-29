// Nathan Hull
// CIS675 HW2

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
	static boolean isComment = false;
	static boolean isString = false;
	static StringBuilder buildingString;

	static List<String> lineTokens;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println(args.length);
			System.out.println("Input error");
			return;
		}

		FileReader fr;
		try {
			fr = new FileReader(args[0]);
		} catch (Exception e) {
			System.out.println("Error finding or opening file");
			return;
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
		ops.put("->", "ARROW");
		ops.put(";", "SEMICOLON");
		ops.put("=", "ASSIGNMENT");
		// ops.put("\"", "QUOTATION-MARK");
		ops.put(",", "COMMA");
		ops.put("/*", "COMMENT");

		int lineNumber = 0;

		try {
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				lineTokens = new ArrayList();
				String tokens[] = line.split(" ");
				System.out.println("\nLINE: " + line);
				for (int x = 0; x < tokens.length; x++) {
					String token = tokens[x];
					token = token.trim();

					printToken(token);
				}

				// Parser: syntax checking
				// Check for mismatched brackets
				if (lineTokens.contains("LEFT-BRACKET") && !lineTokens.contains("RIGHT-BRACKET")) {
					System.out.println("Line " + lineNumber + ": Missing closing bracket");
				}

				// Check for missing node ID
				if (lineTokens.contains("ARROW") && !lineTokens.get(lineTokens.indexOf("ARROW")+1).equals("KEYWORD")) {
					System.out.println("line " + lineNumber + ": Missing target node ID");
				}

				// Check for missing value
				if (lineTokens.contains("ASSIGNMENT") && !(lineTokens.get(lineTokens.indexOf("ASSIGNMENT")+1).equals("KEYWORD") || lineTokens.get(lineTokens.indexOf("ASSIGNMENT")+1).equals("NUMERIC-VALUE") || lineTokens.get(lineTokens.indexOf("ASSIGNMENT")+1).equals("STRING"))) {
					System.out.println("line " + lineNumber + ": Missing value");
				}
			}
		} catch (IOException e) {
			System.out.println("IO Exception");
			try {
				reader.close();
			} catch (IOException f) {
				System.out.println("Error closing reader");
			}
			return;
		}

		try {
			reader.close();
		} catch (IOException e) {
			System.out.println("Error closing reader");
		}
	}

	static void printToken(String token) {
		if (isString) {
			if (token.equals("\"")) {
				isString = false;
				// System.out.println("STRING: " + buildingString.toString());

				lineTokens.add(buildingString.toString());
				return;
			}
		} else if (isComment) {
			if (token.equals("*/")) {
				isComment = false;
				// System.out.println("END-COMMENT");

				lineTokens.add("COMMENT");
			}
			return;
		}

		for (char c : token.toCharArray()) {

			if (isString) {
				if (c == '\"') {
					isString = false;
					// System.out.println("STRING: " + buildingString.toString());

					lineTokens.add(buildingString.toString());
					continue;
				} else {
					buildingString.append(c);
				}
			} else {

				if (c == '\"') {
					isString = !isString;
					if (!isString) {
						// System.out.println("STRING: " + buildingString.toString());

						lineTokens.add(buildingString.toString());
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

						lineTokens.add(ops.get(Character.toString(c)));

						if (partsAfter.length() >= 1) {
							printToken(partsAfter);
						}
					} else {
						// System.out.println(ops.get(Character.toString(c)));

						lineTokens.add(ops.get(Character.toString(c)));
					}

					return;
				}
			}
		}

		if (keywords.contains(token)) {
			// System.out.println(token.toUpperCase());

			lineTokens.add("KEYWORD");
			return;
		} else if (ops.containsKey(token)) {

			if (token.equals("/*")) {
				isComment = true;
				// System.out.println("COMMENT");

				lineTokens.add("COMMENT");
			} else if (token.equals("*/")) {
				isComment = false;
				// System.out.println("END-COMMENT");

				lineTokens.add("END-COMMENT");
			} else if (token.equals("\"")) {
				isString = !isString;
				if (!isString) {
					// System.out.println("STRING: " + buildingString.toString());

					lineTokens.add(buildingString.toString());
				} else {
					buildingString = new StringBuilder();
				}
			} else {
				// System.out.println(ops.get(token));

				lineTokens.add(ops.get(token));
			}

			return;
		}

		// If it's a number...
		try {
			Double.parseDouble(token);
			// System.out.println("NUMERIC-VALUE");

			lineTokens.add("NUMERIC-VALUE");
			return;
		} catch (NumberFormatException nfe) {
		}

		// Otherwise, it's an ID
		// System.out.println("ID");

		lineTokens.add("ID");
	}
}
