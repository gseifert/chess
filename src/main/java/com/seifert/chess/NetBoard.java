/*
 * NetBoard.java
 *
 * Created on March 16, 2004, 12:25 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
public abstract class NetBoard extends Board {
    
    private int localPlayer;
    private int remotePlayer;    
    private Socket messageSocket = null;
    private PrintWriter messageOut = null;    
    private BufferedReader messageIn = null;
    private Socket socket = null;    
    private PrintWriter out = null;    
    private BufferedReader in = null;
    private ChessProtocol cp = null;            
    
    public NetBoard(String name1, String name2) {
        super(name1, name2);        
    }
    
    /** Gets the local player */
    public int getLocalPlayer() {
        return localPlayer;
    }
    
    /** Sets the local player */
    public void setLocalPlayer(int player) {
        this.localPlayer = player;
    }
    
    /** Gets the remote player */
    public int getRemotePlayer() {
        return remotePlayer;
    }
    
    /** Sets the remote player */
    public void setRemotePlayer(int player) {
        this.remotePlayer = player;
    }
    
    /** Closes the socket connection
     *  @param remote true if the connection was closed by the remote player
     */
    public abstract void closeConnection(boolean remote);
        
    /** Gets the message socket object */
    public Socket getMessageSocket() {
        return messageSocket;
    }
    
    /** Sets the message socket object */
    public void setMessageSocket(Socket messageSocket) {
        this.messageSocket = messageSocket;
    }

    /** Gets the print writer associated with the message socket's output stream */
    public PrintWriter getMessageOut() {
        return messageOut;
    }
    
    /** Sets the print writer associated with the message socket's output stream */
    public void setMessageOut(PrintWriter messageOut) {
        this.messageOut = messageOut;
    }
    
    /** Gets the buffered reader associated with the message socket's input stream */
    public BufferedReader getMessageIn() {
        return messageIn;
    }
    
    /** Sets the buffered reader associated with the message socket's input stream */
    public void setMessageIn(BufferedReader messageIn) {
        this.messageIn = messageIn;
    }
    
    /** Gets the socket object */
    public Socket getSocket() {
        return socket;
    }
    
    /** Sets the socket object */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }    
    
    /** Gets the print writer associated with the socket's output stream */
    public PrintWriter getOut() {
        return out;
    }
    
    /** Sets the print writer associated with the socket's output stream */
    public void setOut(PrintWriter out) {
        this.out = out;
    }
    
    /** Gets the buffered reader associated with the socket's input stream */
    public BufferedReader getIn() {
        return in;
    }
    
    /** Sets the buffered reader associated with the socket's input stream */
    public void setIn(BufferedReader in) {
        this.in = in;
    }
    
    /** Gets the chess protocol object */
    public ChessProtocol getChessProtocol() {
        return cp;
    }
    
    /** Sets the chess protocol object */
    public void setChessProtocol(ChessProtocol cp) {
        this.cp = cp;
    }
    
    /** Kicks off thread to listen for remote player's move */
    public void listenForMove() {        
        Runnable r = new Runnable() {
            public void run() {
                handleMove();
            }
        };
        Thread t = new Thread(r, "ListenMove");
        t.start();
    }
    
    private void handleMove() {
        String input;
        try {
            while ((input = in.readLine()) != null) {
                cp.parseMessage(input, this);
                break;
            }        
            if (input == null) {
                closeConnection(true);
            }
        }
        catch (Exception e) {
            String msg = "Error handling remote move: " + e;
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), msg, "Error Handling Move", JOptionPane.ERROR_MESSAGE);
            Chess.logger.warning(msg);
        }
    }
    
    /** Kicks off thread to listen for message */
    public void listenForMessage() {        
        Runnable r = new Runnable() {
            public void run() {
                handleMessage();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
    
    /** Handles the incoming message */    
    private void handleMessage() {
        String input;
        try {
            while ((input = messageIn.readLine()) != null) {
                // post message to screen
                ((Chess)this.getTopLevelAncestor()).insertMessage(input);
            }        
            if (input == null) {
                closeMessageConnection();
            }
        }
        catch (Exception e) {            
            Chess.logger.warning("Error handling message: " + e);
            closeMessageConnection();
        }
    }
    
    /** Closes the message socket connection */
    public void closeMessageConnection() {
        try {               
            if (getMessageOut() != null) {
                getMessageOut().close();            
                setMessageOut(null);
            }            
            if (getMessageIn() != null) {
                getMessageIn().close();
                setMessageIn(null);
            }            
            if (getMessageSocket() != null) {
                getMessageSocket().close();                                
                setMessageSocket(null);
                Chess.logger.info("Message Connection closed.");
            }                                    
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Error closing message connection: " + ioe, "Error closing message connection", JOptionPane.ERROR_MESSAGE);
        }
    }    
}