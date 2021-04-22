class Example1 {
    public static void main(String[] args) {
    }
}

class Aa{
    int i;
    boolean flag;
    int j;
    public int foo() {return j;}
    public boolean fa() {return flag;}
}

class Bb extends Aa{
    A type;
    int k;
    public int foo() {return k;}
    public boolean bla() {return true;}
}