all: lexer.java
	javac lexer.java
	java Lexer input.txt
