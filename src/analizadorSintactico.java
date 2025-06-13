import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class StackType {//para poder meter estados y tokens en el stack
    Token<?> token;
    String estado; //TODO estado tendra que que guardar el tipo de la expresión para en analizador semantico que ocurre de foma simultanea al sintactico
    boolean isToken;
    public StackType(Token<?> t){
        token = t;
        isToken = true;
    }

    public StackType(String s){
        estado = s;
        isToken = false;
    }

    public String toString(){
        if(isToken) return token.toString();
        else return estado;
    }
    
}

class Rule {//para poder meter estados y tokens en el stack
    public String antecedene;
    public String[] elementos; 
    public Rule(String antecedente, String[] resultado){
        this.antecedene = antecedente;
        elementos = new String[resultado.length];
        for (int i = 0; i < resultado.length; i++) {
            elementos[i] = resultado[i];
        }
    }
}

public class analizadorSintactico {
    //cambiar esta parte del codigo dependiendo de los separadores del csv
    static final String CSV_SEPARADOR = ";";

    private Stack<StackType> stack;
    private analizadorLexico AL;
    private ts tablaSim;

    Rule[] rules;

    //los mapas sirven para ubicar la columna más rapidamente en la tabla
    private Map<String, Integer> actionMap; 
    private String[][] actionTable;

    private Map<String, Integer> gotoMap;
    private int[][] gotoTable;
    
    /*gramatica de ejemplo
     * 
     * S -> +A
     * A -> λ
     * 
     * Rule[] gramar = {
     * new Rule("S", {"+", "A"}),
     * new Rule("A", {}) //en caso de expresar lambda se usa la cedena vacia
     * };
    */
    public analizadorSintactico(analizadorLexico AL, String csvName, Rule[] gramar, ts tablaSimbolos){
        this.AL = AL;
        tablaSim = tablaSimbolos;
        stack = new Stack<>();
        actionMap = new HashMap<>();
        gotoMap = new HashMap<>();
        rules = gramar;

        int actionTableColums = 0;
        int curLine  = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvName));
            String line;
            //cuenta para ver cuantas columas tiene la tabla acción porque asume que la tabla goto emieza por A
            if((line = br.readLine()) == null){
                System.out.println("El archivo csv tiene un fallo de formato.");
                br.close();
                return;
            } 

            String[] cells = line.split(CSV_SEPARADOR);// Dividir la línea en columnas usando coma como separador OJO algunos csv usan ; en vez de ,
            boolean foundA = false;
            int offset = 0;
            for (int i = 1; i < cells.length; i++) {
                if (cells[i].trim().equals("S") && !foundA){
                    foundA = true;
                    actionTableColums = i -1;
                }

                if(!foundA){
                    if(cells[i].equals("\"")){
                        actionMap.put(CSV_SEPARADOR, i-1 + offset);
                        i++;
                        offset -=1;
                    }
                    actionMap.put(cells[i], i-1 + offset);
                } 
                else gotoMap.put(cells[i], i - actionTableColums + offset);
            }
            int lines = (int)br.lines().count();
            br.close();
            br = new BufferedReader(new FileReader(csvName));
            br.readLine();
            actionTable = new String[lines][actionTableColums];
            gotoTable = new int[lines][(cells.length) -actionTableColums];
            // Leer el archivo línea por línea
            while ((line = br.readLine()) != null) {
                cells = line.split(CSV_SEPARADOR);
                for(int i = 1; i < cells.length;i++){
                    if(i <= actionTableColums) actionTable[curLine][i -1] = cells[i];
                    else if(!(cells[i].equals(""))) gotoTable[curLine][i - (actionTableColums)] = Integer.parseInt(cells[i]);
                }
                curLine++;
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private String getActionTable(String readInput, int stateNumber){
        return actionTable[stateNumber][actionMap.get(readInput)];
    }

    private int getGotoTable(String readInput, int stateNumber){
        return gotoTable[stateNumber][gotoMap.get(readInput)];
    }

    /*
     * devuelve la traza de las reglas empeladas
     * apila el analizador sintactico y el analizador semantico
     */
    List<String> compute(){
        List<String> trace = new LinkedList<String>();
        Integer state = 0;
        Token<?> token;
        stack.clear();
        stack.add(new StackType("$"));//añadimos el fondo de pila
        System.err.println(gotoMap.toString());

        try{
            while (!stack.empty()) {
                token = AL.nextToken();
                //separa la información de la celda correspondiente en la letra y numero
                System.err.println("token: " + token.toString() + "\tstate: " + state);
                System.err.println("stack: " + stack.toString() + "\n");
                if(!actionMap.containsKey(token.name)) throw new NotValidTokenException();
                String[] cell = getActionTable(token.name, state).split("(?<=\\D)(?=\\d)");
                if(cell.length != 2) throw new NotValidTokenException();
                if(cell[0].equals("s")){//Accion de desplazar o Stack
                    stack.push(new StackType(token));
                    state = Integer.parseInt(cell[1]);
                    stack.push(new StackType(state.toString()));
                }
                else{//Accion de reducir
                    Integer rule = Integer.parseInt(cell[1]); 
                    for(int n =  2 * rules[rule].elementos.length; n > 0; n--){
                        StackType item = stack.pop();
                        //TODO comporbar que el tipo es correcto ANALIZADOR SEMANTICO y apilar el tipo croespondiente
                    }
                    //TODO semantico
                    StackType temp = stack.peek();
                    stack.push(new StackType(rules[rule].antecedene));
                    state = getGotoTable(rules[rule].antecedene, Integer.parseInt(temp.estado));
                    stack.push(new StackType(state.toString()));
                    //TODO las reglas no estan correctamente ordenadas en el conjunto rules y el antecedenteno coincide
                }
                trace.add(cell[0] + cell[1]);
            }

        }catch( NotValidTokenException | IOException e){
            System.err.println("Error al procesar el lenguaje: " + e.getMessage());
        }

        return trace;
    }

    String actionTableToString(){
        String result= actionMap.toString() + "\n";
        for(int i = 0; i < actionTable.length; i++){
            for(int j = 0; j < actionTable[i].length; j++){
                result = result + actionTable[i][j]+",";
            }
            result = result + "\n";
        }
        return result;
    }

    String gotoTableToString(){
        String result= gotoMap.toString() + "\n";
        for(int i = 0; i < gotoTable.length; i++){
            for(int j = 0; j < gotoTable[i].length; j++){
                result = result + gotoTable[i][j]+",";
            }
            result = result + "\n";
        }
        return result;
    }

}
