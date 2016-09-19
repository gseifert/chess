/*
 * HostBoard.java
 *
 * Created on March 10, 2004, 12:59 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.net.*;
import java.io.*;
import javax.swing.*;
public class HostBoard extends NetBoard {
        
    protected static final int DEFAULT_HOST_PORT = 4444;    
    
    protected final int LOCAL_PLAYER = PLAYER_1;
    protected final int REMOTE_PLAYER = PLAYER_2;
    private URL hostURL;    
    private ServerSocket serverSocket = null;
    
    /** Creates a new instance of HostBoard 
     *  @param name the name of hosting player (player 1)
     *  @param hostURL the localhost url
     */    
    public HostBoard(String name, URL hostURL) throws IOException {
        super(name, null);
        this.hostURL = hostURL;
        this.setLocalPlayer(Board.PLAYER_1);
        this.setRemotePlayer(Board.PLAYER_2);
    }
    
    /** Gets the host url */
    public URL getHostURL() {
        return hostURL;
    }
    
    /** Gets the server socket to allow another thread to cancel the accept call */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }
    
    /** Sets up a board to host */
    public void host() throws IOException {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        ChessProtocol cp;
        
        try {
            serverSocket = new ServerSocket(hostURL.getPort());
        }
        catch (IOException ie) {
            throw new HostException("Unable to host board on port " + hostURL.getPort()); 
        }
        
        try {
            socket = serverSocket.accept();
            setSocket(socket);            
        }
        catch (IOException ie) {
            throw new HostException("Client connection failed on port " + hostURL.getPort());            
        }
        
        try {    
            // connect to the Message Server
            Socket messageSocket = new Socket(InetAddress.getByName("localhost"), MessageServer.DEFAULT_MESSAGE_PORT);
            setMessageSocket(messageSocket);
            PrintWriter messageOut = new PrintWriter(messageSocket.getOutputStream(), true);
            setMessageOut(messageOut);    
            BufferedReader messageIn = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
            setMessageIn(messageIn);
            listenForMessage();
            
            out = new PrintWriter(getSocket().getOutputStream(), true);
            setOut(out);
            in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            setIn(in);
            cp = new ChessProtocol();
            setChessProtocol(cp);
            String input;        
            // read in the remote player's name                
            cp.parseMessage(in.readLine(), this);        
            // send the local player's name and the board
            out.println(cp.createMessage(this));
            cp.initMoveNumber();
            if (getWhoseMove() == getRemotePlayer()) {
                // wait for first move
                listenForMove();
            }
        }
        catch (Exception e) {
            throw new HostException(e.toString());
        }
    }
    
    /** Closes the socket connection
     *  @param remote true if the connection was closed by the remote player
     */
    public void closeConnection(boolean remote) {
        try {            
            if (getOut() != null) {
                getOut().close();            
                setOut(null);
            }            
            if (getIn() != null) {
                getIn().close();
                setIn(null);
            }            
            if (getSocket() != null) {
                getSocket().close();    
                setSocket(null);
            }
            if (serverSocket != null) {
                serverSocket.close();                
                serverSocket = null;
                Chess.logger.info("Connection closed.");
            }
            if (remote) {
                String msg = "Connection closed by remote player.";
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(), msg, "Close Connection", JOptionPane.ERROR_MESSAGE);
            }                        
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Error closing connection: " + ioe, "Error closing connection", JOptionPane.ERROR_MESSAGE);            
        }
    }
    
    class HostException extends IOException {
        HostException(String msg) {
            super(msg);
        }
    }
}