class Example1 {
    public static void main(String[] args) {
        boolean t;
        t = true;
        t = true && t;
        Aa a = new Aa();
    }
}

class Aa{
    int i;
    boolean flag;
    public int foo() {return i;}
    public boolean fa() {return flag;}
}

class Cc extends Aa{
    int l;
    public boolean bla() { return true; }
}