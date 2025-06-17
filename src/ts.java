import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Entry{
    static final int TAM_ENTERO = 1;
    static final int TAM_LOGICO = 1;
    static final int TAM_CADENA = 64;
    static final int TAM_VACIO = 0;

    private String name, etiq;
    private AnalizadorSemantio.Type tipo;
    private int desplazamiento, numParametros, ancho;
    private ArrayList<Pair<String, Integer>> params;

    public Entry(String name){
        this.name = name;
        tipo = AnalizadorSemantio.Type.RECURSIVE;
        params = new ArrayList<>();
        numParametros = 0;
    }

    public int setDesplazamiento(int d){
        desplazamiento = d;
        return d + ancho;
    }

    public void setTipo(AnalizadorSemantio.Type t){
        tipo = t;
        switch (tipo) {
            case LOGICO:
                setAncho(TAM_LOGICO);
                break;
            case ENTERO:
                setAncho(TAM_ENTERO);
                break;
            case CADENA:
                setAncho(TAM_CADENA);
                break;
            case VACIO:
                setAncho(TAM_VACIO);
                break;
            default: System.err.println("ts: non valid type provided"); break;
        }
    }

    public void setEtiq(String t){
        etiq = t;
    }

    public void setAncho(int  t){
        ancho = t;
    }

    public void addParametro(AnalizadorSemantio.Type type){
        int mode;
        switch (type) {
            case ENTERO: mode = 1; break;
            case LOGICO: mode = 1; break;
            default: mode = 2;
        }
        params.add(0, new Pair<String,Integer>(typeToString(type), mode));
        numParametros++;
    }

    public void setParametros(Pair<String, Integer>[] tiposConRef){
        for(int i =0; i < tiposConRef.length;i++)
            params.add(tiposConRef[i]);
    }

    public String getName(){return name;}

    public String getEtiqueta(){return etiq;}

    public AnalizadorSemantio.Type getTipo(){return tipo;}

    public int getAncho(){return ancho;}

    public int getNumPerametros(){return numParametros;}

    public int getDesplazamiento(){return desplazamiento;}

    public ArrayList<Pair<String, Integer>> getParametros(){return params;} 

    public String getTipoString (){
            return typeToString(getTipo());
    }

    private String typeToString(AnalizadorSemantio.Type type){
        switch (type) {
            case CADENA: return "cadena";
            case ENTERO: return "entero";
            case LOGICO: return "logico";
            case VACIO: return "vacio";
            default: return "ERROR";
        }
    } 

}

public class ts {
    
    private String name;
    int num;
    int desp;
    private int nextId;

    private Map<Integer, Entry>  ts;

    private Map<String, String>  reservedKeyWords;
    
    public ts(String name, int num){
        reservedKeyWords = new HashMap<String, String>();
        reservedKeyWords.put("boolean", "boolean");
        reservedKeyWords.put("break", "break");
        reservedKeyWords.put("function", "function");
        reservedKeyWords.put("if", "if");
        reservedKeyWords.put("input", "input");
        reservedKeyWords.put("int", "int");
        reservedKeyWords.put("void", "void");
        reservedKeyWords.put("output", "output");
        reservedKeyWords.put("return", "return");
        reservedKeyWords.put("string", "string");
        reservedKeyWords.put("switch", "switch");
        reservedKeyWords.put("var", "var");
        reservedKeyWords.put("-=", "decAsig");
        reservedKeyWords.put("=", "asig");
        reservedKeyWords.put(",", "coma");
        reservedKeyWords.put(";", "puntoYComa");
        reservedKeyWords.put(":", "dosPuntos");
        reservedKeyWords.put("(", "aperturaParentesis");
        reservedKeyWords.put(")", "cierreParentesis");
        reservedKeyWords.put("{", "aperturaLlave");
        reservedKeyWords.put("}", "cierreLlave");
        reservedKeyWords.put("*", "mult");
        reservedKeyWords.put("&&", "and");
        reservedKeyWords.put("<", "menor");
        reservedKeyWords.put("false", "false");
        reservedKeyWords.put("true", "true");
        reservedKeyWords.put("$", "$");

		this.name = name;
        this.num = num;
        desp = 0;
        ts = new HashMap<Integer, Entry>();
        nextId = 0;
	}

    public String getKeyWordName(String s){
        if(reservedKeyWords.containsKey(s)) return reservedKeyWords.get(s);
        else return s;
    }

    public Token<Integer> genToken(String token) {
        //busca palabra reservada
        for(int i=0; i < 26;i++){
			if(reservedKeyWords.containsKey(token))
				return new Token<Integer>(reservedKeyWords.get(token));
		}
        //busca identificador
		for(int i= 0; i < ts.size(); i++){
			if(token.equals(ts.get(i).getName()))
				return new Token<Integer>("id", i);
		}
        //crear identificador
		ts.put(nextId, new Entry(token));
		return new Token<Integer>("id", nextId++);
    }

    void setDesplazamiento(int desplazamiento){
        desp = desplazamiento;
    }

    String getName(){return name;}

    int size(){return nextId;}

    void changeName(String newName){name  = newName;}

    Entry getEntry(int desplazamiento){
        return ts.get(desplazamiento);
    }

    public int getSizeTipo (AnalizadorSemantio.Type t){
            switch (t) {
            case CADENA: return Entry.TAM_CADENA;
            case ENTERO: return Entry.TAM_ENTERO;
            case LOGICO: return Entry.TAM_LOGICO;
            default: return 0;
        }
    }

    public void dump(String file){
        try{
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(name + " #"+ num +":\n");
            for(int i =0; i < ts.size(); i++){
                if(ts.get(i).getEtiqueta() == null){
                    fileWriter.write(" * lexema: \'" +  ts.get(i).getName() + "\'\n");
                    if(ts.get(i).getAncho() > getSizeTipo(ts.get(i).getTipo())){   
                        fileWriter.write("    + tipo: \'vector\'\n");
                        fileWriter.write("    + tam: "+ ts.get(i).getAncho() + "\n");
                    }
                    else fileWriter.write("    + tipo: " + "\'" + ts.get(i).getTipoString() + "\'\n");
                    desp = ts.get(i).setDesplazamiento(desp);
                    fileWriter.write("  + despl: " + ts.get(i).getDesplazamiento() + "\n");
                }
                else{
                    fileWriter.write(" * lexema:"+ ts.get(i).getName() + "\n   +tipo: \'funcion\'\n    +numParam: " + ts.get(i).getNumPerametros() + "\n");
                    for(int a = 0; a < ts.get(i).getNumPerametros(); a++){
                        fileWriter.write("    +TipoParam" + (a +1) +": " + "\'" + ts.get(i).getParametros().get(a).getKey() + "\'" + "\n"); 
                        fileWriter.write("     +ModoParam" + (a +1) +": " + ts.get(i).getParametros().get(a).getValue() + "\n"); //1 se pasa por valor 2 por referencia
                    }
                    fileWriter.write("    +TipoRetorno: " + "\'"+ ts.get(i).getTipoString() + "\'"+ "\n");
                    fileWriter.write("    +EtiqFuncion: " + "\'"+ ts.get(i).getEtiqueta() + "\'"+ "\n");
                }
            }
            fileWriter.close();
        }catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

