/*
 * RemoteBoard.java
 *
 * Created on March 10, 2004, 12:59 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import javax.swing.*;
import java.net.*;
import java.io.*;
public class RemoteBoard extends NetBoard {
        
    protected static final String DEFAULT_REMOTE_IP = "999.999.999.999";
    protected static final int DEFAULT_REMOTE_PORT = 4444;
    
    protected final int LOCAL_PLAYER = PLAYER_2;
    protected final int REMOTE_PLAYER = PLAYER_1;
    private URL remoteURL;
    //private URLConnection uc = null;    
    
    /** Creates a new instance of RemoteBoard 
     *  @param name the name of the connecting player
     *  @param remoteURL the url of the remote chess board
     */    
    public RemoteBoard(String name, URL remoteURL) throws IOException {
        super(null, name);
        this.remoteURL = remoteURL;
        this.setLocalPlayer(Board.PLAYER_2);
        this.setRemotePlayer(Board.PLAYER_1);
        this.connect();
    }
    
    /** Gets the url of the remote computer */
    public URL getRemoteURL() {
        return remoteURL;
    }
    
    private void connect() throws IOException {        
        Socket socket;        
        PrintWriter out;
        BufferedReader in;        
        ChessProtocol cp;
        
        try {
            socket = new Socket(InetAddress.getByName(remoteURL.getHost()), remoteURL.getPort());
            setSocket(socket);
            //uc = remoteURL.openConnection();                        
            //uc.setDoOutput(true);            
            out = new PrintWriter(socket.getOutputStream(), true);
            setOut(out);    
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            setIn(in);
        }
        catch (UnknownServiceException use) {
            throw new RemoteException("Couldn't get I/O on connection to: " + remoteURL);
        }
        catch (IOException e) {
            throw new RemoteException("Couldn't get connection to: " + remoteURL);
        }       
        
        try {
            // send the local player's name
            cp = new ChessProtocol();
            setChessProtocol(cp);
            out.println(cp.createMessage(this));
            // read in the remote player's name and the board
            cp.parseMessage(in.readLine(), this);
            cp.initMoveNumber();
            if (getWhoseMove() == getRemotePlayer()) {
                // wait for first move
                listenForMove();
            }
        }
        catch (Exception e) {
            throw new RemoteException(e.toString());
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
    
    class RemoteException extends IOException {
        RemoteException(String msg) {
            super(msg);
        }
    }
}