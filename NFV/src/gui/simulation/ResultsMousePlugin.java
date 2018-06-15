package gui.simulation;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import gui.components.EdgeMenuListener;
import gui.components.MenuPointListener;
import gui.components.MouseMenus.CpuDisplay;
import gui.components.MouseMenus.MemoryDisplay;
import gui.components.MouseMenus.DiskSpaceDisplay;
import gui.components.MouseMenus.LogicalInterfaceDisplay;
import gui.components.MouseMenus.VlansDisplay;
import gui.components.VertexMenuListener;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JPopupMenu;

import model.components.Node;
import model.components.RequestSwitch;
import model.components.Server;
import model.components.SubstrateRouter;
import model.components.SubstrateSwitch;
import model.components.VirtualMachine;

/**
 * A GraphMousePlugin that brings up distinct popup menus when an edge or vertex is
 * appropriately clicked in a graph.  If these menus contain components that implement
 * either the EdgeMenuListener or VertexMenuListener then the corresponding interface
 * methods will be called prior to the display of the menus (so that they can display
 * context sensitive information for the edge or vertex).
 */
public class ResultsMousePlugin<V, E> extends AbstractPopupGraphMousePlugin {
    private JPopupMenu edgePopup, vertexPopup;
    
    /** Creates a new instance of PopupVertexEdgeMenuMousePlugin */
    public ResultsMousePlugin() {
        this(MouseEvent.BUTTON3_MASK);
    }
    
    /**
     * Creates a new instance of PopupVertexEdgeMenuMousePlugin
     * @param modifiers mouse event modifiers see the jung visualization Event class.
     */
    public ResultsMousePlugin(int modifiers) {
        super(modifiers);
    }
    
    /**
     * Implementation of the AbstractPopupGraphMousePlugin method. This is where the 
     * work gets done. You shouldn't have to modify unless you really want to...
     * @param e 
     */
    @SuppressWarnings("unchecked")
	protected void handlePopup(MouseEvent e) {
        final VisualizationViewer<V,E> vv =
                (VisualizationViewer<V,E>)e.getSource();
        Point2D p = e.getPoint();
        
        GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
        if(pickSupport != null) {
            final V v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
            if(v != null) {
                // System.out.println("Vertex " + v + " was right clicked");
                updateVertexMenu(v, vv, p);
                vertexPopup.show(vv, e.getX(), e.getY());
            } else {
                final E edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
                if(edge != null) {
                    // System.out.println("Edge " + edge + " was right clicked");
                    updateEdgeMenu(edge, vv, p);
                    edgePopup.show(vv, e.getX(), e.getY());
                  
                }
            }
        }
    }
    
    /** This actions adds/removes desired components of the vertexPopup dynamically **/
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateVertexMenu(V v, VisualizationViewer vv, Point2D point) {
    	
    	if (vertexPopup == null) return;
    	
    	Component[] menuComps = vertexPopup.getComponents();
    	for (Component comp: menuComps) {
    		if (comp instanceof CpuDisplay) {
        		vertexPopup.remove(comp);
            }
    		if (comp instanceof MemoryDisplay) {
        		vertexPopup.remove(comp);
            }
    		if (comp instanceof DiskSpaceDisplay) {
        		vertexPopup.remove(comp);
            }
    		else if (comp instanceof VlansDisplay) {
        		vertexPopup.remove(comp);
            }
    		else if (comp instanceof LogicalInterfaceDisplay){
    			vertexPopup.remove(comp);
    		}
//    		else if (comp instanceof AvailableCpuDisplay) {
//        		vertexPopup.remove(comp);
//            }
//    		else if (comp instanceof AvailableMemoryDisplay) {
//        		vertexPopup.remove(comp);
//            }
//    		else if (comp instanceof AvailableDiskSpaceDisplay) {
//        		vertexPopup.remove(comp);
//            }
//    		else if (comp instanceof AvailableVlansDisplay) {
//        		vertexPopup.remove(comp);
//            }
            if (comp instanceof VertexMenuListener) {
                ((VertexMenuListener)comp).setVertexAndView(v, vv);
            }
            if (comp instanceof MenuPointListener) {
                ((MenuPointListener)comp).setPoint(point);
            }
            
    	}
    	
    	// Specific Node parameters
    	if (v instanceof Server ||
    			v instanceof VirtualMachine) {
    		CpuDisplay cpu = new CpuDisplay();
    		cpu.setVertexAndView((Node) v, vv);
    		vertexPopup.add(cpu);
    	}
    	if (v instanceof Server ||
    			v instanceof VirtualMachine) {
    		MemoryDisplay mem = new MemoryDisplay();
    		mem.setVertexAndView((Node) v, vv);
    		vertexPopup.add(mem);
    	}
    	if (v instanceof Server ||
    			v instanceof VirtualMachine) {
    		DiskSpaceDisplay diskSpace = new DiskSpaceDisplay();
    		diskSpace.setVertexAndView((Node) v, vv);
    		vertexPopup.add(diskSpace);
    	}
    	if (v instanceof RequestSwitch ||
    			v instanceof SubstrateSwitch ||
    			v instanceof SubstrateRouter ||
    			v instanceof Server) {
    		VlansDisplay vlans = new VlansDisplay();
    		vlans.setVertexAndView((Node) v, vv);
    		vertexPopup.add(vlans);
    	}
    	if (v instanceof SubstrateRouter || 
    			v instanceof SubstrateSwitch){
    		LogicalInterfaceDisplay logic = new LogicalInterfaceDisplay();
    		logic.setVertexAndView((Node) v, vv);
    		vertexPopup.add(logic);
    		
    	}
        // Specific Substrate Node information
//    	if (v instanceof Server ||
//    			v instanceof SubstrateRouter ||
//    			v instanceof SubstrateSwitch) {
//    		AvailableCpuDisplay cpu = new AvailableCpuDisplay();
//    		cpu.setVertexAndView((Node) v, vv);
//    		vertexPopup.add(cpu);
//    		AvailableMemoryDisplay memory = new AvailableMemoryDisplay();
//    		memory.setVertexAndView((Node) v, vv);
//    		vertexPopup.add(memory);
//    	}
//    	if (v instanceof Server) {
//    		AvailableDiskSpaceDisplay ds = new AvailableDiskSpaceDisplay();
//    		ds.setVertexAndView((Node) v, vv);
//    		vertexPopup.add(ds);
//    	}
//    	if (v instanceof SubstrateSwitch ||
//    			v instanceof SubstrateRouter ||
//    			v instanceof Server) {
//    		AvailableVlansDisplay vlans = new AvailableVlansDisplay();
//    		vlans.setVertexAndView((Node) v, vv);
//    		vertexPopup.add(vlans);
//    	}
    	
    }
    
    /**
     * Getter for the edge popup.
     * @return 
     */
    public JPopupMenu getEdgePopup() {
        return edgePopup;
    }
    
    /**
     * Setter for the Edge popup.
     * @param edgePopup 
     */
    public void setEdgePopup(JPopupMenu edgePopup) {
        this.edgePopup = edgePopup;
    }
    
    /**
     * Getter for the vertex popup.
     * @return 
     */
    public JPopupMenu getVertexPopup() {
        return vertexPopup;
    }
    
    /**
     * Setter for the vertex popup.
     * @param vertexPopup 
     */
    public void setVertexPopup(JPopupMenu vertexPopup) {
        this.vertexPopup = vertexPopup;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateEdgeMenu(E edge, VisualizationViewer vv, Point2D point) {
        if (edgePopup == null) return;
        Component[] menuComps = edgePopup.getComponents();
        for (Component comp: menuComps) {
//        	if (comp instanceof AvailableBandwidthDisplay) {
//        		edgePopup.remove(comp);
//            }
            if (comp instanceof EdgeMenuListener) {
                ((EdgeMenuListener)comp).setEdgeAndView(edge, vv);
            }
            if (comp instanceof MenuPointListener) {
                ((MenuPointListener)comp).setPoint(point);
            }
        }
        
        // Specific SubstrateLink information
//        if (edge instanceof SubstrateLink) {
//    		AvailableBandwidthDisplay bw = new AvailableBandwidthDisplay();
//    		bw.setEdgeAndView((SubstrateLink) edge, vv);
//    		edgePopup.add(bw);
//    	}
    }
    
}
