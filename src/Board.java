/**
 * Board+java
 * 
 * @license		Creative Commons Attribution-NonCommercial 3.0 Unported [http://creativecommons+org/licenses/by-nc/3.0/]
 * @author		Chris Granville
 * @link		http://chrispyslicevwordpress+com/
 * @email		granville <dot> chris <at> gmail <dot> com
 * 
 * @file		Boardvjava
 * @version		1.0
 * @date		01/24/2011
 * 
 * Copyright (c) 2011 Chris Granville. All rights reserved.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import java.util.Vector;

public class Board extends Frame
{
	private static final int MAX_BOARD_SIZE = 8, GRID_BORDER = 50, PIECE_SIZE = 20;
	private static final char WHITE = 'w', BLACK = 'b', BLANK = ' ';
	private static final boolean DEBUG = true, INSERT_TEST_PIECES = false;
	
	private boolean blackTurn;
	private Insets border;
	private Dimension windowSize;
	private int size, posX, posY, row;
	private float rowPixelHeight, columnPixelWidth;
	private char pieces[][], winner;
	
	public Board(int size)
	{
		// Initialize size
		this.size = (size > MAX_BOARD_SIZE) ? size : MAX_BOARD_SIZE;

		// Initialize the pieces array
		this.pieces = new char[this.size][this.size];
		this.clearGrid();

		// Init turns and winners
		this.blackTurn = false;
		this.winner = BLANK;
		
		// Setup the window
		setTitle("The Board");
		setSize(500, 500);
		setBackground(Color.GRAY);
		setVisible(true);

		// Make sure we close the program when the window is closed
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt)
			{
				System.exit(0);
			}
		});

		// Mouse interactions
		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent evt)
			{
				if(winner == WHITE || winner == BLACK) clearGrid();
				else makeMove(evt.getX(), evt.getY());
			}
		});
	}
	
	private void print()
	{
		for(int i = 0; i < this.pieces.length; i++)
		{
			for(int j = 0; j < this.pieces.length; j++)
			{
				System.out.print(this.pieces[i][j]);
			}
			
			System.out.print('\n');
		}
	}

	private void clearGrid()
	{
		// Set all the pieces to BLANK
		for(int i = 0; i < pieces.length; i++) for(int j = 0; j < pieces.length; j++) pieces[i][j] = BLANK;

		// Insert some test pieces
		if(INSERT_TEST_PIECES)
		{
			pieces[4][5] = BLACK;
			pieces[0][0] = WHITE;
			pieces[7][7] = BLACK;
		}

		winner = BLANK;
		repaint();
	}

	/**
	 * Make a move
	 */
	private boolean makeMove(int x, int y)
	{
		// Check whether the click is within the grid 
		if(!((x >= GRID_BORDER + border.left) && (x <= GRID_BORDER + border.left + (size * columnPixelWidth)) && (y >= GRID_BORDER + border.top) &&  (y <= GRID_BORDER + border.top +  (size * rowPixelHeight)))) return false;

		// Calculate which grid space has been clicked on by first getting the mouseX and mouseY minus the top left of the grid, then dividing that by the height of the rows and width of the columns
		int xGridSpace = (int) ((x - (GRID_BORDER + border.left)) / columnPixelWidth),
			yGridSpace = (int) ((y - (GRID_BORDER + border.top)) / rowPixelHeight);

		//if(DEBUG) System.out.println("Grid (" + xGridSpace + ", " + yGridSpace + ") has been clicked");

		// Check if the space has already been taken
		if(pieces[xGridSpace][yGridSpace] != BLANK) return false;

		// Actually set the piece
		pieces[xGridSpace][yGridSpace] = blackTurn ? BLACK : WHITE;	

		// Change the turn indicator
		blackTurn = !blackTurn;

		// Check if we have a winner yet
		determineWinner();

		// Make sure we redraw the window
		repaint();

		return true;
	}

	/**
	 * Determine if the either player has won
	 */
	private void determineWinner()
	{
		// Get which kind of piece to parse
		char kind = blackTurn ? WHITE : BLACK;

		// 
		for(int i = 0; i < size; i++)
		{
			if(kind == WHITE && pieces[i][0] == WHITE && pathRun(new Vector<Point>(), WHITE, i, 0)) winner = WHITE;
			else if(kind == BLACK && pieces[0][i] == BLACK && pathRun(new Vector<Point>(), BLACK, 0, i)) winner = BLACK;
		}
	}

	/**
	 * Determine if there is a path
	 */
	private boolean pathRun(Vector<Point> searched, char type, int x, int y)
	{
		Stack<Point> traverse = new Stack<Point>();
		traverse.push(new Point(x, y));

		while(!traverse.empty())
		{
			Point current = traverse.pop();

			// already been searched
			if(searched.contains(current)) continue;
			
			// Invalid point
			if(!inGrid(current.x, current.y)) continue;

			// Not the same type
			if(pieces[current.x][current.y] != type) continue;

			searched.add(current);

			// Check if it's a winner
			if((type == BLACK && current.x == size -1) || (type == WHITE && current.y == size-1)) return true;

			// Add neighbours to the stack to check for more points
			traverse.push(new Point(current.x + 1, current.y));
			traverse.push(new Point(current.x - 1, current.y));
			traverse.push(new Point(current.x, current.y + 1));
			traverse.push(new Point(current.x, current.y -1 ));
		}

		return false;
	}

	private boolean inGrid(int x, int y)
	{
		if((x < 0) || (x > size)) return false;
		if((y < 0) || (y > size)) return false;

		return true;
	}
	
	/**
	 * paint
	 */
	public void paint(Graphics gfx)
	{
		// Set anti-aliasing
		((Graphics2D) gfx).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Get window size
		windowSize = getSize();
		
		// Get window border size
		this.border = getInsets();
		
		// Calculate the row/column size in pixels here to we can resize the window
		this.rowPixelHeight = ((float) windowSize.height - border.top - border.bottom - (GRID_BORDER * 2)) / size;
		this.columnPixelWidth = ((float) windowSize.width - border.left - border.right - (GRID_BORDER * 2)) / size;
		
		// Set initial starting coordinates
		posX = GRID_BORDER + border.left;
		posY = GRID_BORDER + border.top;

		// Draw borders
		gfx.drawLine(posX, posY, windowSize.width - GRID_BORDER, posY - border.right); // Horizontal
		gfx.drawLine(posX, posY, posX, windowSize.height - GRID_BORDER - border.bottom); // Vertical
		
		for(int i = 0; i < size; i++)
		{
			// Grid
			// Calculate the offsets
			int yOffset = (int) ((i + 1) * rowPixelHeight),
				xOffset = (int) ((i + 1) * columnPixelWidth);

			// Lower horizontal
			gfx.drawLine(posX, posY + yOffset, windowSize.width - GRID_BORDER, posY + yOffset - border.right);

			// Righthand vertical
			gfx.drawLine(posX + xOffset, posY, posX + xOffset, windowSize.height - GRID_BORDER - border.bottom);

			// Draw pieces
			for(int j = 0; j < size; j++)
			{
				if(pieces[i][j] != BLANK)
				{
					// Calculate starting position for the pieces
					// Do this by getting the left or top-most position, adding on the row/column height * the row/column number (giving the co-ordinates of the row/column), getting the middle of the row and offsetting for the piece size
					int x = (int) ((posX + (columnPixelWidth * i) + (columnPixelWidth / 2))) - (PIECE_SIZE / 2),
						y = (int) ((posY + (rowPixelHeight * j) + (rowPixelHeight / 2))) - (PIECE_SIZE / 2);

					// Draw the pieces
					gfx.setColor(pieces[i][j] == WHITE ? Color.WHITE : Color.BLACK);
					gfx.fillOval(x, y, PIECE_SIZE, PIECE_SIZE);
				}
			}

			// Reset color
			gfx.setColor(Color.BLACK);
		}

		// Draw turn information
		gfx.drawString(((String) (blackTurn ? "Black" : "White") + "'s turn"), posX, posY - 10);

		// Do we have a winner?
		if(winner == WHITE || winner == BLACK)
		{
			String winner_info = "The winner is ";
			if(winner == WHITE) winner_info += "white";
			else if(winner == BLACK) winner_info += "black";
			winner_info += ", click again to restart";

			gfx.drawString(winner_info, posX, windowSize.height - 15);
		}
	}
	
	public static void main(String[] args)
	{
		Board board = new Board(8);
	}
}

/* End of file Board.java */
/* Location: ./Volumes/roadrunner/Users/chris/Documents/dev/java/boardgame/src/Board.java */