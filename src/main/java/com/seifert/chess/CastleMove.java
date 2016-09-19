/*
 * CastleMove.java
 *
 * Created on March 22, 2004, 12:12 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.io.*;
public class CastleMove implements Serializable {
    private Square startKingSquare = null, endKingSquare = null;    
    private Square startRookSquare = null, endRookSquare = null;
    private int iDirection;
    
    /** Creates a new castle move with the given parameters */
    CastleMove(Square startKingSquare, Square endKingSquare, Square startRookSquare, Square endRookSquare, int iDirection) {
        this.startKingSquare = startKingSquare;
        this.endKingSquare = endKingSquare;
        this.startRookSquare = startRookSquare;
        this.endRookSquare = endRookSquare;        
        this.iDirection = iDirection;
    }

    /** Returns the start king square of the castle */
    public Square getStartKingSquare() {
        return startKingSquare;
    }
    
    /** Returns the end king square of the castle */
    public Square getEndKingSquare() {
        return endKingSquare;
    }
    
    /** Returns the start rook square of the castle */
    public Square getStartRookSquare() {
        return startRookSquare;
    }
    
    /** Returns the end rook square of the castle */
    public Square getEndRookSquare() {
        return endRookSquare;
    }
    
    /** Returns the direction of the castle */
    public int getDirection() {
        return iDirection;
    }
    
    /** Returns the direction of the castle as a string */
    public String getDirectionDesc() {
        switch (iDirection) {
            case King.CASTLE_KINGSIDE:
                return "kingside";
            case King.CASTLE_QUEENSIDE:
                return "queenside";
            default:
                return null;
        }
    }
}