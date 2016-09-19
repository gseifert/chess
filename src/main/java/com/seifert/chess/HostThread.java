/*
 * HostThread.java
 *
 * Created on March 12, 2004, 10:39 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.net.*;
import java.io.*;
import javax.swing.*;
public class HostThread extends Thread {
    private HostBoard hostBoard = null;
    private String name = null;
    private URL hostURL = null;
    private String hostMessage = null;
    private boolean hostSuccessful = false;    
    private Board currentBoard = null;

    HostThread(String name, URL hostURL, Board currentBoard) {
        this.name = name;
        this.hostURL = hostURL;        
        this.currentBoard = currentBoard;
    }

    public void run() {
        try {
            hostBoard = new HostBoard(name, hostURL);
            // adjust the board if necessary
            if (currentBoard != null) {
                while (hostBoard.getPosition() != currentBoard.getPosition()) {
                    hostBoard.rotate();
                }
                if (hostBoard.getWhoseMove() != currentBoard.getWhoseMove()) {
                    hostBoard.switchWhoseMove();
                }
                for (int i = 0; i <= 7; i++) {
                    for (int j = 0; j<=7; j++) {                        
                        hostBoard.getSquare(i, j).setPiece(currentBoard.getSquare(i, j).getPiece());
                    }
                }                
            }
            hostBoard.host();            
            hostSuccessful = true;            
            currentBoard = null;
        }
        catch (IOException ioe) {                            
            hostMessage = ioe.getMessage();                        
            // restore the current board
            if (currentBoard != null) {
                for (int i = 0; i <= 7; i++) {
                    for (int j = 0; j<=7; j++) {                        
                        currentBoard.getSquare(i, j).setPiece(hostBoard.getSquare(i, j).getPiece());
                    }
                }
            }
        }   
        closeWaitDialog();
    }       
    
    private void closeWaitDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Chess.currentWaitDialog.dispose();
            }
        });
    }

    public HostBoard getHostBoard() {
        return hostBoard;
    }

    public boolean wasHostSuccessful() {
        return hostSuccessful;
    }

    public String getHostMessage() {
        return hostMessage;
    }
}