import java.util.Random;
import java.util.concurrent.*;

public class Client extends Thread
{
    private BlockingQueue messageQueue;
    private Server server;
    private int clientID, numIterations, minThinking, maxThinking, minEating, 
        maxEating;
    private int[][] timingInformation; 
    
    /**
     * Basic constructor for the Client class. Requires an associated server
     * from which to request resources, and an ID number to identify it.
     */
    public Client(Server server, int clientID, int numIterations, 
        int minThinking, int maxThinking, int minEating, int maxEating)
    {
        messageQueue = new ArrayBlockingQueue(1);
        
        this.server = server;
        this.clientID = clientID;
        this.numIterations = numIterations;
        this.minThinking = minThinking;
        this.maxThinking = maxThinking;
        this.minEating = minEating;
        this.maxEating = maxEating;
        
        timingInformation = new int[numIterations][3];
    }
    
    /** 
     * Runs the client for 'numIterations' iterations, where each iteration
     * consists of
     *     1) A thinking period of length in milliseconds drawn from the
     *        uniform distribution on '[minThinking, maxThinking]'. The client
     *        does nothing during this period.
     *     2) A hungry period, where the client requests the token from
     *        'server' and does nothing until it receives the token.
     *     3) An eating period of length in milliseconds drawn from the uniform
     *        distribution on '[minEating, maxEating]'. The client does nothing
     *        during this period.
     *     4) Finally, the client relinquishes the token to the server.
     * After all the iterations have finished, the client sends a message to the
     * server informing it that the client is done.
     */
    public void run()
    {
        Random generator = new Random();
        
        for(int i = 0; i < numIterations; i++)
        {
            try {
                long startThinking, endThinking, startHungry, endHungry, 
                    startEating, endEating;
                
                /* Thinking. */
                startThinking = System.currentTimeMillis();
                int thinkingTime = minThinking + 
                    generator.nextInt(maxThinking - minThinking + 1);
                Thread.sleep(thinkingTime);
                endThinking = System.currentTimeMillis();
                    
                
                /* Hungry. */
                startHungry = System.currentTimeMillis();
                sendMessage(new Message(this, "request"));
                Token t = (Token) messageQueue.take();
                endHungry = System.currentTimeMillis();
                
                /* Eating. */
                startEating = System.currentTimeMillis();
                int eatingTime = minEating + 
                    generator.nextInt(maxEating - minEating + 1);
                Thread.sleep(eatingTime);
                endEating = System.currentTimeMillis();
                    
                /* Record how long each step took. */
                timingInformation[i][0] = (int)(endThinking - startThinking);
                timingInformation[i][1] = (int)(endHungry - startHungry);
                timingInformation[i][2] = (int)(endEating - startEating);
                
                /* Relinquish token. */
                sendToken(t);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        /* Tells the server that this client is finished with computation. */
        sendMessage(new Message(this, "done"));
    }
    
    /**
     * Returns the ID number of this client.
     */
    public int getID()
    {
        return clientID;
    }
    
    /**
     * Appends the received Token 't' to the tail of 'messageQueue'. 
     */
    public void receiveToken(Token t)
    {
        try {
            messageQueue.put(t);
            System.out.println("Client " + clientID + " has received a token!");
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends the Token 't' to the associated server.
     */
    private void sendToken(Token t)
    {
        System.out.println("Client " + clientID + " has sent a token!");
        server.receiveToken(t);
    }
    
    /**
     * Sends the Message 'm' to the associated server.
     */
    private void sendMessage(Message m)
    {
        System.out.println("Client " + clientID + " has sent a " + 
                           m.getType() + " message!");
        server.receiveMessage(m);
    }
    
    /**
     * Returns the average time, in milliseconds, that this Client has spent in
     * each of the "thinking", "hungry", and "eating" states. This method should
     * only be invoked if the Client has finished running, and the array it
     * returns is such that
     *     - averageTimes[0] is the average time the Client spent in the
     *       "thinking" state,
     *     - averageTimes[1] is the average time the Client spent in the
     *       "hungry" state, and
     *     - averageTimes[2] is the average time the Client spent in the
     *       "eating" state.
     */
    public int[] getAverageTimes()
    {
        int[] averageTimes = new int[3];
        averageTimes[0] = 0;
        averageTimes[1] = 0;
        averageTimes[2] = 0;
        
        for(int i = 0; i < numIterations; i++)
        {
            averageTimes[0] += timingInformation[i][0];
            averageTimes[1] += timingInformation[i][1];
            averageTimes[2] += timingInformation[i][2];
        }
        
        averageTimes[0] /= numIterations;
        averageTimes[1] /= numIterations;
        averageTimes[2] /= numIterations;
        
        return averageTimes;
    }
}