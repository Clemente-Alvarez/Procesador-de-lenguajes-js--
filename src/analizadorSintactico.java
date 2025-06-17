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
    String estado;
    AnalizadorSemantio.Type tipo;
    boolean isToken;
    boolean hasType;
    public StackType(Token<?> t){
        token = t;
        hasType = false;
        isToken = true;
    }

    public StackType(String s){
        estado = s;
        hasType = false;
        isToken = false;
    }

    public StackType(String s, AnalizadorSemantio.Type t){
        estado = s;
        tipo = t;
        hasType = true;
        isToken = false;
    }

    public Token<?> getToken(){
        if(isToken) return token;
        else return null;
    }

    public String getEstado(){
        if(!isToken) return estado;
        else return null;
    }

    public AnalizadorSemantio.Type getType(){
        if(hasType) return tipo;
        else return AnalizadorSemantio.Type.ERROR;
    }

    public String toString(){
        if(isToken) return token.toString();
        else if(hasType) switch (tipo) {
            case TIPO_OK: return "[ "+ estado + ", TIPO_OK ]";
            case ERROR: return "[ "+ estado + ", ERROR ]";
            case CADENA: return "[ "+ estado + ", CADENA ]";
            case ENTERO: return "[ "+ estado + ", ENTERO ]";
            case VACIO: return "[ "+ estado + ", VACIO ]";
            case LOGICO: return "[ "+ estado + ", LOGICO ]";

        }
        return estado;
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
    private AnalizadorSemantio AS;

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
    public analizadorSintactico(String csvName, Rule[] gramar, String inputFileName){
        stack = new Stack<>();
        actionMap = new HashMap<>();
        gotoMap = new HashMap<>();
        rules = gramar;
        AS = new AnalizadorSemantio();
        try {
            AL = new analizadorLexico(inputFileName, AS);
        } catch (Exception e) {
            System.out.println(e);
        }

        int actionTableColums = 0;
        int curLine  = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvName));
            String line;
            //cuenta para ver cuantas columas tiene la tabla acción porque asume que la tabla goto emieza por A (correctión S')
            if((line = br.readLine()) == null){
                System.out.println("El archivo csv tiene un fallo de formato.");
                br.close();
                return;
            } 

            String[] cells = line.split(CSV_SEPARADOR);// Dividir la línea en columnas usando coma como separador OJO algunos csv usan ; en vez de ,
            boolean foundA = false;
            int offset = 0;
            for (int i = 1; i < cells.length; i++) {
                if (cells[i].trim().equals("S'") && !foundA){
                    foundA = true;
                    actionTableColums = i -1;
                }

                if(!foundA){
                    if(cells[i].equals("\"")){
                        actionMap.put(AS.getTs().getKeyWordName(CSV_SEPARADOR), i-1 + offset);
                        i++;
                        offset -=1;
                    }
                    else actionMap.put(AS.getTs().getKeyWordName(cells[i]), i-1 + offset);
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
        Token<?> token = null;
        stack.clear();
        stack.push(new StackType(state.toString()));//añadimos el fondo de pila

        try{
            boolean getNext = true;
            while (!stack.empty()) {
                if(getNext) token = AL.nextToken();
                getNext = false;
                //separa la información de la celda correspondiente en la letra y numero
                //System.err.println("token: " + token.toString() + "\tstate: " + state);
                //System.err.println("stack: " + stack.toString() + "\n");
                String columName;
                columName = token.getName();
                if(!actionMap.containsKey(columName)){
                    System.out.println("no se reconoce " +columName);
                    throw new NotValidTokenException();
                } 
                String op = getActionTable(columName, state);
                if(op.equals("acc")){//Accion accept
                    System.err.println("Sintaxis accepted!");
                    trace.add("acc");
                    stack.pop();
                    StackType temp[] = {stack.pop()};
                    AnalizadorSemantio.Type type = AS.computeReduce(temp, 0);
                    if(type == AnalizadorSemantio.Type.TIPO_OK) System.out.println("Semantic accepted!");
                    else System.out.println("Semantic falied!");
                    break;
                }
                String[] cell = op.split("(?<=\\D)(?=\\d)");
                if(cell.length != 2) {
                        System.err.println("transición no valida");
                        throw new NotValidTokenException();
                }
                if(cell[0].equals("s")){//Accion de desplazar o Stack
                        AS.computeStack(token);
                        stack.push(new StackType(token));
                    state = Integer.parseInt(cell[1]);
                    stack.push(new StackType(state.toString()));
                    getNext = true;
                }
                else{//Accion de reducir
                    Integer rule = Integer.parseInt(cell[1]); 
                    StackType[] reductionData = new StackType[rules[rule].elementos.length];
                    for(int n =  2 * rules[rule].elementos.length; n > 0; n--){
                        StackType item = stack.pop();
                        if( n % 2 == 1){
                            reductionData[n/2] = item;
                        }
                    }
                    AnalizadorSemantio.Type type = AS.computeReduce(reductionData, rule);
                    StackType temp = stack.peek();
                    stack.push(new StackType(rules[rule].antecedene, type));
                    state = getGotoTable(rules[rule].antecedene, Integer.parseInt(temp.estado));
                    stack.push(new StackType(state.toString()));
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
