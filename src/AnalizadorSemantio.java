
public class AnalizadorSemantio {
    public enum Type {
        TIPO_OK,
        ERROR,
        CADENA,
        ENTERO,
        VACIO,
        LOGICO
    }

    private boolean zonaDeclaracion;
    ts mainTs;
    ts tsl;


    

    public AnalizadorSemantio(ts tablaSim){
        mainTs = tablaSim;
        zonaDeclaracion  = false;
    }

    public Type compute(StackType[] data, int rule){
        switch (rule) {
            //S -> B S
            case 0: return Type.TIPO_OK;
            //S -> F S
            case 1: return Type.TIPO_OK; 
            //S -> eof
            case 2: return Type.TIPO_OK; 

            //P -> id S1
            case 3: if(data[0].getToken().getName() != "id"){
                        System.out.println("a variable was expected");
                        return Type.ERROR;
                    }
                    else if(tsl.getType((Integer)data[0].getToken().getMod()) == Type.ERROR){
                            System.out.println("the variable was not declared yet");
                            return Type.ERROR;
                    }
                    else return tsl.getType((Integer)data[0].getToken().getMod());
            //P -> output E ;
            case 4: if(data[1].getType() == Type.CADENA || data[1].getType() == Type.ENTERO) return Type.TIPO_OK;
                    else{
                        System.err.println("boolean outputs not permited");
                        return Type.ERROR;
                    }
            //P -> input ( id ) ;
            case 5: if(data[2].getToken().getName() != "id"){
                        System.out.println("a variable was expected");
                        return Type.ERROR;
                    }
                    else if(tsl.getType((Integer)data[2].getToken().getMod()) == Type.CADENA 
                            || tsl.getType((Integer)data[2].getToken().getMod()) == Type.ENTERO) return Type.TIPO_OK;
                    else{            
                            System.out.println("boolean inputs not permited");
                            return Type.ERROR;
                    }
            //P -> return X ;
            case 6: return data[1].getType();
            //P -> break ;
            case 7: return Type.TIPO_OK;
            //P -> case V : P
            case 8: return data[1].getType();

            //F -> function F1 F2 F3 { C }
            case 9: tsl = new ts(data[0].getEstado(), 1);
                    Type t;
                    zonaDeclaracion = true;
                    //TODO instertar la función en la tabla de simbolos 
                    zonaDeclaracion = false;
                    tsl.setDesplazamiento(0);
                    if(data[5].getType() != data[1].getType()){
                        System.err.println("Invalid return type");
                        t =  Type.ERROR;
                    }
                    else t = Type.TIPO_OK;
                    tsl = mainTs;
                    return t;
            //F1 -> H
            case 10: return data[0].getType();
            //F2 -> id
            case 11: return tsl.getType((Integer)data[0].getToken().getMod());
            //F3 -> ( A )
            case 12: return data[1].getType();

            //B -> if ( E ) P
            case 13: if(data[3].getType() != Type.LOGICO){
                System.err.println("a logic statement is required");
                return Type.ERROR;
            }
                return data[4].getType();
            //B -> var T id ;
            case 14: zonaDeclaracion = true;
                    //TODO instertar la variable en la tabla de simbolos 
                    //TODO modificar el desplazamiento
                    zonaDeclaracion = false;
            //B -> P
            case 15: return data[0].getType();
            //B -> switch ( E ) { C }
            case 16: if(data[3].getType() == Type.CADENA || data[3].getType() == Type.ENTERO) return data[5].getType();
                    else{
                        System.err.println("switch requires a string or integer");
                        return Type.ERROR;
                    }
            //E -> Z E1
            case 17: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.TIPO_OK;
            //Z -> R Z1
            case 18: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.TIPO_OK;
            //R -> U R1
            case 19: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.TIPO_OK;
            //U -> V U1
            case 20: if(data[1].getType() == Type.TIPO_OK) return data[0].getType();
                    else if(data[0].getType() != Type.ENTERO){
                        System.err.println("The parameters must be integers");
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;

            //V -> id V1
            case 21: return tsl.getType((Integer)data[0].getToken().getMod());
            //V -> constEntera
            case 22: return Type.ENTERO;
            //V -> cadena
            case 23: return Type.CADENA;

            //V1 -> ( V2 )
            case 24: return data[1].getType();
            //V2 -> L
            case 25: return data[0].getType();
            //S1 -> = E ;
            case 26: return data[1].getType();
            //S1 -> ( L ) ;
            case 27: return data[1].getType();

            //L -> E Q
            case 28: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.ERROR;
            //L -> lambda
            case 29: return Type.VACIO;
            
            //Q -> , E Q
            case 30: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.ERROR;
            //Q -> lambda
            case 31: return Type.VACIO;

            //X -> E
            case 32: return data[0].getType();
            //X -> lambda
            case 33: return Type.VACIO;

            //C -> B C
            case 34: if(data[0].getType() == data[1].getType()) return data[0].getType();
                    else return Type.ERROR;
            //C -> lambda
            case 35: return Type.VACIO;

            //H -> T
            case 36: return data[0].getType();
            //H -> lambda
            case 37: return Type.VACIO;

            //A -> T id K
            case 38: zonaDeclaracion = true;
                    //TODO instertar la variable en la tabla de simbolos 
                    //TODO modificar el desplazamiento
                    zonaDeclaracion = false;
            //A -> lambda
            case 39: return Type.TIPO_OK;
            //A -> void
            case 40: return Type.VACIO;

            //K -> , T id K
            case 41: zonaDeclaracion = true;
                    //TODO instertar la variable en la tabla de simbolos 
                    //TODO modificar el desplazamiento
                    zonaDeclaracion = false;
            //K -> lambda
            case 42: return Type.TIPO_OK;

            //T -> int
            case 43: return Type.ENTERO;
            //T -> boolean
            case 44: return Type.LOGICO;
            //T -> string
            case 45: return Type.CADENA;
            //T -> void
            case 46: return Type.VACIO;

            //E1 -> && Z E1
            case 47: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.LOGICO){
                        System.err.println("a boolean was expected");
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;
            //E1 -> lambda
            case 48: return Type.TIPO_OK;

            //Z1 -> < R Z1
            case 49: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.ENTERO){
                        System.err.println("a boolean was expected");
                        return Type.ERROR;
                    }
                    else return Type.ENTERO;
            //Z1 -> lambda
            case 50: return Type.TIPO_OK;

            //R1 -> * U R1
            case 51: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.LOGICO){
                        System.err.println("a boolean was expected");
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;
            //R1 -> lambda
            case 52: return Type.TIPO_OK;

            //U1 -> -= V U1
            case 53: if(data[2].getType() == Type.TIPO_OK) return data[1].getType();
                    else if(data[1].getType() != Type.LOGICO){
                        System.err.println("a boolean was expected");
                        return Type.ERROR;
                    }
                    else return Type.LOGICO;
            //U1 -> lambda
            case 54: return Type.TIPO_OK;

            default: System.out.println("internal semantic flaw");
            return Type.ERROR;
        }
    }
}
