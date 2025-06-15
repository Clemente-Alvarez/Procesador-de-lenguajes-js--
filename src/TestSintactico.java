import java.util.List;

public class TestSintactico {
    public static void main(String[] args) {
        try {
            String inputFileName = "gramar/test_input.txt"; 
            String csvFileName = "gramar/Accion-GoTo.csv"; 
            ts symbolTable = new ts("TablaGloval", 0);
            analizadorLexico lexer = new analizadorLexico(inputFileName, symbolTable);
            
            
            Rule[] grammar = {
                new Rule("S", new String[] {"B", "S"}),
                new Rule("S", new String[] {"F", "S"}),
                new Rule("S", new String[] {"eof"}),
                
                new Rule("P", new String[] {"id", "S1"}),
                new Rule("P", new String[] {"output", "E", ";"}),
                new Rule("P", new String[] {"input", "(", "id", ")", ";"}),
                new Rule("P", new String[] {"return", "X", ";"}),
                new Rule("P", new String[] {"break", ";"}),
                new Rule("P", new String[] {"case", "V", ":", "P"}),
            
                new Rule("F", new String[] {"function", "F1", "F2", "F3", "{", "C", "}"}),
                new Rule("F1", new String[] {"H"}),
                new Rule("F2", new String[] {"id"}),
                new Rule("F3", new String[] {"(", "A", ")"}),
            
                new Rule("B", new String[] {"if", "(", "E", ")", "P"}),
                new Rule("B", new String[] {"var", "T", "id", ";"}),
                new Rule("B", new String[] {"P"}),
                new Rule("B", new String[] {"switch"}),
            
            
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
                new Rule("A", new String[] {"T", "id", "K"}),
                new Rule("A", new String[] {"lambda"}),
                new Rule("A", new String[] {"void"}),
                new Rule("K", new String[] {",", "T", "id", "K"}),
                new Rule("K", new String[] {"lambda"}),
                new Rule("T", new String[] {"int"}),
                new Rule("T", new String[] {"boolean"}),
                new Rule("T", new String[] {"string"}),
                new Rule("T", new String[] {"void"}),
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


