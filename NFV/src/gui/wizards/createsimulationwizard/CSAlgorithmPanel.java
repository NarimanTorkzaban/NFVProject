package gui.wizards.createsimulationwizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;

import model.Algorithm;

public class CSAlgorithmPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Title panel elements
    private JPanel titlePanel;
    private JLabel title;
    private JSeparator separator;
	
	// Main panel elements
    @SuppressWarnings("rawtypes")
	private JList availableList;
    @SuppressWarnings("rawtypes")
	private DefaultListModel availableListModel;

    // Model elements
    private List<Algorithm> algorithms;
    
	private JPanel contentPanel;
    
    public CSAlgorithmPanel(List<Algorithm> algorithms) {
        
    	this.algorithms = algorithms;
    	
        contentPanel = getContentPanel();
        contentPanel.setBorder(new EmptyBorder(new Insets(50, 50, 50, 50)));

        setLayout(new java.awt.BorderLayout()); 
        
        // Setting title
        titlePanel = new JPanel();
        separator = new JSeparator();
        titlePanel.setLayout(new java.awt.BorderLayout());
        titlePanel.setBackground(Color.gray);
        
        title = new JLabel();
        title.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
        title.setText("Select the algorithm to use in the simulation");
        title.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        title.setOpaque(true);
        
        titlePanel.add(title, BorderLayout.CENTER);
        titlePanel.add(separator, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);
        JPanel secondaryPanel = new JPanel();
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.CENTER);

    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JPanel getContentPanel() {
		
		JPanel contentPanel1 = new JPanel();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        JLabel availableLabel = new JLabel("Available algorithms");
        availableLabel.setFont(new Font("MS Sans Serif", Font.BOLD, 12));
        availableLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(availableLabel);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        availableListModel = new DefaultListModel();
		availableList = new JList(availableListModel);
		availableList.setName("availableList");
		AlgorithmListCellRenderer alcr = new AlgorithmListCellRenderer();
		availableList.setCellRenderer(alcr);
		initAvailableList();
		JScrollPane availablePane = new JScrollPane(availableList);
		availablePane.setPreferredSize(new Dimension(200,250));
		availablePane.setBorder(BorderFactory.createLineBorder(SystemColor.activeCaption, 2));
		panel.add(availablePane);
        
        contentPanel1.add(panel);
        
        return contentPanel1;
	}

	@SuppressWarnings("unchecked")
	private void initAvailableList() {
		for (Algorithm algorithm : algorithms)
			availableListModel.addElement(algorithm);
	}

	public void addListeners(ListSelectionListener s) {
		availableList.addListSelectionListener(s);
    }

	@SuppressWarnings("deprecation")
	public boolean canFinish() {
		return availableList.getSelectedValues().length==1;
	}

	@SuppressWarnings("deprecation")
	public Algorithm getSelectedAlgorithm() {
		if (availableList.getSelectedValues().length!=1) return null;
		return (Algorithm) availableList.getSelectedValue();
	}

}