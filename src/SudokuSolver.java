import java.util.Stack;

/**
 * Place for your code.
 */
public class SudokuSolver {

	private final int BOARD_SIZE = 9;
	private final int SAMPLE_FREQUENCY = 2000;

	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		// TODO write it;
		return "Stewart Grant ()\nAlbert Kim (20567111)";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) {
		// TODO write it;

		// this stack will be used for the DFS traversal of boards
		Stack<Arc[][]> stack = new Stack<Arc[][]>();
		stack.push(initArcs(board));
		int sampler = 0;

		while(!stack.empty()){
			Arc[][] arcs = stack.pop();

			// make current arc array consistent
			while(!isClean(arcs)){
				arcConsistency(arcs);
			}
			//check if a bad assumption was made by splitting domains
			if(!totalConsistancy(arcs)){
				sampler++;
				if((sampler%SAMPLE_FREQUENCY) == 0){
					printBoard(arcs);
				}
				//System.out.println("bad split dropping board");
				continue;
			}

			// check if domain must be split
			if(!complete(arcs)){
				//System.out.println("Not complete, requires domain split");

				// find the arc position greater with domain size greater than 1
				// shouldn't need a null check because we do a complete check beforehand
				boolean[][] splits = splitableDomains(arcs);
			       	for(int i =0;i<BOARD_SIZE;i++){
					for(int j =0;j<BOARD_SIZE;j++){
						//push domains in decreasing order of size
						for(int k=BOARD_SIZE;k>1;k--){
							if(splits[i][j] && arcs[i][j].dom.size() == k){
								Arc[] splitArc = arcs[i][j].split();
								dirtyEffected(arcs, i,j);
								// create the left and right arc arrays
								Arc[][] leftArcs = clone(arcs);
								leftArcs[i][j] = splitArc[0];
								Arc[][] rightArcs= clone(arcs);
								rightArcs[i][j] = splitArc[1];

								if(consistant(leftArcs,i,j)){
									//System.out.println("push left");
									stack.push(leftArcs);
								}
								if(consistant(rightArcs,i,j)){
									//System.out.println("push right");
									stack.push(rightArcs);
								}
								splits[i][j] = false;
							}
						}
					}
				}
			} else {
				return arcToIntArray(arcs);
			}

		}

		return null;

	}

	private void arcConsistency(Arc[][] arcs){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				if(arcs[i][j].getDirty() && arcs[i][j].dom.size() > 1){
					rowCondition(arcs, i,j);
					columnCondition(arcs, i,j);
					sectorCondition(arcs, i,j);
					//if the size of the domain is now 1 set the actual board
					if(arcs[i][j].dom.size() == 1){
						//System.out.println("setting ["+i+"]["+j+"]");
						arcs[i][j].value = arcs[i][j].dom.get(0);
					}
				}

				arcs[i][j].setDirty(false);
			}
		}

	}

	private void sectorCondition(Arc[][] arcs, int i, int j){
		boolean modified = false;
		for(int k = (i - i%3); k < (i - i%3 +3); k++){
			for(int l = (j - j%3); l < (j - j%3 +3); l++){
				if(i == k && j == l){
					continue;
				} else {
					//System.out.println("cons ["+i+"]["+j+"]->["+k+"]["+l+"]");
					modified = (modified || notEqualConsistancy(arcs[i][j],arcs[k][l]));
				}
			}
		}
		if(modified){
			dirtyEffected(arcs, i,j);
		}
	}

	private void columnCondition(Arc[][] arcs, int i, int j){
		boolean modified = false;
		for(int r=0;r<BOARD_SIZE;r++){
			if(i == r){
				continue;
			} else {
				//System.out.println("cons ["+i+"]["+j+"]->["+r+"]["+j+"]");
				modified = (modified || notEqualConsistancy(arcs[i][j],arcs[r][j]));
			}
		}
		if(modified){
			dirtyEffected(arcs, i,j);
		}
	}

	private void rowCondition(Arc[][] arcs, int i, int j){
		boolean modified = false;
		for(int r=0;r<BOARD_SIZE;r++){
			if(j == r){
				continue;
			} else {
				//System.out.println("cons ["+i+"]["+j+"]->["+i+"]["+r+"]");
				modified = (modified || notEqualConsistancy(arcs[i][j],arcs[i][r]));
			}
		}
		if(modified){
			dirtyEffected(arcs, i,j);
		}
	}

	/**
	 *notEqualConsistancy makes two squares not equal consistant, the arcs are modifed so that arc a is trimmed to be consistant with arc b and not the other way around.
	 @param Arc a, the arc being made consistant
	 @param Arc b, the arc used as a comparitor
	 */
	private boolean notEqualConsistancy(Arc a, Arc b){
		boolean modified = false;
		for(int k=0;k<a.dom.size();k++){
			boolean consistant = false;
			for(int l=0;l<b.dom.size();l++){
				if(a.dom.get(k).intValue() != b.dom.get(l).intValue()){
					consistant = true;
				}
			}
			if(!consistant){
				//System.out.println("removing");
				a.dom.remove(new Integer(a.dom.get(k).intValue()));
				modified = true;
			}
		}
		return modified;

	}

	/*
	 * dirtyEffected marks all arcs that have had a dependent arc [i][j] modified.
	 * @param i row of the updated arc
	 * @param j column of the updated arc
	 */
	private Arc[][] dirtyEffected(Arc[][] arcs, int i, int j){
		for(int k=0;k<BOARD_SIZE;k++){
			if(k!=j){
				arcs[i][k].setDirty(true);
			}
			if(k!=i){
				arcs[k][j].setDirty(true);
			}
		}
		for(int k = (i - i%3); k < (i - i%3 +3); k++){
			for(int l = (j - j%3); l < (j - j%3 +3); l++){
				//System.out.println("k = "+k+" l = "+l);
				if(k!=i && l!=j){
					arcs[k][l].setDirty(true);
				}
			}
		}
		return arcs;
	}		

	private boolean totalConsistancy(Arc[][] arcs){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				if(!consistant(arcs,i,j)){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * returns true if the value of the given position is valid with respect to the rest of the board
	 * @param arcs, the board
	 * @param i, the row of the arc to check
	 * @param j, the column of the arc to check
	 * @return true if the given arc has a valid value false otherwise
	 */
	private boolean consistant(Arc[][] arcs, int i, int j){
		if(arcs[i][j].dom.size() <=0 ){
			//System.out.println("incons");
			return false;
		}
		if(arcs[i][j].value == 0){
			//System.out.println("cons");
			return true;
		}

		for(int k=0;k<BOARD_SIZE;k++){
			if(k!=j){
				if(arcs[i][k].value == arcs[i][j].value){
					//System.out.println("incons");
					return false;
				}
			}
			if(k!=i){
				if(arcs[k][j].value == arcs[i][j].value){
					//System.out.println("incons");
					return false;
				}
			}
		}
		for(int k = (i - i%3); k < (i - i%3 +3); k++){
			for(int l = (j - j%3); l < (j - j%3 +3); l++){
				//System.out.println("k = "+k+" l = "+l);
				if(k!=i && l!=j){
					if(arcs[k][l].value == arcs[i][j].value){
						//System.out.println("incons");
						return false;
					}
				}
			}
		}
		return true;
	}		


	/**
	 * Preforms a completion check on the board. The board is complete if all the values are non zero
	 *
	 * @param board 2d int array representing a sudoku board
	 * @return true if the board is solved, false otherwise
	 */
	private boolean complete(Arc[][] board){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				if(board[i][j].value == 0){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Preforms a completion check on the board. The board is clean if all the arcs cannot be reduced any more
	 *
	 * @param board 2d int array representing a sudoku board
	 * @return true if the board is solved, false otherwise
	 */
	private boolean isClean(Arc[][] arcs){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				if(arcs[i][j].getDirty()){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Intalize a set of arcs for each of squares on the sudoku board. The board is intalized with all arcs containing the values [1..9], then if the board contains a vaild set number the arc is trimmed down to the one coreect value. All of the arcs which were effected by the modification are set to dirty
	 * @param board the sudoku board with preset values
	 */
	private Arc[][] initArcs(int[][] board){
		Arc[][] arcs = new Arc[BOARD_SIZE][BOARD_SIZE];
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				//Init all new arcs
				arcs[i][j] = new Arc(board[i][j], BOARD_SIZE);
			}
		}
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				//Trim domains preset values
				if(board[i][j]!=0){
					for(int k=0;k<BOARD_SIZE;k++){
						//System.out.println("i = "+i+", j = "+j + "k ="+k);
						if(board[i][j]!=(k+1)){
							arcs[i][j].dom.remove(new Integer(k + 1));
						}
					}
					arcs = dirtyEffected(arcs, i,j);
				}
			}
		}
		return arcs;
	}

	private Arc[][] clone(Arc[][] board){
		Arc[][] arcs = new Arc[BOARD_SIZE][BOARD_SIZE];
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				arcs[i][j] = board[i][j].clone();
			}
		}
		return arcs;
	}
			
	
	private boolean[][] splitableDomains(Arc[][] arcs){
		boolean[][] splits = new boolean[BOARD_SIZE][BOARD_SIZE];
		for(int i=0; i<BOARD_SIZE; i++){
			for(int j=0; j<BOARD_SIZE; j++){
				if(arcs[i][j].dom.size() > 1){
					splits[i][j] = true;
				} else {
					splits[i][j] = false;
				}
			}
		}
		return splits;
	}

	private void printArcs(Arc[][] arcs){
		for(int i=0;i<BOARD_SIZE;i++){
			for(int j=0;j<BOARD_SIZE;j++){
				System.out.println(arcs[i][j].toString());
			}
		}
	}

	private int[][] arcToIntArray(Arc[][] arcs){
		int[][] returnArray = new int[BOARD_SIZE][BOARD_SIZE];
		for(int i=0; i<BOARD_SIZE; i++) {
			for (int j=0; j<BOARD_SIZE; j++) {
				returnArray[i][j] = arcs[i][j].value;
			}
		}
		return returnArray;
	}

	private void printBoard(Arc[][] arcs){
		for(int i=0; i<BOARD_SIZE; i++) {
			for (int j=0; j<BOARD_SIZE; j++) {
				if(arcs[i][j].value == 0){
					System.out.print(" ");
				} else {
					System.out.print(arcs[i][j].value);
				}
				if( (j > 0 && j < 8) && (j + 1)%3 == 0){
					System.out.print("|");
				}
			}
			System.out.println("");
			if( (i > 0 && i < 8) && (i + 1)%3 == 0){
				System.out.print("-----------\n");
			}
		}
		System.out.println("\n");
	}


}
