/**
 * The Main class initiates the program execution 
 * by starting the server thread, in the main
 * function. Information about group members is in
 * the ReadMe.txt file.
 */
public class Main {
    /* Starts the server thread */
    public static void main(String[] args) {
        (new Thread(new Server())).start();
    }
}
