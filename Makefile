all: lexer.java parser.java
	javac lexer.java
	javac parser.java
	java Parser input.txt
