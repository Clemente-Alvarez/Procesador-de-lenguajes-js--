import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class AnalizadorSemantio {
    private final String OUTPUTFILE = "grammar/output.txt";

    public enum Type {
        ERROR,
        TIPO_OK,
        CADENA,
        ENTERO,
        VACIO,
        LOGICO
    }

    private boolean zonaDeclaracion;
    ts mainTs;
    ts tsl;
    private int numTabla;
    

    public AnalizadorSemantio(){
        mainTs = new ts("TablaGloval", 0);
        tsl = mainTs;
        numTabla = 1;
        zonaDeclaracion  = true;
        try{
                FileWriter fileWriter = new FileWriter(OUTPUTFILE);
                fileWriter.flush();
                fileWriter.close();
        }
        catch(IOException e){
                System.err.println("ASe: fail to write on " + OUTPUTFILE);
        }
    }

    ts getTs(){
        return tsl;
    }

    public void computeStack(Token<?> t){
        switch (t.getName()) {
                case "var": zonaDeclaracion = true;        
                        break;
                case "funcion": zonaDeclaracion = true;
                        break;
                default: break;
        }
    }

    public Type computeReduce(StackType[] data, int rule){
        switch (rule) {
             //S' -> S
            case 0: tsl.dump(OUTPUTFILE);
                    return data[0].getType();
            //S -> B S
            case 1: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return data[0].getType();
            //S -> F S
            case 2: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return data[0].getType();
           //S -> lambda
           case 3: return Type.TIPO_OK;

            //P -> id S1
            case 4: if(data[0].getToken().getName() != "id"){
                        System.out.println("a variable was expected at " + getTs().getName());
                        return Type.ERROR;
                    }
                    else if(tsl.getEntry((Integer)data[0].getToken().getMod()).getTipo() == Type.ERROR){
                            System.out.println("the variable was not declared yet at " + getTs().getName());
                            return Type.ERROR;
                    }
                    else return tsl.getEntry((Integer)data[0].getToken().getMod()).getTipo();
            //P -> output E ;
            case 5: if(data[1].getType() == Type.CADENA || data[1].getType() == Type.ENTERO) return Type.TIPO_OK;
                    else{
                        System.err.println("boolean outputs not permited at " + getTs().getName());
                        return Type.ERROR;
                    }
            //P -> input ( id ) ;
            case 6: if(data[2].getToken().getName() != "id"){
                        System.out.println("a variable was expected at " + getTs().getName());
                        return Type.ERROR;
                    }
                    else if(tsl.getEntry((Integer)data[2].getToken().getMod()).getTipo() == Type.CADENA 
                            || tsl.getEntry((Integer)data[2].getToken().getMod()).getTipo()  == Type.ENTERO)return Type.TIPO_OK;
                    else{            
                            System.out.println("boolean inputs not permited at "+ getTs().getName());
                            return Type.ERROR;
                    }
            //P -> return X ;
            case 7: return data[1].getType();
            //P -> break ;
            case 8: return Type.TIPO_OK;
            //P -> case V : P
            case 9: return data[1].getType();

            //F -> function F1 F2 F3 { C }
            case 10: Type t;
                    mainTs.getEntry(mainTs.size() -1).setTipo(data[1].getType());
                    zonaDeclaracion = false;
                    if(data[5].getType() != data[1].getType()){
                        System.err.println(data[5] + "does not match with " + data[1] +" of "+ getTs().getName());
                        t =  Type.ERROR;
                    }
                    else t = Type.TIPO_OK;
                    tsl.dump(OUTPUTFILE);
                    tsl = mainTs;
                    return t;
            //F1 -> H
            case 11: return data[0].getType();
            //F2 -> id
            case 12: tsl = new ts(tsl.getEntry((Integer)data[0].getToken().getMod()).getName(), numTabla++);
                     mainTs.getEntry((Integer)data[0].getToken().getMod()).setEtiq(mainTs.getEntry((Integer)data[0].getToken().getMod()).getName());
                     return Type.TIPO_OK;
            //F3 -> ( A )
            case 13: return data[1].getType();

            //B -> if ( E ) P
            case 14: if(data[2].getType() != Type.LOGICO){
                System.err.println("a logic statement is required at " + getTs().getName());
                return Type.ERROR;
            }
                return data[4].getType();
            //B -> var T id ;
            case 15: if(zonaDeclaracion){
                        tsl.getEntry((Integer)data[2].getToken().getMod()).setTipo(data[1].getType());
                        zonaDeclaracion = false;
                        return Type.TIPO_OK;
                     }else{
                        System.err.println("declaration out of declaration zone at "+ getTs().getName());
                        return Type.ERROR;
                    }
            //B -> P
            case 16: return data[0].getType();
            //B -> switch ( E ) { C }
            case 17: if(data[3].getType() == Type.CADENA || data[3].getType() == Type.ENTERO) return data[5].getType();
                    else{
                        System.err.println("switch requires a string or integer at " + getTs().getName());
                        return Type.ERROR;
                    }
            //E -> Z E1
            case 18: if(data[1].getType() != Type.ERROR) return data[0].getType();
                    else return Type.ERROR;
            //Z -> R Z1
            case 19: if(data[1].getType() != Type.ERROR) return data[0].getType();
                    else return Type.ERROR;
            //R -> U R1
            case 20: if(data[1].getType() != Type.ERROR) return data[0].getType();
                    else return Type.ERROR;
            //U -> V U1
            case 21: if(data[1].getType() == Type.TIPO_OK) return data[0].getType();
                    else if(data[0].getType() != Type.ENTERO){
                        System.err.println("The parameters must be integers at " + getTs().getName());
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;

            //V -> id V1
            case 22: return tsl.getEntry((Integer)data[0].getToken().getMod()).getTipo();
            //V -> constEntera
            case 23: return Type.ENTERO;
            //V -> cadena
            case 24: return Type.CADENA;

            //V1 -> ( V2 )
            case 25: return data[1].getType();
            //V2 -> L
            case 26: return data[0].getType();
            //S1 -> = E ;
            case 27: return data[1].getType();
            //S1 -> ( L ) ;
            case 28: return data[1].getType();

            //L -> E Q
            case 29: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.ERROR;
            //L -> lambda
            case 30: return Type.VACIO;
            
            //Q -> , E Q
            case 31: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.ERROR;
            //Q -> lambda
            case 32: return Type.VACIO;

            //X -> E
            case 33: return data[0].getType();
            //X -> lambda
            case 34: return Type.VACIO;

            //C -> B C
            case 35: if(data[0].getType() == Type.TIPO_OK) return data[1].getType();
                    if(data[0].getType() != Type.ERROR) return data[0].getType();
                    else return Type.ERROR;
            //C -> lambda
            case 36: return Type.VACIO;

            //H -> T
            case 37: return data[0].getType();
            //H -> lambda
            case 38: return Type.VACIO;

            //A -> T id K
            case 39: if(zonaDeclaracion){
                        Entry entry = tsl.getEntry((Integer)data[1].getToken().getMod());
                        entry.setTipo(data[0].getType());
                        mainTs.getEntry(mainTs.size()-1).addParametro(data[0].getType());
                        zonaDeclaracion = false;
                        return Type.TIPO_OK;
                    }
                    else{
                        System.err.println("declaration out of declaration zone at " + getTs().getName());
                        return Type.ERROR;
                    }
            //A -> lambda
            case 40: return Type.TIPO_OK;
            //A -> void
            case 41: return Type.VACIO;

            //K -> , T id K
            case 42: if(zonaDeclaracion){
                        Entry entry = tsl.getEntry((Integer)data[2].getToken().getMod());
                        entry.setTipo(data[1].getType());
                        mainTs.getEntry(mainTs.size()-1).addParametro(data[1].getType());
                        return Type.TIPO_OK;
                    }
                    else{
                        System.err.println("declaration out of declaration zone at " + getTs().getName());
                        return Type.ERROR;
                    }
            //K -> lambda
            case 43: return Type.TIPO_OK;

            //T -> int
            case 44: zonaDeclaracion = true;
                        return Type.ENTERO;
            //T -> boolean
            case 45: zonaDeclaracion = true;
                        return Type.LOGICO;
            //T -> string
            case 46: zonaDeclaracion = true;
                        return Type.CADENA;
            //T -> void
            case 47: zonaDeclaracion = true;
                        return Type.VACIO;

            //E1 -> && Z E1
            case 48: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.LOGICO){
                        System.err.println("a boolean was expected at " + getTs().getName());
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;
            //E1 -> lambda
            case 49: return Type.TIPO_OK;

            //Z1 -> < R Z1
            case 50: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.ENTERO){
                        System.err.println("a boolean was expected at " + getTs().getName());
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;
            //Z1 -> lambda
            case 51: return Type.TIPO_OK;

            //R1 -> * U R1
            case 52: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.ENTERO){
                        System.err.println("a integer was expected at " + getTs().getName());
                        return Type.ERROR;
                    }
                    else return Type.ENTERO;
            //R1 -> lambda
            case 53: return Type.TIPO_OK;

            //U1 -> -= V U1
            case 54: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.ENTERO){
                        System.err.println("a integer was expected at" + getTs().getName());
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;
            //U1 -> lambda
            case 55: return Type.TIPO_OK;
            //V1 -> lambda
            case 56: return Type.TIPO_OK;

            default: System.out.println("internal semantic flaw at " + getTs().getName());
            return Type.ERROR;
        }
    }
}
