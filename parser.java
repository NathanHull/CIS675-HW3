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

// Grammar
// 	graph 	: 	[ strict ] (graph | digraph) [ ID ] '{' stmt_list '}'
// 	stmt_list 	: 	[ stmt [ ';' ] stmt_list ]
// 	stmt 	: 	node_stmt
// 	| 	edge_stmt
// 	| 	attr_stmt
// 	| 	ID '=' ID
// 	| 	subgraph
// 	attr_stmt 	: 	(graph | node | edge) attr_list
// 	attr_list 	: 	'[' [ a_list ] ']' [ attr_list ]
// 	a_list 	: 	ID '=' ID [ (';' | ',') ] [ a_list ]
// 	edge_stmt 	: 	(node_id | subgraph) edgeRHS [ attr_list ]
// 	edgeRHS 	: 	edgeop (node_id | subgraph) [ edgeRHS ]
// 	node_stmt 	: 	node_id [ attr_list ]
// 	node_id 	: 	ID [ port ]
// 	port 	: 	':' ID [ ':' compass_pt ]
// 	| 	':' compass_pt
// 	subgraph 	: 	[ subgraph [ ID ] ] '{' stmt_list '}'
// 	compass_pt 	: 	(n | ne | e | se | s | sw | w | nw | c | _)

import java.util.*;

import java.io.*;

class Parser {

	static List<List<String>> lines;
	static String currentToken = "";
	static int lineNumber = 0;
	static int tokenNumber = 0;
	static int checkCtr = 0;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println(args.length);
			System.out.println("Input error");
			return;
		}

		System.out.println("Lexing file " + args[0]);
		lines = Lexer.lexFile(args[0]);

		if (lines.size() > 0) {
			currentToken = lines.get(0).get(0);
		}

		System.out.println("\n============");
		System.out.println("Parsing file");

		lineNumber = 0;
		tokenNumber = 0;
		checkCtr = 0;

		parseGraph();
	}

	// Check if next token is sent string, if so move forward
	static boolean accept(String token) {
		boolean isMatching;
		checkCtr++;
		if (checkCtr > 100) {
			System.out.println("Line " + lineNumber + ": infinite loop error");
		}

		if (currentToken.equals(token)) {
			isMatching = true;
			checkCtr = 0;

			// If more tokens, iterate forward
			if (!currentToken.equals("EOF")) {
				tokenNumber++;
				if (tokenNumber >= lines.get(lineNumber).size()) {
					lineNumber++;
					tokenNumber = 0;
		
					if (lineNumber >= lines.size()) {
						currentToken = "EOF";
					}
				}

				currentToken = lines.get(lineNumber).get(tokenNumber);
			}
		} else {
			isMatching = false;
		}

		return isMatching;
	}

	static void parseGraph() {
		if (accept("STRICT")) {
			// Is strict
		}
		
		if (accept("GRAPH")) {
			// Is graph
		} else if (accept("DIGRAPH")) {
			// Is digraph
		} else {
			// Error: graph or digraph required
			System.out.println("Line " + lineNumber + ": missing graph or digraph keyword");
			System.exit(1);
		}

		if (!accept("ID")) {
			// Error: missing graph ID
			System.out.println("Line " + lineNumber + ": missing graph ID");
			System.exit(1);
		}

		if (accept("LEFT-BRACE")) {
			// Begin graph description
			parseStmtList();
		} else {
			System.out.println("Line " + lineNumber + ": missing left bracket");
			System.exit(1);
		}
	}

	static void parseStmtList() {
		if (accept("ID")) {
			// can be node_stmt, edge_stmt, or ID '=' ID
			parseNodeId();
			if (accept("GRAPH") || accept("NODE") || accept("EDGE")) {
				// is attr_stmt
				if (accept("LEFT-BRACKET")) {
					parseAttrList();
				} else {
					System.out.println("Line " + lineNumber + ": missing attr_stmt brace");
				}
			} else if (accept("EDGEOP")) {
				// is edgeRHS
				parseEdgeRHS();
				if (accept("LEFT-BRACKET")) {
					parseAttrList();
				}
			} else if (accept("ASSIGNMENT")) {
				// is ID '=' ID
				if (!accept("ID")) {
					System.out.println("Line " + lineNumber + ": missing ID after assignment");
					System.exit(1);
				}
			} else if (accept("LEFT-BRACKET")) {
				// is node_stmt
				parseAttrList();
			}
		} else if (accept("SUBGRAPH")) {
			parseSubgraph();
		} else if (accept("LEFT-BRACE")) {
			// move r-value subgraph up to this level
			parseStmtList();
			if (!accept("RIGHT-BRACE")) {
				System.out.println("Line " + lineNumber + ": missing closing brace");
				System.exit(1);
			}
		}

		// Optional after each statement
		accept("SEMICOLON");

		// Check if finished
		if (accept("RIGHT-BRACE")) {
			// End graph
			// No error if no stmt in stmt_list
			if (accept("EOF")) {
				// Finished
				System.out.println("No errors found");
				System.exit(0);
			} else {
				// Error: code after finish
				System.out.println("Line " + lineNumber + ": code after program termination");
				System.exit(1);
			}
		} else if (accept("EOF")) {
			// Error: missing closing bracket
			System.out.println("Line " + lineNumber + ": missing closing brace");
			System.exit(1);
		} else {
			parseStmtList();
		}
	}

	static void parseNodeId() {
		if (accept("COLON")) {
			parsePort();
		}
	}

	static void parsePort() {
		if (accept("ID")) {
			if (accept("COLON")) {
				parseCompassPt();
			}
		} else {
			System.out.println("Line " + lineNumber + ": missing port ID");
			System.exit(1);
		}
	}
	
	static void parseCompassPt() {
		if (accept("N") || accept("NE") || accept("E") || accept("SE") || accept("S") || accept("SW") || accept("W") || accept("NW") || accept("C") || accept("_")) {
			// is valid compass point
		} else {
			System.out.println("Line " + lineNumber + ": invalid compass point");
			System.exit(1);
		}
	}

	static void parseAttrList() {
		if (accept("ID")) {
			parseAList();
		}
		if (accept("LEFT-BRACKET")) {
			parseAttrList();
		}
		if (!accept("RIGHT-BRACKET")) {
			System.out.println("Line " + lineNumber + ": missing closing bracket");
			System.exit(1);
		}
	}

	static void parseAList() {
		if (accept("ASSIGNMENT")) {
			if (accept("ID")) {
				// Optionals
				// accept("SEMICOLON");
				accept("COMMA");
				if (accept("ID")) {
					parseAList();
				}
			} else {
				System.out.println("Line " + lineNumber + ": missing assigned value");
				System.exit(1);
			}
		} else {
			System.out.println("Line " + lineNumber + ": missing assignment");
			System.exit(1);
		}
	}

	static void parseEdgeRHS() {
		if (accept("ID") || accept("SUBGRAPH")) {
			parseNodeId();
			if (accept("EDGEOP")) {
				parseEdgeRHS();
			}
		} else {
			System.out.println("Line " + lineNumber + ": missing target node ID");
			System.exit(1);
		}
	}

	static void parseSubgraph() {
		accept("ID");
		if (accept("LEFT-BRACKET")) {
			parseStmtList();
			if (!accept("RIGHT-BRACKET")) {
				System.out.println("Line " + lineNumber + ": missing closing bracket");
				System.exit(1);
			}
		} else {
			System.out.println("Line " + lineNumber + ": missing bracket");
			System.exit(1);
		}
	}

	// Grammar
// 	graph 	: 	[ strict ] (graph | digraph) [ ID ] '{' stmt_list '}'
// 	stmt_list 	: 	[ stmt [ ';' ] stmt_list ]
// 	stmt 	: 	node_stmt
// 	| 	edge_stmt
// 	| 	attr_stmt
// 	| 	ID '=' ID
// 	| 	subgraph
// 	attr_stmt 	: 	(graph | node | edge) attr_list
// 	attr_list 	: 	'[' [ a_list ] ']' [ attr_list ]
// 	a_list 	: 	ID '=' ID [ (';' | ',') ] [ a_list ]
// 	edge_stmt 	: 	(node_id | subgraph) edgeRHS [ attr_list ]
// 	edgeRHS 	: 	edgeop (node_id | subgraph) [ edgeRHS ]
// 	node_stmt 	: 	node_id [ attr_list ]
// 	node_id 	: 	ID [ port ]
// 	port 	: 	':' ID [ ':' compass_pt ]
// 	| 	':' compass_pt
// 	subgraph 	: 	[ subgraph [ ID ] ] '{' stmt_list '}'
// 	compass_pt 	: 	(n | ne | e | se | s | sw | w | nw | c | _)
}
