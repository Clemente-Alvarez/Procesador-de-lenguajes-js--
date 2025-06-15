public class Token<T> {
    
    private String name;
    private T mod;

    public Token(String name, T mod){
        this.name = name;
        this.mod =  mod;
    }

    public Token(String name){
        this.name = name;
        mod = null;
    }

    public String getName(){
        return name;
    }

    public T getMod(){
        return mod;
    }

    public String toString(){
        if(mod instanceof Integer) return "<" + name + ", " + (Integer)mod + ">";
        else return "<" + name + ", " + (String)mod + ">";
    }
}
