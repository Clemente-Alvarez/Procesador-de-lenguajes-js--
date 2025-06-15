
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
    ts ts;

    
    

    public AnalizadorSemantio(ts tablaSim){
        ts = tablaSim;
        zonaDeclaracion  = false;
    }

    public Type compute(StackType[] data, int rule){
        return Type.TIPO_OK;
    }
}
