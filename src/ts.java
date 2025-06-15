import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Entry{
    String name, tipo, etiq;
    int desplazamiento, numParametros, ancho;
    ArrayList<Pair<String, Integer>> params;

    public Entry(String name){
        this.name = name;
        params = new ArrayList<>();
    }

    void setDesplazamiento(int d){
        desplazamiento = d;
    }

    void setTipo(String t){
        tipo = t;
    }

    void setEtiq(String t){
        etiq = t;
    }

    void setAncho(int  t){
        ancho = t;
    }

    void setNumParametros(int n){
        numParametros = n;
    }

    void setParametros(Pair<String, Integer>[] tiposConRef){
        for(int i =0; i < tiposConRef.length;i++)
            params.add(tiposConRef[i]);
    }
}

public class ts {
    
    public String name;
    int num;
    int desp;
    private int nextId;

    private Map<Integer, Entry>  ts;//TODO modificar la tabla de simbolos para que incluya los atrivutos

    private Map<String, String>  reservedKeyWords;
    
    public ts(String name, int num){
        reservedKeyWords = new HashMap<String, String>();
        reservedKeyWords.put("boolean", "boolean");
        reservedKeyWords.put("break", "break");
        reservedKeyWords.put("function", "function");
        reservedKeyWords.put("if", "if");
        reservedKeyWords.put("input", "input");
        reservedKeyWords.put("int", "int");
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
			if(token.equals(ts.get(i).name))
				return new Token<Integer>(token, i);
		}
        //crear identificador
		ts.put(nextId, new Entry(token));
		return new Token<Integer>("id", nextId++);
    }

    void setDesplazamiento(int desplazamiento){
        desp = desplazamiento;
    }

    public AnalizadorSemantio.Type getType(int pos){
        return AnalizadorSemantio.Type.ERROR;
    }

    public void dump(String file){
        try{
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(name + " #"+ num +":\n");
            for(int i =0; i < ts.size(); i++){
                if(!ts.get(i).equals("function")){
                    fileWriter.write(" * lexema: \'" +  ts.get(i).name + "\'\n");
                    if(ts.get(i).ancho > 1){   
                        fileWriter.write("    + tipo: \'vector\'\n");
                        fileWriter.write("    + tam: "+ ts.get(i).ancho + "\n");
                    }
                    else fileWriter.write("    + tipo: " + "\'" + ts.get(i).tipo + "\'\n");
                    fileWriter.write("  + despl: " + ts.get(i).desplazamiento + "\n");
                }
                else{
                    fileWriter.write(" * lexema:"+ ts.get(i).name + "\n+tipo: \'funcion\'\n    +numParam: " + ts.get(i).numParametros + "\n");
                    for(int a = 0; a < ts.get(i).numParametros; a++){
                        fileWriter.write("    +TipoParam" + (i +1) +": " + ts.get(i).params.get(i).getKey() + "\n"); 
                        fileWriter.write("    +ModoParam" + (i +1) +": " + ts.get(i).params.get(i).getValue() + "\n"); //1 se pasa por valor 2 por referencia
                    }
                    fileWriter.write("    +EtiqFuncion: " + ts.get(i).etiq + "\n");
                }
            }
            fileWriter.close();
        }catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

