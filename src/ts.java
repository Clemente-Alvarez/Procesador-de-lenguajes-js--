import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Entry{
    private String name, etiq;
    private AnalizadorSemantio.Type tipo;
    private int desplazamiento, numParametros, ancho;
    private ArrayList<Pair<String, Integer>> params;

    public Entry(String name){
        this.name = name;
        tipo = AnalizadorSemantio.Type.ERROR;
        params = new ArrayList<>();
    }

    public void setDesplazamiento(int d){
        desplazamiento = d;
    }

    public void setTipo(AnalizadorSemantio.Type t){
        tipo = t;
        switch (tipo) {
            case LOGICO:
                setAncho(1);
                setDesplazamiento(getDesplazamiento() +1);
                break;
            case ENTERO:
                setAncho(4);
                setDesplazamiento(getDesplazamiento() +4);
                break;
            case CADENA:
                setAncho(64);
                setDesplazamiento(getDesplazamiento() +64);
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

    public void setNumParametros(int n){
        numParametros = n;
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
            switch (getTipo()) {
            case TIPO_OK: return "TIPO_OK";
            case ERROR: return "ERROR";
            case CADENA: return "CADENA";
            case ENTERO: return "ENTERO";
            case VACIO: return "VACIO";
            case LOGICO: return "LOGICO";
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
        reservedKeyWords.put("eof", "eof");

		this.name = name;
        this.num = num;
        desp = 0;
        ts = new HashMap<Integer, Entry>();
        nextId = 0;
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

    public void dump(String file){
        try{
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(name + " #"+ num +":\n");
            for(int i =0; i < ts.size(); i++){
                if(!ts.get(i).equals("function")){
                    fileWriter.write(" * lexema: \'" +  ts.get(i).getName() + "\'\n");
                    if(ts.get(i).getAncho() > 1){   
                        fileWriter.write("    + tipo: \'vector\'\n");
                        fileWriter.write("    + tam: "+ ts.get(i).getAncho() + "\n");
                    }
                    else fileWriter.write("    + tipo: " + "\'" + ts.get(i).getTipoString() + "\'\n");
                    fileWriter.write("  + despl: " + ts.get(i).getDesplazamiento() + "\n");
                }
                else{
                    fileWriter.write(" * lexema:"+ ts.get(i).getName() + "\n+tipo: \'funcion\'\n    +numParam: " + ts.get(i).getNumPerametros() + "\n");
                    for(int a = 0; a < ts.get(i).getNumPerametros(); a++){
                        fileWriter.write("    +TipoParam" + (i +1) +": " + ts.get(i).getParametros().get(i).getKey() + "\n"); 
                        fileWriter.write("    +ModoParam" + (i +1) +": " + ts.get(i).getParametros().get(i).getValue() + "\n"); //1 se pasa por valor 2 por referencia
                    }
                    fileWriter.write("    +EtiqFuncion: " + ts.get(i).getEtiqueta() + "\n");
                }
            }
            fileWriter.close();
        }catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

