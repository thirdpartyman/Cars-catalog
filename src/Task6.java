
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

class Form extends JFrame {

	private Vector<Car> cars = new Vector<Car>();			//�������� ������ ������������ ������
	private ListBox listBox = new ListBox();				//����������� ������
	private SearchBox searchBox = new SearchBox();			//������ ������
	private JPopupMenu listBoxPopupMenu = new JPopupMenu();	//����������� ���� ������

	private StatusBar statusBar = new StatusBar();					//������ �������
	private JLabel elementsStatusLabel = new JLabel("��������");	//����������� ������ ���������� ��������� � ���������� ������������ � ������ � ������ ������
	private JLabel sortingStatusLabel = new JLabel("����������");	//����������� ���������� � ����������� ����������

	//����������� ���������� (����� ��� ����������)
	CollectionComparator comparator = new CollectionComparator("", CollectionComparator.Order.Alphabetical);
	ButtonGroup radioGroupSorting = new ButtonGroup();		//����������� ���� "����������", ���������� �� ����� ���� ����������
	ButtonGroup radioGroupSortingOrder = new ButtonGroup();	//����������� ���� "����������", ���������� �� ����� ����������� ����������

	Settings settings = new Settings();//��������� ����������
	
	private ItemDialog itemDialog = new ItemDialog(this);			//������ ��������/��������� �������� ������

	
	public Form() {

		setTitle("������� �����������");
		setSize(500, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowWatcher());
		
		//�������� ��������
		loadSettings();

		//�������� ���� � ��������
		createMenu();
		add(searchBox, BorderLayout.NORTH);
		add(createMainPanel());
		add(statusBar, BorderLayout.SOUTH);
		statusBar.add(elementsStatusLabel);
		statusBar.add(sortingStatusLabel);
		createListBoxPopupMenu();

		//��������� ��������� (�������)
		searchBox.textChangedEventHandler = new SearchBoxTextChangedHandler();
		listBox.itemDoubleClickEventHandler = new ListBoxItemDoubleClick();
		listBox.onDeleteKeyPressed = new ListBoxOnDeleteKeyPressed();
		itemDialog.onSaveNewItem = new ItemDialogOnSaveNewItem();
		itemDialog.onChangeItem = new ItemDialogOnChangeItem();
		listBox.onRightButtonClick = new ListBoxOnRightButtonClick();

		//�������� ������ �� ������ ��� ������
		if (settings.loadOnStartup)
			loadList();

		setElementsStatusLabel();
		setSortingStatusLabel();
	}

	
	// �������� ������ ������
	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(new JScrollPane(listBox), BorderLayout.CENTER);
		return panel;
	}
///////////////////////////////////////////////////////////////////////////////	
	

	
	//����������� ���������
///////////////////////////////////////////////////////////////////////////////		
	
	//������� ��� ��������� ������ ������
	class SearchBoxTextChangedHandler implements Consumer<String> {
		@Override
		public void accept(String text) {
			updateListBox();
		}
	}

	//������� ���� �� �������� ������
	class ListBoxItemDoubleClick implements Consumer<Integer> {
		@Override
		public void accept(Integer index) {
			itemDialog.show(listBox.getModel().getElementAt(index));
		}
	}

	//�������� ���������� ��������� ��� ������� ������� delete
	class ListBoxOnDeleteKeyPressed implements Consumer<List<Car>> {
		@Override
		public void accept(List<Car> selectedItems) {
			removeItems(selectedItems);
		}
	}

	//������� ������� ������ "�����������" ItemDialog ��� �������� ������ ��������
	class ItemDialogOnSaveNewItem implements Consumer<Car> {
		@Override
		public void accept(Car car) {
			cars.add(car);
			sortAndUpdateListBox();
		}
	}

	//������� ������� ������ "�����������" ItemDialog ��� ��������� ������������� ��������
	class ItemDialogOnChangeItem implements Consumer<Car> {
		@Override
		public void accept(Car car) {
			sortAndUpdateListBox();
		}
	}


	//��������� ���������� �������. ������ �� ����� ������ ��� ������ ������ "��������" � ����������� ����
	private int lastSelected = 0;
	
	//����� ������������ ���� ������ ��� ������� ������ ������ ����
	class ListBoxOnRightButtonClick implements Consumer<MouseEvent> {
		@Override
		public void accept(MouseEvent e) {
			listBoxPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			lastSelected = listBox.locationToIndex(e.getPoint());
		}
	}
///////////////////////////////////////////////////////////////////////////////	
	
	
	
	//�������� �������� ����
///////////////////////////////////////////////////////////////////////////////	
	
	// ����
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu list = new JMenu("������");
		JMenu sort = new JMenu("����������");
		JMenu settings = new JMenu("���������");

		menuBar.add(list);
		menuBar.add(sort);
		menuBar.add(settings);

		createMenuList(list);
		createMenuSort(sort);
		createMenuSettings(settings);
	}

	// ���� "������"
	private void createMenuList(JMenu list) {
		JMenuItem createMenuItem = new JMenuItem(new CreateNewElementAction());
		JMenuItem reloadMenuItem = new JMenuItem(new LoadItemsAction());
		JMenuItem saveMenuItem = new JMenuItem(new SaveItemsAction());
		JMenuItem exitMenuItem = new JMenuItem(new ExitAction());

		createMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		reloadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
//		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		
		list.add(createMenuItem);
		list.add(reloadMenuItem);
		list.add(saveMenuItem);
		list.addSeparator();
		list.add(exitMenuItem);
	}

	// ���� "����������"
	private void createMenuSort(JMenu sortMenu) {
		//������ �����-������, ���������� �� ����� ���� ����������
		{
			JRadioButtonMenuItem[] radioItems = { 
					new JRadioButtonMenuItem("�����"), 
					new JRadioButtonMenuItem("������"),
					new JRadioButtonMenuItem("��������"),
					new JRadioButtonMenuItem("����� ���������"),
					new JRadioButtonMenuItem("������������ ��������"), 
					new JRadioButtonMenuItem("������ �������"), 
			};
			//��������� ��������� �����������, �� �������, ���������� � ����������
			radioItems[settings.sortingFieldIndex].setSelected(true);

			ActionListener action = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//��������� ���������� ����� ���������� �������� � �����-������
					int index = Arrays.asList(radioItems).indexOf(e.getSource());

					//��������� ����� ����, �� �������� ���������� ����� ���������� �������
					//���� ���������� �� ������������ �������
					comparator.setField(Car.class.getDeclaredFields()[index].getName());
					sortAndUpdateListBox();//���������� ������������ ������
					setSortingStatusLabel();

					//���������� ��������
					settings.sortingFieldIndex = index;
					saveSettings();
				}
			};

			int i = 0;
			for (JRadioButtonMenuItem item : radioItems) {
				item.addActionListener(action);
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + ++i, 0));	//��������� ������
				radioGroupSorting.add(item);	//���������� � �����-������
				sortMenu.add(item);					//���������� � ����
			}
		}
		sortMenu.addSeparator();
		//������ �����-������, ���������� �� ������� ����������
		{
			JRadioButtonMenuItem[] radioItems = { 
					new JRadioButtonMenuItem("�� �����������"),
					new JRadioButtonMenuItem("�� ��������"), 
			};
			//��������� ��������� �����������, �� ��������, ���������� � ����������
			radioItems[settings.sortingOrder.ordinal()].setSelected(true);
			
			//��������� �������
			radioItems[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
			radioItems[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0));

			for (JRadioButtonMenuItem item : radioItems) {
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//��������� ���������� ����� ���������� �������� � �����-������
						int index = Arrays.asList(radioItems).indexOf(e.getSource());
						//����� ������� enum Order �� �������
						CollectionComparator.Order order = CollectionComparator.Order.values()[index];

						//��������� ������� ���������� ��� �����������
						comparator.setOrder(order);
						sortAndUpdateListBox();
						setSortingStatusLabel();

						//���������� ��������
						settings.sortingOrder = order;
						saveSettings();
					}
				});
				radioGroupSortingOrder.add(item);	//���������� � �����-������
				sortMenu.add(item);					//���������� � ����
			}
		}
	}

	// ���� "���������"
	private void createMenuSettings(JMenu settingsMenu) {
		JCheckBoxMenuItem loadOnStartup = new JCheckBoxMenuItem("������������ ��� ������", this.settings.loadOnStartup);
		JCheckBoxMenuItem saveOnExit = new JCheckBoxMenuItem("�������������� ��� ������", this.settings.saveOnExit);
		JCheckBoxMenuItem resetAfterSave = new JCheckBoxMenuItem("����� ����� ���������� ��������", this.settings.resetAfterSave);

		loadOnStartup.addActionListener((e) -> { this.settings.loadOnStartup = loadOnStartup.getState(); saveSettings(); });
		saveOnExit.addActionListener((e) -> { this.settings.saveOnExit = saveOnExit.getState(); saveSettings(); });
		resetAfterSave.addActionListener((e) -> {
			this.settings.resetAfterSave = resetAfterSave.getState(); 
			itemDialog.setResetAfterSave(settings.resetAfterSave);
			saveSettings();
		});
		
		settingsMenu.add(loadOnStartup);
		settingsMenu.add(saveOnExit);
		settingsMenu.add(resetAfterSave);
	}

	
	//������� ���� "������"
///////////////////////////////////////////////////////////////////////////////	
	
	//��������������� �������
	private Window getWindow() {
		return this;
	}

	// ������� ��� ������ "�����"
	class ExitAction extends AbstractAction {

		ExitAction() {
			putValue(NAME, "�����");
		}

		public void actionPerformed(ActionEvent e) {
			dispatchEvent(new WindowEvent(getWindow(), WindowEvent.WINDOW_CLOSING));
		}
	}

	// ������� ��� ������ "��������"
	class CreateNewElementAction extends AbstractAction {

		public CreateNewElementAction() {
			putValue(NAME, "��������");
		}

		public void actionPerformed(ActionEvent e) {
			itemDialog.show(new Car());
		}
	}

	// ������� ��� ������ "���������"
	class LoadItemsAction extends AbstractAction {

		public LoadItemsAction() {
			putValue(NAME, "���������");
		}

		public void actionPerformed(ActionEvent e) {
			loadList();
		}
	}

	// ������� ��� ������ "���������"
	class SaveItemsAction extends AbstractAction {

		public SaveItemsAction() {
			putValue(NAME, "���������");
		}

		public void actionPerformed(ActionEvent e) {
			SerializeObject.toFile("cars", cars);
		}
	}
///////////////////////////////////////////////////////////////////////////////	

	
	
	
	//�������� ������������ ���� ������
///////////////////////////////////////////////////////////////////////////////	
	private void createListBoxPopupMenu() {
		{
			JMenuItem menuItem = new JMenuItem("�����������");
			listBoxPopupMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (listBox.getSelectedIndices().length == 1) {
							Car newItem = listBox.getSelectedValue().clone();
							addItem(newItem);
							itemDialog.show(newItem);
						} else {
							List<Car> clonedList = cloneList(listBox.getSelectedValuesList());
							addItems(clonedList);
						}
					} catch (CloneNotSupportedException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
//        listBoxPopupMenu.addSeparator();
		{
			JMenuItem menuItem = new JMenuItem("��������");
			listBoxPopupMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					itemDialog.show(listBox.getModel().getElementAt(lastSelected));
				}
			});
		}
//        listBoxPopupMenu.addSeparator();
		{
			JMenuItem menuItem = new JMenuItem("�������");
			listBoxPopupMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeItems(listBox.getSelectedValuesList());
				}
			});
		}

	}
///////////////////////////////////////////////////////////////////////////////	
	
	
	
	//���������� ����������
///////////////////////////////////////////////////////////////////////////////	
	// ������� ���������� ����� "��������" ����������
	private void setElementsStatusLabel() {
		Object[] args = { cars.size(), listBox.getModel().getSize() };
		MessageFormat fmt = new MessageFormat("��������: {1} / {0}");
		elementsStatusLabel.setText(fmt.format(args));
	}

	// ������� ���������� ����� "����������" ����������
	private void setSortingStatusLabel() {
		Object[] args = { getSelectedButtonText(radioGroupSorting), getSelectedButtonText(radioGroupSortingOrder) };
		MessageFormat fmt = new MessageFormat("����������: {0} | {1}");
		sortingStatusLabel.setText(fmt.format(args));
	}

	//��������� ������ ��������� �����-������
	private String getSelectedButtonText(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected())
				return button.getText();
		}
		return null;
	}
///////////////////////////////////////////////////////////////////////////////	
	
	
	
	//���������: ��������, ����������
///////////////////////////////////////////////////////////////////////////////		
	private void loadSettings() {
		settings = SerializeObject.fromFile("settings", settings);
		comparator = new CollectionComparator(Car.class.getDeclaredFields()[settings.sortingFieldIndex].getName(),
				settings.sortingOrder);
		itemDialog.setResetAfterSave(settings.resetAfterSave);
	}

	private void saveSettings() {
		SerializeObject.toFile("settings", settings);
	}
///////////////////////////////////////////////////////////////////////////////	


	//���������� ������
///////////////////////////////////////////////////////////////////////////////	
	
	//������� �� ������� �� ��������� ������ ������
	public void makeSearchSample(String text) {

		//��������� ������ ������ �� �����
		String[] words = text.split("\\s");
		//��������� ���������� ��������� ��� ������, �� ���������� �� ��������
		Pattern[] patterns = new Pattern[words.length];
		for (int i = 0; i != words.length; i++)
			patterns[i] = Pattern.compile("(?iu)" + words[i]);

		//������� �� �������
		Vector<Car> cars_new = new Vector<Car>();

		for (Car car : cars) {
			Boolean flag = true;
			
			//���� ������ ����� � brand � model
			//���� ���� �� ���� ����� �� �������, ������ ������� ������� �� ��������
			for (Pattern pattern : patterns) {
				if (!pattern.matcher(car.getBrand()).find() && !pattern.matcher(car.getModel()).find()) {
					flag = false;
					break;
				}
			}
			//���� ������� ������������� ������� ������, ��������� ��� � �������
			if (flag)
				cars_new.add(car);
		}

		//������������� ������� � �������� ��������� ������ ������������ ������
		listBox.setListData(cars_new);
	}
	
	private void updateListBox() {
		//���� �������������� �����, �� ��� ���������� ������ ��������� �������
		//���� ������ ������������� cars � �������� ��������� ������ JList
		if (searchBox.getText().isEmpty())
			listBox.setListData(cars);
		else
			makeSearchSample(searchBox.getText());
		setElementsStatusLabel();
	}

	private void sortAndUpdateListBox() {
		Collections.sort(cars, comparator);
		updateListBox();
	}
///////////////////////////////////////////////////////////////////////////////	


	//��������� ������
///////////////////////////////////////////////////////////////////////////////	
	private void addItem(Car item) {
		cars.add(item);
		sortAndUpdateListBox();
	}

	private void addItems(List<Car> items) {
		cars.addAll(items);
		sortAndUpdateListBox();
	}

	private void removeItem(Car item) {
		cars.remove(item);
		sortAndUpdateListBox();
	}

	private void removeItems(List<Car> items) {
		cars.removeAll(items);
		sortAndUpdateListBox();
	}
	
	//�������� ������ �� �����
	private void loadList() {
		cars = SerializeObject.fromFile("cars", cars);
		sortAndUpdateListBox();
	}

	//���������� ������ � ����
	private void saveList() {
		SerializeObject.toFile("cars", cars);
	}
///////////////////////////////////////////////////////////////////////////////	


///////////////////////////////////////////////////////////////////////////////	
	//��������� ��������
	private Boolean compareVectors(Vector<Car> vec1, Vector<Car> vec2) {
		if (vec1.size() != vec2.size())
			return false;

		for (int i = 0; i != vec1.size(); i++)
			if (!vec1.get(i).isEqual(vec2.get(i)))
				return false;

		return true;
	}

	//����������� ������� � ����������
	public static List<Car> cloneList(List<Car> list) {
		List<Car> clone = new ArrayList<Car>(list.size());
		try {
			for (Car item : list)
				clone.add(item.clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	//��������� ������� cars � �������� �� �����
	private boolean listIsChanged() {
		Vector<Car> temp = new Vector<Car>();
		temp = SerializeObject.fromFile("cars", temp);
		Collections.sort(temp, comparator);
		return !compareVectors(cars, temp);
	}
///////////////////////////////////////////////////////////////////////////////	

	
	public class WindowWatcher implements WindowListener {

		@Override
		public void windowClosing(WindowEvent e) {
			//���� ������ �������������� ��� ������, ������ ��������� ������
			if (settings.saveOnExit)	
				saveList();

			//���� ����������� ������ �� ��������� �� �������, ���������� � �����, ��� ����� ������������
			if (listIsChanged() && settings.saveOnExit == false) {
				int Result = JOptionPane.showConfirmDialog(e.getWindow(), 
						"��������� ���������?", "�����",
						JOptionPane.YES_NO_CANCEL_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
				switch (Result) {
				case JOptionPane.YES_OPTION:
					saveList();
				case JOptionPane.NO_OPTION:
					System.exit(0);
				case JOptionPane.CANCEL_OPTION:
				}
			} else System.exit(0);
		}

		@Override public void windowActivated(WindowEvent arg0) {}
		@Override public void windowClosed(WindowEvent arg0) {}
		@Override public void windowDeactivated(WindowEvent arg0) {}
		@Override public void windowDeiconified(WindowEvent arg0) {}
		@Override public void windowIconified(WindowEvent arg0) {}
		@Override public void windowOpened(WindowEvent arg0) {}
	}

}

public class Task6 {
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		EventQueue.invokeLater( () -> new Form().show() );
	}
}