import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class analizadorLexico {
    private static final int EOF = -1;

    private FileReader fileReader;
    private char currentChar;
    private boolean canRead;
    private int currentState;
    private ts symbolTable;

    private String lexeme;
    private int count;
    private int value;
    private int currentLine; // Keep track of the current line number

    public analizadorLexico(String fileName, ts symbolTable) throws FileNotFoundException {
        this.fileReader = new FileReader(new File(fileName));
        this.canRead = true;
        this.currentState = 0;
        this.symbolTable = symbolTable;
        this.lexeme = "";
        this.count = 0;
        this.value = 0;
        this.currentLine = 1; // Start from the first line
    }

    private void readNextChar() throws IOException {
        int charRead = fileReader.read();
        if (charRead != EOF) {
            currentChar = (char) charRead;
            if (currentChar == '\n') {
                currentLine++; // Increment line number on new line
            }
        } else {
            currentChar = '\uFFFF'; // Represent EOF
        }
    }

    public Token<?> nextToken() throws IOException {
        while (true) {
            if (currentState == -1) { // If the lexer is in exit state, stop processing
                return symbolTable.genToken("eof");
            }

            if (canRead) {
                readNextChar();
                canRead = false;
            }

            switch (currentState) {
                case 0: // Initial state
                    if (Character.isLetter(currentChar)) {
                        lexeme = "" + currentChar;
                        count = 1;
                        currentState = 1; // Move to identifier state
                        canRead = true;
                    } else if (Character.isDigit(currentChar)) {
                        value = Character.getNumericValue(currentChar);
                        currentState = 2; // Move to number state
                        canRead = true;
                    } else if (currentChar == '\'') {
                        lexeme = ""; // Start collecting the string
                        currentState = 3; // Move to string state
                        canRead = true;
                    } else if (currentChar == '=') {
                        canRead = true;
                        return symbolTable.genToken("=");
                    } else if (currentChar == '{') {
                        canRead = true;
                        return symbolTable.genToken("{");
                    } else if (currentChar == '}') {
                        canRead = true;
                        return symbolTable.genToken("}");
                    } else if (currentChar == '(') {
                        canRead = true;
                        return symbolTable.genToken("(");
                    } else if (currentChar == ')') {
                        canRead = true;
                        return symbolTable.genToken(")");
                    } else if (currentChar == '*') {
                        canRead = true;
                        return symbolTable.genToken("*");
                    } else if (currentChar == '<') {
                        canRead = true;
                        return symbolTable.genToken("<");
                    } else if (currentChar == ';') {
                        canRead = true;
                        return symbolTable.genToken(";");
                    } else if (currentChar == ',') {
                        canRead = true;
                        return symbolTable.genToken(",");
                    } else if (currentChar == ':') {
                        canRead = true;
                        return symbolTable.genToken(":");
                    }else if (currentChar == '&') {
                        readNextChar();
                        if (currentChar == '&') {
                            canRead = true;
                            return symbolTable.genToken("&&");
                        } else {
                            // Error: Single '&' is not a valid token
                            String errorMessage = "Invalid character: '&' at line " + currentLine;
                            canRead = false; // Reprocess the current character in the next cycle
                            return new Token<>("error", errorMessage);
                        }
                    }
                    else if (currentChar == '-') {
                        readNextChar();
                        if (currentChar == '=') {
                            canRead = true;
                            return symbolTable.genToken("decAsig");
                        } else {
                            // Error: Single '-' is not valid
                            String errorMessage = "Invalid character: '-' at line " + currentLine;
                            canRead = false; // Reprocess the current character in the next cycle
                            return new Token<>("error", errorMessage);
                        }
                    }
                     else if (currentChar == '/') {
                        canRead = true;
                        return symbolTable.genToken("div");
                    } else if (currentChar == '\uFFFF') { // EOF
                        currentState = -1; // Mark lexer as done
                        return symbolTable.genToken("eof");
                    } else if (Character.isWhitespace(currentChar)) {
                        canRead = true; // Skip whitespace
                    } else {
                        // Error: Invalid character
                        String errorMessage = "Invalid character: '" + currentChar + "' at line " + currentLine;
                        canRead = true; // Consume the character
                        return new Token<>("error", errorMessage); // Return error token
                    }
                    break;

                case 1: // Identifier state
                    if (Character.isLetterOrDigit(currentChar)) {
                        lexeme += currentChar;
                        count++;
                        canRead = true;
                    } else {
                        if (count <= 64) {
                            //String lexemeValue = symbolTable.searchOrInsert(lexeme); // Get lexeme
                            currentState = 0;
                            return symbolTable.genToken(lexeme); // Use lexeme as value
                        } else {
                            String errorMessage = "Identifier too long: '" + lexeme + "' at line " + currentLine;
                            currentState = 0;
                            return new Token<>("error", errorMessage); // Return error token
                        }
                    }
                    break;

                case 2: // Number state
                    if (Character.isDigit(currentChar)) {
                        value = value * 10 + Character.getNumericValue(currentChar);
                        canRead = true;
                    } else {
                        if (value <= 32767) {
                            currentState = 0;
                            return new Token<>("constEntera", value);
                        } else {
                            String errorMessage = "Number too large: " + value + " at line " + currentLine;
                            currentState = 0;
                            return new Token<>("error", errorMessage); // Return error token

                        }
                    }
                    break;

                    case 3: // String literal (cadena) state
                    if (currentChar != '\'' && currentChar != '\uFFFF' && currentChar != '\n') {
                        lexeme += currentChar;
                        canRead = true;
                    } else if (currentChar == '\'') { // Closing single quote
                        canRead = true; // Consume the closing quote
                        currentState = 0;
                        return new Token<>("constEntera", lexeme); // Emit token with the string value
                    } else { // Unterminated string literal
                        String errorMessage = "Unterminated string literal: '" + lexeme + "' at line " + currentLine;
                        currentState = 0;
                        return new Token<>("error", errorMessage);
                    }
                    break;
                

                default:
                    // Error: Unknown state
                    String errorMessage = "Unknown state: " + currentState + " at line " + currentLine;
                    currentState = 0; // Reset state
                    return new Token<>("error", errorMessage); // Return error token
            }
        }
    }
}
