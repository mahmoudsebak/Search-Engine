public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread queryProcessor = new Thread();
        Thread dataProcessing = new Thread();
        dataProcessing.start();
        
        
        dataProcessing.join();



    }   
}