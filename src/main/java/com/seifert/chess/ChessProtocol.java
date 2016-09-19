/*
 * ChessProtocol.java
 *
 * Created on March 11, 2004, 11:25 AM
 */

package com.seifert.chess;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author  ZZ3JPZ
 */
//import javax.xml.parsers.*;
//import org.xml.sax.*;
//import org.w3c.dom.*;
//import java.io.*;
public class ChessProtocol {
    
    private DocumentBuilder builder;
    private Document document;
    private int moveNumber = 0;
    
    /** Creates a new instance of ChessProtocol */
    public ChessProtocol() throws Exception {        
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            throw new Exception("Parser with specified options can't be built");            
        }
    }
    
    public void initMoveNumber() {
        moveNumber = 1;
    }
    
    public String createMessage(NetBoard board) {                
        document = builder.newDocument();
        Element root = document.createElement("Chess");
        Element moveNum = document.createElement("MoveNumber");
        moveNum.appendChild(document.createTextNode(String.valueOf(moveNumber)));
        root.appendChild(moveNum);
        if (moveNumber == 0) {            
            // send the local player's name
            Element name = document.createElement("Name");
            name.appendChild(document.createTextNode(board.getPlayerName(board.getLocalPlayer())));
            root.appendChild(name);
            // send the board from the host player
            if (board instanceof HostBoard) {
                Element brd = document.createElement("Board");
                Element position = document.createElement("Position");
                position.appendChild(document.createTextNode(String.valueOf(board.getPosition())));
                brd.appendChild(position);
                Element whoseMove = document.createElement("WhoseMove");
                whoseMove.appendChild(document.createTextNode(String.valueOf(board.getWhoseMove())));
                brd.appendChild(whoseMove);
                for (int i = 0; i <= 7; i++) {
                    for (int j = 0; j <= 7; j++) {
                        Element sq = document.createElement("Square");
                        Element row = document.createElement("Row");
                        row.appendChild(document.createTextNode(String.valueOf(i)));
                        Element col = document.createElement("Col");
                        col.appendChild(document.createTextNode(String.valueOf(j)));
                        Element piece = document.createElement("Piece");
                        ChessPiece cp = board.getSquare(i, j).getPiece();
                        if (cp.getType() == ChessPiece.EMPTY) {
                            piece.appendChild(document.createTextNode("0"));
                        }
                        else {
                            piece.appendChild(document.createTextNode((cp.getColor() == ChessPiece.colorPlayer1 ? "1," : "2,") + cp.getType()));
                        }
                        sq.appendChild(row);
                        sq.appendChild(col);
                        sq.appendChild(piece);
                        brd.appendChild(sq);
                    }
                }
                root.appendChild(brd);
            }
        }
        else {
            // send the position of the board
            Element position = document.createElement("Position");
            position.appendChild(document.createTextNode(String.valueOf(board.getPosition())));
            root.appendChild(position);
            // send the move      
            Object obj = board.getLastMove();
            if (obj instanceof Move) {
                Move move = (Move)obj;                                
                Element startSquare = document.createElement("StartSquare");
                startSquare.appendChild(document.createTextNode(move.getStartSquare().getRow() + "," + move.getStartSquare().getCol()));
                Element endSquare = document.createElement("EndSquare");
                endSquare.appendChild(document.createTextNode(move.getEndSquare().getRow() + "," + move.getEndSquare().getCol()));
                root.appendChild(startSquare);
                root.appendChild(endSquare);
            }
            else if (obj instanceof CastleMove) {
                CastleMove castleMove = (CastleMove)obj;
                Element startSquare = document.createElement("StartSquare");
                startSquare.appendChild(document.createTextNode(castleMove.getStartKingSquare().getRow() + "," + castleMove.getStartKingSquare().getCol()));
                Element castle = document.createElement("Castle");
                castle.appendChild(document.createTextNode(String.valueOf(castleMove.getDirection())));
                root.appendChild(startSquare);
                root.appendChild(castle);                
            }                                        
            moveNumber++;            
        } 
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StringWriter buffer = new StringWriter();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(root),
                    new StreamResult(buffer));
            String strXml = buffer.toString();
            return strXml;
        } catch (Exception e) {
            return "";
        }

        /*
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String strXml = serializer.writeToString(root);
        return strXml;
        */
        //return root.toString();            
    }
    
    public void parseMessage(String msg, NetBoard board) throws Exception {        
        try {
            document = builder.parse(new InputSource(new StringReader(msg)));
            if (moveNumber == 0) {                
                // set the remote player's name
                board.setPlayerName(board.getRemotePlayer(), document.getElementsByTagName("Name").item(0).getChildNodes().item(0).getNodeValue());                
                // set up the board for remote player
                if (board instanceof RemoteBoard) {
                    while (board.getPosition() != Integer.parseInt(document.getElementsByTagName("Position").item(0).getChildNodes().item(0).getNodeValue())) {
                        board.rotate();
                    }                        
                    if (Integer.parseInt(document.getElementsByTagName("WhoseMove").item(0).getChildNodes().item(0).getNodeValue()) != board.getWhoseMove()) {
                        board.switchWhoseMove();
                    }
                    NodeList squares = document.getElementsByTagName("Square");
                    for (int i = 0; i <= squares.getLength() - 1; i++) {
                        NodeList squareProps = squares.item(i).getChildNodes();
                        int row = Integer.parseInt(squareProps.item(0).getChildNodes().item(0).getNodeValue());
                        int col = Integer.parseInt(squareProps.item(1).getChildNodes().item(0).getNodeValue());
                        String[] piece = squareProps.item(2).getChildNodes().item(0).getNodeValue().split(",");
                        ChessPiece cp = null;
                        if (piece[0].equals("0")) {
                            cp = new ChessPiece(null, ChessPiece.EMPTY);                            
                        }
                        else {
                            java.awt.Color color = piece[0].equals("1") ? ChessPiece.colorPlayer1 : ChessPiece.colorPlayer2;                            
                            switch (Integer.parseInt(piece[1])) {
                                case ChessPiece.PAWN:
                                    cp = new Pawn(color);
                                    break;
                                case ChessPiece.KNIGHT:
                                    cp = new Knight(color);
                                    break;
                                case ChessPiece.BISHOP:
                                    cp = new Bishop(color);
                                    break;
                                case ChessPiece.ROOK:
                                    cp = new Rook(color);
                                    break;
                                case ChessPiece.QUEEN:
                                    cp = new Queen(color);
                                    break;
                                case ChessPiece.KING:
                                    cp = new King(color);
                            }                            
                        }
                        Square sq = board.getSquare(row, col);
                        sq.setPiece(cp);
                        // add mouse listener        
                        cp.addMouseListener(sq.new DragMouseHandler());
                    }
                }
            }   
            else {
                String[] sqLoc = document.getElementsByTagName("StartSquare").item(0).getChildNodes().item(0).getNodeValue().split(",");                                
                // adjust the move for the position of the board
                int[] offsets = {7, 5, 3, 1, -1, -3, -5, -7};
                int row = Integer.parseInt(sqLoc[0]);
                int col = Integer.parseInt(sqLoc[1]);
		int newRow = 0;
                int newCol = 0;
                int currPos = board.getPosition();
                int remotePos = Integer.parseInt(document.getElementsByTagName("Position").item(0).getChildNodes().item(0).getNodeValue());
                while (currPos != remotePos) {
                    newRow = col;
                    newCol = row + offsets[row];
                    row = newRow;
                    col = newCol;
                    switch (remotePos) {
                        case Board.POSITION_1:                            
                            remotePos = Board.POSITION_2;
                            break;
                        case Board.POSITION_2:                            
                            remotePos = Board.POSITION_3;
                            break;
                        case Board.POSITION_3:                            
                            remotePos = Board.POSITION_4;
                            break;
                        case Board.POSITION_4:                            
                            remotePos = Board.POSITION_1;
                            break;                            
                    }
                }
                Square startSquare = board.getSquare(row, col);
                if (document.getElementsByTagName("Castle").getLength() == 0) {
                    // parse a regular move
                    sqLoc = document.getElementsByTagName("EndSquare").item(0).getChildNodes().item(0).getNodeValue().split(",");                    
                    // adjust the move for the position of the board
                    row = Integer.parseInt(sqLoc[0]);
                    col = Integer.parseInt(sqLoc[1]);
                    remotePos = Integer.parseInt(document.getElementsByTagName("Position").item(0).getChildNodes().item(0).getNodeValue());
                    while (currPos != remotePos) {
                        newRow = col;
                        newCol = row + offsets[row];
                        row = newRow;
                        col = newCol;
                        switch (remotePos) {
                            case Board.POSITION_1:                            
                                remotePos = Board.POSITION_2;
                                break;
                            case Board.POSITION_2:                            
                                remotePos = Board.POSITION_3;
                                break;
                            case Board.POSITION_3:                            
                                remotePos = Board.POSITION_4;
                                break;
                            case Board.POSITION_4:                            
                                remotePos = Board.POSITION_1;
                                break;                            
                        }
                    }
                    Square endSquare = board.getSquare(row, col);
                    board.movePiece(startSquare, endSquare, false);
                }
                else {
                    // parse a castle move
                    King king = (King)startSquare.getPiece();                    
                    king.castle(Integer.parseInt(document.getElementsByTagName("Castle").item(0).getChildNodes().item(0).getNodeValue()), false);                    
                }
            }
        }
        catch (SAXException sxe) {
            // Error generated during parsing
            Exception  x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            throw new Exception("Error parsing message received from remote computer: " + x);
        }
        catch (IOException ioe) {
            // I/O error
            throw new Exception("I/O Error in parsing message");            
        }  
        catch (Exception e) {
            throw new Exception("Error parsing message received from remote computer: " + e);
        }
    }
}
