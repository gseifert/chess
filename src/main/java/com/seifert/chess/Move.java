/*
 * Move.java
 *
 * Created on March 16, 2004, 1:12 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.io.*;
public class Move implements Serializable {
    private Square startSquare = null, endSquare = null;
    private ChessPiece capturedPiece = null;
    private boolean promotion = false;
    private Square enPassant = null;
        
    /** Creates a new move with the given parameters */
    Move(Square startSquare, Square endSquare, ChessPiece capturedPiece) {
        this.startSquare = startSquare;
        this.endSquare = endSquare;        
        this.capturedPiece = capturedPiece;
    }
    
    /** Returns the start square of the move */
    public Square getStartSquare() {
        return startSquare;
    }
    
    /** Returns the end square of the move */
    public Square getEndSquare() {
        return endSquare;
    }
    
    /** Returns the captured piece of the move */
    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }
    
    public boolean isPromotion() {
		return promotion;
	}

	public void setPromotion(boolean promotion) {
		this.promotion = promotion;
	}

	public Square getEnPassant() {
		return enPassant;
	}

	public void setEnPassant(Square enPassant) {
		this.enPassant = enPassant;
	}
}
