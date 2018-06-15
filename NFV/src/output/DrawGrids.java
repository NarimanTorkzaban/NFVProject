package output;

import java.awt.Frame;

public class DrawGrids extends Frame {
    /**
	 * 
	 */
   private static final long serialVersionUID = 1L;
   	 private Grids grid=null;

	public DrawGrids(String title, int w, int h, int rows, int columns) {
      setTitle(title);
      this.grid = new Grids(w, h, rows, columns);
      add(grid);      
    }
	
	
	public Grids getGrid(){
		return this.grid;
	}
  }

