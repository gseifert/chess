/*
 * ChessPiece.java
 *
 * Created on February 24, 2004, 10:19 AM
 */

package com.seifert.chess;

/**
 *
 * @author  ZZ3JPZ
 */
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
public class ChessPiece extends JLabel {
    
    protected static Color colorPlayer1 = null;
    protected static Color colorPlayer2 = null;    
    protected static final int EMPTY = 0;
    protected static final int PAWN = 1;
    protected static final int KNIGHT = 2;
    protected static final int BISHOP = 3;
    protected static final int ROOK = 4;
    protected static final int QUEEN = 5;
    protected static final int KING = 6;
    
    private String iconFile;
    private String iconFileCaptured;
    private String iconDesc;    
    private int type;                   // the type of the piece
    private Color color;                // the color of the piece
    private int numMoves;               // the number of moves the piece has made
    private boolean captured = false;   // whether or not the piece is captured   
    
    /** Creates a new instance of ChessPiece */
    public ChessPiece(Color color, int type) {
        super("", JLabel.CENTER);
        this.color = color; 
        this.type = type;        
    }    

    /** Returns the type of the piece as an integer */
    public int getType() {
        return type;
    }
    
    /** Returns the type of the piece as a String */
    public String getTypeDesc() {
        switch(type) {
            case EMPTY:
                return "Empty";
            case PAWN:
                return "Pawn";
            case KNIGHT:
                return "Knight";
            case BISHOP:
                return "Bishop";
            case ROOK:
                return "Rook";
            case QUEEN:
                return "Queen";
            case KING:
                return "King";
            default:
                return null;
        }
    }
    
    /** Returns the color of the piece */
    public Color getColor() {
        return color;
    }
    
    /** Returns the number of moves this piece has made */
    public int getNumMoves() {
        return numMoves;
    }
    
    /** Adds one to the number of moves this piece has made */
    public void incrementNumMoves() {
        numMoves++;
    }
    
    /** Subtracts one from the number of moves this piece has made */
    public void decrementNumMoves() {
        numMoves--;
    }

    /** Sets whether or not this piece is currently captured */
    public void setCaptured(boolean isCaptured) {
        captured = isCaptured;
        // update the piece's icon
        this.setPieceIcon();
    }
    
    /** Returns true if this piece is currently captured, false if its not */
    public boolean isCaptured() {
        return captured;
    }
    
    /** Sets the strings used to create the icon for the piece.
     *  @param wIconFile the image file to be used for a "white" piece on the board
     *  @param wIconFileCaptured the image file to be used for a "white" piece that has been captured
     *  @param wIconDesc the description of the "white" image files
     *  @param bIconFile the image file to be used for a "black" piece on the board
     *  @param bIconFileCaptured the image file to be used for a "black" piece that has been captured
     *  @param bIconDesc the description of the "black" image files
     */
    public void setIconStrings(String iconFile, String iconFileCaptured, String iconDesc) {
        this.iconFile = iconFile;
        this.iconFileCaptured = iconFileCaptured;
        this.iconDesc = iconDesc;
        this.setPieceIcon();
    }
    
    /** Sets the piece's icon appropriately depending on its color and whether or 
     *  not it has been captured.
     */
    private void setPieceIcon() {
        ImageIcon imgIcon = null;
        if (captured) {
            imgIcon = Chess.createImageIcon(iconFileCaptured, iconDesc);            
        }
        else {
            imgIcon = Chess.createImageIcon(iconFile, iconDesc);            
        }
                
        Image img = imgIcon.getImage();        
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        //build icm and buffered image
        final int NUM_BITS_PER_PIXEL = 8;
        final int NUM_COLORS = 256;        
        final int TRANSPARENT_COLOR = 16;
        byte[] r = new byte[NUM_COLORS];
        byte[] g = new byte[NUM_COLORS];
        byte[] b = new byte[NUM_COLORS];                        
        r[0] = (byte)TRANSPARENT_COLOR;
        g[0] = (byte)TRANSPARENT_COLOR;
        b[0] = (byte)TRANSPARENT_COLOR;
        if (color.equals(ChessPiece.colorPlayer1)) {        
            r[1] = (byte)ChessPiece.colorPlayer1.getRed();
            g[1] = (byte)ChessPiece.colorPlayer1.getGreen();
            b[1] = (byte)ChessPiece.colorPlayer1.getBlue();                
        }
        else {
            r[1] = (byte)ChessPiece.colorPlayer2.getRed();
            g[1] = (byte)ChessPiece.colorPlayer2.getGreen();
            b[1] = (byte)ChessPiece.colorPlayer2.getBlue();            
        }   
        for (int i = 2; i < NUM_COLORS; i++) {
            r[i] = (byte)0;
            g[i] = (byte)0;
            b[i] = (byte)0; 
        }   
        // create the indexcolormodel with the transparent color set to index 0
        IndexColorModel icm = new IndexColorModel(NUM_BITS_PER_PIXEL, NUM_COLORS, r, g, b, 0);        
        // create a blank buffered image with the indexcolormodel
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, icm);        
        // draw the piece's image onto the buffered image
        newImg.createGraphics().drawImage(img, 0, 0, null);
        imgIcon.setImage(newImg);
        this.setIcon(imgIcon);
    }
    
    /** This method checks to see whether or not the move would put the player 
     *  in check.
     *  @param endSquare the square the piece is trying to move to
     *  @return true if this move would put the player in check
     */
    public boolean isCheck(Square endSquare) {     
        // get the moving piece's starting square
        Square startSquare = (Square)this.getParent();
        // get the piece on the square that the piece is trying to move to
        ChessPiece targetPiece = endSquare.getPiece();
        // set the moving piece on the ending square
        endSquare.setPiece(this);
        // set a blank piece on the starting square        
        startSquare.setPiece(new ChessPiece(null, ChessPiece.EMPTY));

        // check if this move would put the player into check
        boolean blnReturn;
        Board board = (Board)endSquare.getParent().getParent();
        int player = color.equals(ChessPiece.colorPlayer1) ? Board.PLAYER_1 : Board.PLAYER_2;
        if (board.check(player, false) == Board.NO_CHECK) {
            blnReturn = false;
        }
        else {
            blnReturn = true;
        }

        // return the pieces to their proper position
        startSquare.setPiece(this);
        endSquare.setPiece(targetPiece);        
        return blnReturn;
    }
    
    /** To be implemented by subclasses according to the specific piece's capabilities.
     *  Should return true if the piece can move to the given square from its
     *  present location.
     *  @param endSquare the square the piece is trying to move to
     *  @param check true if this method should check if the move would put the player in check
     *  @return true if the piece can move to the given square of the board
     */
    public boolean isValidMove(Square endSquare, boolean check) {        
        return false;        
    }
}
