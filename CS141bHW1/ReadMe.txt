Homework 1 Submission Information (CS 141b, January 2012)

Group Members: Daniel Chen, Jocelyn (Josie) Kishi, and Kevin Kowalski

There are 5 java files in this program: Main.java, Client.java, Server.java, 
Message.java, and Token.java. (The class files have also been included in the
submission). The main function to start the program (create the server thread)
is in the Main class.

From a single run of the program with parameters
    NUM_CLIENTS = 10
    NUM_ITERATIONS = 100
    MIN_THINKING = 195
    MAX_THINKING = 205
    MIN_EATING = 15
    MAX_EATING = 25,
we obtained the following timing information for the clients:
          Average Times (in milliseconds)
          Thinking     Hungry     Eating
Client 0:      199          4         20
Client 1:      200          6         20
Client 2:      200          6         20
Client 3:      200          4         20
Client 4:      199          6         19
Client 5:      199          5         19
Client 6:      200          5         19
Client 7:      200          5         20
Client 8:      200          5         20
Client 9:      200          6         19

These hungry times seem pretty reasonable. Since the thinking time is much
greater than the eating time and both have small variance, after the initial
couple of iterations the clients settle into a pattern where the token becomes
available more or less as soon as the next client transitions from thinking to
hungry. We would expect this pattern to hold as long as
     average eating time * num clients <= average thinking time.
     
In support of this hypothesis, we present the results of another run where
NUM_CLIENTS = 20:
           Average Times (in milliseconds)
           Thinking     Hungry     Eating
Client  0:      200        185         20
Client  1:      200        187         20
Client  2:      200        184         20
Client  3:      199        186         20
Client  4:      200        186         19
Client  5:      200        186         20
Client  6:      200        189         19
Client  7:      199        189         20
Client  8:      200        186         20
Client  9:      200        184         20
Client 10:      200        187         19
Client 11:      200        187         20
Client 12:      199        186         20
Client 13:      200        185         20
Client 14:      199        188         19
Client 15:      200        185         20
Client 16:      199        184         21
Client 17:      201        187         20
Client 18:      200        188         20
Client 19:      200        185         19

and another one where NUM_CLIENTS = 1:
          Average Times (in milliseconds)
          Thinking     Hungry     Eating
Client 0:      200        0           20