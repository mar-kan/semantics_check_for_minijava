class test68{
    public static void main(String[] a){

        Test2 test;
        test = new Test2();
        //test = new Test();

        System.out.println(new Test().start());
    }
}

class Test2 {

    public Test2 start(){

        Test2 test22;
        test22=null;

        return test22;
    }
}

class Test {

    Test test;
    int i;

    public int start(){

        test = test.next();

        return 0;
    }

    public Test next() {

        Test2 test21;
        test21=null;

        return test;	// TE
    }
}