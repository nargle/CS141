import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * The Server class creates N clients, each in a separate thread.
 * Clients request the token from the server and notify the server
 * when they have terminated. If the token is available, the server
 * passes the token to the client, which holds on to it for a finite
 * amount of time. If the token is not currently held by the server,
 * a token will be passed to the client that requested it when it 
 * becomes available. Priority is given to clients who requested
 * tokens sooner.
 */
public class Server implements Runnable
{
    /**
     * Parameters of the system.
     *     - NUM_CLIENTS is the number of Clients the server creates.
     *     - NUM_ITERATIONS is the number of thinking-hungry-eating cycles each
     *       Client runs for.
     *     - MIN_THINKING is the minimum amount of time each Client will spend in
     *       the "thinking" state, in milliseconds, before switching to the
     *       "hungry" state.
     *     - MAX_THINKING is the maximum amount of time each Client will spend in
     *       the "thinking" state, in milliseconds, before switching to the
     *       "hungry" state.
     *     - MIN_EATING is the minimum amount of time each Client will spend in
     *       the "eating" state, in milliseconds, before switching to the
     *       "thinking" state.
     *     - MAX_EATING is the maximum amount of time each Client will spend in
     *       the "thinking" state, in milliseconds, before switching to the
     *       "thinking" state.
     */
    public static final int NUM_CLIENTS = 10;
    public static final int NUM_ITERATIONS = 100;
    public static final int MIN_THINKING = 195;
    public static final int MAX_THINKING = 205;
    public static final int MIN_EATING = 15;
    public static final int MAX_EATING = 25;
    
    /** The messageQueue keeps track of the request and termination
     * messages received by the server from clients, and the tokenQueue
     * is empty if the server doesn't hold the token and has a Token
     * object if it does.
     */
    private BlockingQueue messageQueue, tokenQueue;
    /**
     * The numClients variable defines the number of clients that
     * run concurrently in the program.
     */
    private int numClients;
    /**
     * The ArrayList clients keeps track of the currently running
     * clients. When a client terminates, it notifies the server,
     * which deletes it from the clients list.
     */
    private ArrayList<Client> clients;
    /**
     * The averageClientTimes matrix records the average time each Client spent
     * in each of the "thinking", "hungry", and "eating" states as each Client
     * terminates.
     */
    private int[][] averageClientTimes;
    
    /**
     * The constructor the the Server defines the number of clients
     * and initializes its two queues. Additionally, the token is
     * put into the tokenQueue, as the server starts with it.
     */
    public Server()
    {
        // Number of clients
        numClients = NUM_CLIENTS;
        messageQueue = new ArrayBlockingQueue(numClients);
        tokenQueue = new ArrayBlockingQueue(1);        
        try {
            tokenQueue.put(new Token());
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        clients = new ArrayList();
        averageClientTimes = new int[numClients][3];
        System.out.println("Server initialized");
    }
    
    /**
     * In the run function, the client threads are created and
     * the token waits for messages.
     */
    public void run()
    {
        System.out.println("Server has started running.");
        // Create clients and threads
        for(int i = 0; i < numClients; i++)
        {
            Client client = new Client(this, i, NUM_ITERATIONS, MIN_THINKING,
                MAX_THINKING, MIN_EATING, MAX_EATING);
            clients.add(client);
            client.start();
        }
        System.out.println(numClients + " clients created");
        // Wait until all clients are terminated
        while(clients.size() > 0)
            waitForMessage();
        System.out.println("All clients terminated");
        
        System.out.println("Average Times (Thinking, Hungry, Eating):");
        for(int i = 0; i < numClients; i++)
        {
            System.out.println("Client " + i + ": (" + 
                       averageClientTimes[i][0] + 
                ", " + averageClientTimes[i][1] + 
                ", " + averageClientTimes[i][2] + ")");
        }
    }
    
    /**
     * The server waits for one message, and it gets handled
     * when it arrives.
     */
    private void waitForMessage()
    {
        try
        {
            handleMessage((Message) messageQueue.take());
        }
        catch(InterruptedException ex) {}
    }
    
    /**
     * The server waits for one token to give to the client
     * that requested it, which is passed in through the 
     * requester argument of the function.
     */
    private void waitForToken(Client requester)
    {
        try
        {
            requester.receiveToken((Token) tokenQueue.take());
        }
        catch(InterruptedException ex) {}
    }
    
    /**
     * The server handles Message m, which is either a request for
     * a token or a notification that a client has terminated. If
     * the message is a request, then a token is given to the client
     * as soon as it becomes available. Otherwise, if the message is
     * a termination message, the client is removed from the server's
     * client list and set to null so the thread will terminate.
     */
    private void handleMessage(Message m)
    {
        if(m.getType().equals("request")) // Message is a request
        {
            System.out.println("Server has received Client " + 
                               m.getClientID() + "'s message!");
            waitForToken(m.getClient());
        }
        else if(m.getType().equals("done")) // Message is termination
        {
            System.out.println("Client " + m.getClientID() + 
                               " terminated, removed from list");
            averageClientTimes[m.getClientID()] = 
                (m.getClient()).getAverageTimes();
            clients.remove(m.getClient());
        }
    }
    
    /**
     * When the server receives a message, it is put into the
     * messageQueue.
     */
    public void receiveMessage(Message m)
    {
        try
        {
            messageQueue.put(m);
        }
        catch(InterruptedException ex) {}
    }
    
    /**
     * When the server receives a token, it gets put into the
     * tokenQueue.
     */
    public void receiveToken(Token t)
    {
        try
        {
            tokenQueue.put(t);
        }
        catch(InterruptedException ex) {}
    }
}
