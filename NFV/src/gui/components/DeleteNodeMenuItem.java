package gui.components;
/*
 * DeleteVertexMenuItem.java
 *
 * Created on March 21, 2007, 2:03 PM; Updated May 29, 2007
 *
 * Copyright March 21, 2007 Grotto Networking
 *
 */


import edu.uci.ics.jung.visualization.VisualizationViewer;
import gui.Icons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

/**
 * A class to implement the deletion of a vertex from within a 
 * PopupVertexEdgeMenuMousePlugin.
 */
@SuppressWarnings("serial")
public class DeleteNodeMenuItem<V> extends JMenuItem implements VertexMenuListener<V> {
    private V vertex;
    @SuppressWarnings("rawtypes")
	private VisualizationViewer visComp;
    
    /** Creates a new instance of DeleteVertexMenuItem */
    public DeleteNodeMenuItem() {
        super("Delete Vertex");
        this.addActionListener(new ActionListener(){
            @SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
                visComp.getPickedVertexState().pick(vertex, false);
                visComp.getGraphLayout().getGraph().removeVertex(vertex);
                visComp.repaint();
            }
        });
    }

    /**
     * Implements the VertexMenuListener interface.
     * @param v 
     * @param visComp 
     */
    @SuppressWarnings("rawtypes")
	public void setVertexAndView(V v, VisualizationViewer visComp) {
        this.vertex = v;
        this.visComp = visComp;
        this.setText("Delete Node " + v.toString());
        this.setIcon(Icons.DELETE);
    }    
}
