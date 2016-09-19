/*
 * MessageServer.java
 *
 * Created on April 28, 2004, 9:20 AM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.net.*;
import java.io.*;
public class MessageServer {
        
    protected static final int DEFAULT_MESSAGE_PORT = 4750;    
    private ServerSocket messageServerSocket = null; 
    private Socket[] messageSocket;    
    private PrintWriter[] messageOut;
    private BufferedReader[] messageIn;    
    private int port;
    
    /** Creates a new instance of MessageServer
     *  @param port the port to listen on     
     */    
    public MessageServer(int port) throws IOException {                                
        try {
            this.port = port;
            messageServerSocket = new ServerSocket(port);
            messageSocket = new Socket[2];
            messageOut = new PrintWriter[2];
            messageIn = new BufferedReader[2];
        }
        catch (IOException ie) {
            throw new IOException("Unable to listen for messages on port " + port); 
        }
    }
    
    /** Returns the port the message server is listening on */
    public int getPort() {
        return port;
    }
    
    public void getClients() throws Exception {        
        for (int i = 0; i <= 1; i++) {
            messageSocket[i] = messageServerSocket.accept();                        
            messageOut[i] = new PrintWriter(messageSocket[i].getOutputStream(), true);
            messageIn[i] = new BufferedReader(new InputStreamReader(messageSocket[i].getInputStream()));
            listenForMessage(i);
        }
    }
    
    /** Kicks off thread to listen for message 
     *  @param i the index of the socket to listen for
     */
    public void listenForMessage(int i) {
        final int j = i;
        Runnable r = new Runnable() {
            public void run() {
                handleMessage(j);
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
    
    /** Handles the incoming message 
     *  @param i the index of the socket the message is from
     */    
    private void handleMessage(int i) {
        String input;
        try {
            while ((input = messageIn[i].readLine()) != null) {
                // send message to both clients
                for (int j = 0; j <= 1; j++) {
                    messageOut[j].println(input);
                }                
            }        
            if (input == null) {
                closeConnection();
            }
        }
        catch (Exception e) {            
            Chess.logger.warning("Error handling message at message server: " + e);
            closeConnection();
        }
    }    
    
    /** Closes the socket connection */
    public void closeConnection() {
        try {                       
            for (int i = 0; i <= 1; i++) {
                if (messageOut[i] != null) {
                    messageOut[i].close();            
                    messageOut[i] = null;
                }                    
                if (messageIn[i] != null) {
                    messageIn[i].close();
                    messageIn[i] = null;
                }
                if (messageSocket[i] != null) {
                    messageSocket[i].close();                
                    messageSocket[i] = null;
                }
            }
            if (messageServerSocket != null) {
                messageServerSocket.close();                
                messageServerSocket = null;
                Chess.logger.info("Message Server closed.");
            }                        
        }
        catch (IOException ioe) {
            Chess.logger.warning("Error closing Message Server: " + ioe);
        }
    }
}