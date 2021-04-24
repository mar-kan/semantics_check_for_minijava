class Example1 {
    public static void main(String[] args) {
        int i;
        boolean t;
        t = true && false;
    }
}

class Aa{
    int i;
    boolean flag;
    int j;
    public int foo() {return i;}
    public boolean fa() {return flag;}
}

class Bb extends Aa{
    A type;
    int k;
    public int foo() {
        int i;
        int j;
        return k;
    }
    public boolean bla() {return true;}
}

class Cc extends Bb{
    int l;
    public boolean bla() { return true; }
}