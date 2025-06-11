import java.util.List;

public class TestSintactico {
    public static void main(String[] args) {
        try {
            String inputFileName = "test_input.txt"; 
            String csvFileName = "Accion-GoTo.csv"; 
            ts symbolTable = new ts("TablaGloval", 0);
            analizadorLexico lexer = new analizadorLexico(inputFileName, symbolTable);

            
            Rule[] grammar = {
                new Rule("S", new String[] {"id", "S1"}),
                new Rule("S", new String[] {"output", "E", ";"}),
                new Rule("S", new String[] {"input", "(", "id", ")", ";"}),
                new Rule("S", new String[] {"return", "X", ";"}),
                new Rule("S", new String[] {"break", ";"}),
                new Rule("S", new String[] {"case", "V", ":", "S"}),
            
                new Rule("F", new String[] {"function", "F1", "F2", "F3", "{", "C", "}"}),
                new Rule("F1", new String[] {"H"}),
                new Rule("F2", new String[] {"id"}),
                new Rule("F3", new String[] {"(", "A", ")"}),
            
                new Rule("B", new String[] {"if", "(", "E", ")", "S"}),
                new Rule("B", new String[] {"var", "T", "id", ";"}),
                new Rule("B", new String[] {"S"}),
                new Rule("B", new String[] {"switch"}),
            
                new Rule("P", new String[] {"B", "P"}),
                new Rule("P", new String[] {"F", "P"}),
                new Rule("P", new String[] {"eof"}),
                new Rule("P", new String[] {"$"}),
            
                new Rule("E", new String[] {"Z", "E1"}),
                new Rule("Z", new String[] {"R", "Z1"}),
                new Rule("R", new String[] {"U", "R1"}),
                new Rule("U", new String[] {"V", "U1"}),
                new Rule("V", new String[] {"id", "V1"}),
                new Rule("V", new String[] {"constEntera"}),
                new Rule("V", new String[] {"cadena"}),
                new Rule("V1", new String[] {"(", "V2", ")"}),
                new Rule("V2", new String[] {"L"}),
                new Rule("S1", new String[] {"=", "E", ";"}),
                new Rule("S1", new String[] {"(", "L", ")", ";"}),
                new Rule("L", new String[] {"E", "Q"}),
                new Rule("L", new String[] {"lambda"}),
                new Rule("Q", new String[] {",", "E", "Q"}),
                new Rule("Q", new String[] {"lambda"}),
                new Rule("X", new String[] {"E"}),
                new Rule("X", new String[] {"lambda"}),
                new Rule("C", new String[] {"B", "C"}),
                new Rule("C", new String[] {"lambda"}),
                new Rule("H", new String[] {"T"}),
                new Rule("H", new String[] {"lambda"}),
                new Rule("H", new String[] {"void"}),
                new Rule("A", new String[] {"T", "id", "K"}),
                new Rule("A", new String[] {"lambda"}),
                new Rule("A", new String[] {"void"}),
                new Rule("K", new String[] {",", "T", "id", "K"}),
                new Rule("K", new String[] {"lambda"}),
                new Rule("T", new String[] {"int"}),
                new Rule("T", new String[] {"boolean"}),
                new Rule("T", new String[] {"string"}),
                new Rule("E1", new String[] {"&&", "Z", "E1"}),
                new Rule("E1", new String[] {"lambda"}),
                new Rule("Z1", new String[] {"<", "R", "Z1"}),
                new Rule("Z1", new String[] {"lambda"}),
                new Rule("R1", new String[] {"*", "U", "R1"}),
                new Rule("R1", new String[] {"lambda"}),
                new Rule("U1", new String[] {"-=", "V", "U1"}),
                new Rule("U1", new String[] {"lambda"}),
            };
            

            analizadorSintactico parser = new analizadorSintactico(lexer, csvFileName, grammar, symbolTable);

            List<String> trace = parser.compute();

            
            System.out.println("Parsing Trace:");
            for (String step : trace) {
                System.out.println(step);
            }
        } catch (Exception e) {
            //System.err.println("Error: " + e.getMessage());
            e.printStackTrace(); 
        }
        
    }
}


