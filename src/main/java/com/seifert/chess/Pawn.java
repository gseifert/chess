/*
 * Pawn.java
 *
 * Created on February 24, 2004, 12:02 PM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;
public class Pawn extends ChessPiece { 
    
    /** Creates a new instance of Pawn
     *  @param color the color of the new pawn     
     */
    public Pawn(Color color) {
        super(color, ChessPiece.PAWN);
        this.setIconStrings("images/wpawn.gif", "images/wpawn_captured.gif", "Pawn");        
    }
    
    /** Checks to see if the piece can legally move to the given square of the board
     *  from its present location.
     *  @param newSq the square of the board the piece is trying to move to
     *  @param check true if this method should check if the move would put the player in check
     *  @return true if the piece can move to the given square of the board
     */
    public boolean isValidMove(Square newSq, boolean check) {        
        // get the row and column of the square that the piece wants to move to
        int newRow = newSq.getRow();
        int newCol = newSq.getCol();
        // get the square that the piece is currently on
        Square sq = (Square)this.getParent();
        // get the current row and column of the square that the piece is on
        int currentRow = sq.getRow();
        int currentCol = sq.getCol();        
        // get the instance of the board
        Board board = (Board)sq.getParent().getParent();                
        
        // get the direction of the move
        int iDirectionRow, iDirectionCol;
        if (newRow > currentRow) {
            iDirectionRow = 1;
        }
        else if (newRow < currentRow) {
            iDirectionRow = -1;            
        }        
        else {
            iDirectionRow = 0;
        }        
        if (newCol > currentCol) {
            iDirectionCol = 1;
        }
        else if (newCol < currentCol) {
            iDirectionCol = -1;            
        }
        else {
            iDirectionCol = 0;
        }
        
        // if the board is layed out vertically, then a move forward changes the 
        // row while a move sideways changes the column 
        // (vice versa if the board is layed out horizontally)
        int newIndex1 = 0, currentIndex1 = 0, newIndex2 = 0, currentIndex2 = 0, iPosition = 0;
        switch (board.getPosition()) {
            case Board.POSITION_1:
                newIndex1 = newRow;
                currentIndex1 = currentRow;
                newIndex2 = newCol;
                currentIndex2 = currentCol;
                if (this.getColor().equals(ChessPiece.colorPlayer1)) {
                    iPosition = -1;
                }
                else {
                    iPosition = 1;
                }
                break;
            case Board.POSITION_2:
                newIndex1 = newCol;
                currentIndex1 = currentCol;
                newIndex2 = newRow;
                currentIndex2 = currentRow;
                if (this.getColor().equals(ChessPiece.colorPlayer2)) {
                    iPosition = -1;
                }
                else {
                    iPosition = 1;
                }
                break;
            case Board.POSITION_3:
                newIndex1 = newRow;
                currentIndex1 = currentRow;
                newIndex2 = newCol;
                currentIndex2 = currentCol;
                if (this.getColor().equals(ChessPiece.colorPlayer2)) {
                    iPosition = -1;
                }
                else {
                    iPosition = 1;
                }
                break;                                    
            case Board.POSITION_4:
                newIndex1 = newCol;
                currentIndex1 = currentCol;
                newIndex2 = newRow;
                currentIndex2 = currentRow;
                if (this.getColor().equals(ChessPiece.colorPlayer1)) {
                    iPosition = -1;
                }
                else {
                    iPosition = 1;
                }
                break;                
        }        
        // adjust the values for the position of the board
        newIndex1 *= iPosition;
        currentIndex1 *= iPosition;
        newIndex2 *= iPosition;
        currentIndex2 *= iPosition;
        
        // a pawn cannot move backwards
        if (newIndex1 <= currentIndex1) {
            return false;
        }        
        // a pawn can only move sideways 1 space if it is also moving foward 1 space and
        // it is taking a piece
        if (newIndex2 != currentIndex2) {
            if ((newIndex2 != currentIndex2 + 1) && (newIndex2 != currentIndex2 - 1)) {
                return false;
            }
            else if (newIndex1 != currentIndex1 + 1) {
                return false;
            }
            else if (newSq.getPiece().getType() == ChessPiece.EMPTY) {
                return false;
            }
        }        
        // the max a pawn can move forward is two spaces, and then only if
        // it hasn't already moved and both squares in front of it are empty
        if (newIndex1 > currentIndex1 + 2) {
            return false;
        }
        if (newIndex1 == currentIndex1 + 2) {
            if (getNumMoves() > 0) {
                return false;
            }
            else if (newSq.getPiece().getType() != ChessPiece.EMPTY) {
                return false;
            }            
            else if (board.getSquare(currentRow + (1 * iDirectionRow), currentCol + (1 * iDirectionCol)).getPiece().getType() != ChessPiece.EMPTY) {
                return false;
            }
        }
        // a pawn can only move foward 1 space if the square is empty
        if (newIndex2 == currentIndex2 && newSq.getPiece().getType() != ChessPiece.EMPTY) {
            return false;
        }        
        
        // if needed, check if this move would put the player into check
        if (check) {
            if (isCheck(newSq)) {
                return false;
            }
        }
        
        // if we get here, it is a valid move        
        return true;
    }
}
