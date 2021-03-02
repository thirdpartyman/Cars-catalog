
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

public class ListBox extends JList<Car> {

	ListBox() {
		setCellRenderer(new CellRenderer());
		addMouseListener(new MyMouseListener());
		addKeyListener(new MyKeyListener());
	}

	Consumer<Integer> itemDoubleClickEventHandler = (index) -> {};
	Consumer<MouseEvent> onRightButtonClick = (e) -> {};
	Consumer<List<Car>> onDeleteKeyPressed = (selectedItems) -> {};

	
	private class MyMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			JList<?> list = (JList<?>) e.getSource();
			// двойной щелчек левой кнопкой мыши
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				int index = list.locationToIndex(e.getPoint());
				itemDoubleClickEventHandler.accept(index);
			} 
			else // щелчек правой кнопкой мыши
			if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
				int index = list.locationToIndex(e.getPoint());
				// если щелкнутый элемент не выделен, выдел€ем его (выделенные ранее будут сброшены)
				if (Arrays.binarySearch(getSelectedIndices(), index) < 0)
					setSelectedIndex(index);
				onRightButtonClick.accept(e);
			}
		}
	}


	private class MyKeyListener implements KeyListener {

		//если нажаты другие клавиши, событие не произойдет
		TreeSet<Integer> pressed_keys = new TreeSet<Integer>();

		public void keyPressed(KeyEvent e) {
			pressed_keys.add(e.getKeyCode());
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE && pressed_keys.size() == 1)
				onDeleteKeyPressed.accept(getSelectedValuesList());
			pressed_keys.remove(e.getKeyCode());
		}

		public void keyTyped(KeyEvent e) {
//	    	System.out.println(pressed_keys.size());
		}
	};
}


// ласс, отвечающий за рендеринг элемента JLIST
class CellRenderer extends JPanel implements ListCellRenderer<Car> {

	private JLabel index = new JLabel();
	private JLabel name = new JLabel();
	private JPanel detailsPanel = new JPanel();
	private JLabel[] labels = new JLabel[8];
	
	
	public CellRenderer() {		
		super(new GridBagLayout());
			
	    //настройка €чейки, отображающей пор€дковый номер
	    GridBagConstraints indexConstraints = new GridBagConstraints();
	    indexConstraints.insets = new Insets(-1, 4, 0, 4);	//отступы
	
	    //настройка €чейки name
	    GridBagConstraints panelConstraints = new GridBagConstraints();
	    panelConstraints.anchor = GridBagConstraints.WEST;
	    
	    //настройка €чейки detailsPanel
	    GridBagConstraints detailsPanelConstraints = new GridBagConstraints();
	    detailsPanelConstraints.gridx = 1;
	    detailsPanelConstraints.gridy = 1;
	    detailsPanelConstraints.anchor = GridBagConstraints.WEST;
	    detailsPanelConstraints.weightx = 1.0f;
	    
	    
		add(index, indexConstraints);
	    add(name, panelConstraints);
	    add(detailsPanel, detailsPanelConstraints);
		

	    //составление таблицы элементов detailsPanel
		detailsPanel.setLayout(new SpringLayout());
		for (int i = 0; i < labels.length; i++) {
			detailsPanel.add(labels[i] = new JLabel());
		}
		SpringUtilities.makeCompactGrid(detailsPanel, 4, 2, 1, 1, 3, 1);
		
		
		index.setFont(new Font("serif", Font.BOLD, 16));
		name.setFont(new Font("Verdana", Font.BOLD, 14));
		
		
		Border outsideBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border insideBorder = BorderFactory.createEtchedBorder();
		Border border = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
		setBorder(border);

		Border nameBorder = BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0));
		name.setBorder(nameBorder);
	}

	
	@Override
	public Component getListCellRendererComponent(JList<? extends Car> list, Car car, int index, boolean isSelected,
			boolean cellHasFocus) {
		
		this.index.setText((index+1) + ".");	
		name.setText(car.getBrand() + ' ' + car.getModel());

		//заполнение элементов detailsPanel
		int i = 0;
		labels[i++].setText("мощность двигател€:");
		labels[i++].setText(Short.toString(car.getEnginePower()) + " л.с.");	
		labels[i++].setText("объЄм двигател€:");
		labels[i++].setText(Float.toString(car.getEngineVolume()) + " л");	
		labels[i++].setText("максимальна€ скорость:");
		labels[i++].setText(Short.toString(car.getMaxSpeed()) + " км/ч");	
		labels[i++].setText("расход топлива:");
		labels[i++].setText(Float.toString(car.getFuelConsumption()) + " л/100 км");	
			

		if (isSelected) {
			Color selectionColor = list.getSelectionBackground();
			detailsPanel.setBackground(selectionColor);
			setBackground(selectionColor);
		} else {
			Color color = (index % 2 == 0) ? new Color(192, 220, 192) : new Color(255, 250, 235);
			detailsPanel.setBackground(color);
			setBackground(color);
		}
		
		return this;
	}
}
