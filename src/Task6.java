
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

	private Vector<Car> cars = new Vector<Car>();			//Источник данных графического списка
	private ListBox listBox = new ListBox();				//Графический список
	private SearchBox searchBox = new SearchBox();			//Строка поиска
	private JPopupMenu listBoxPopupMenu = new JPopupMenu();	//Контекстное меню списка

	private StatusBar statusBar = new StatusBar();					//Строка статуса
	private JLabel elementsStatusLabel = new JLabel("Элементы");	//Отображение общего количества элементов и количества отображенных в списке в данный момент
	private JLabel sortingStatusLabel = new JLabel("Сортировка");	//Отображение информации о действующей сортировке

	//Действующий компаратор (нужен для сортировки)
	CollectionComparator comparator = new CollectionComparator("", CollectionComparator.Order.Alphabetical);
	ButtonGroup radioGroupSorting = new ButtonGroup();		//радиобатоны меню "Сортировка", отвечающие за выбор поля сортировки
	ButtonGroup radioGroupSortingOrder = new ButtonGroup();	//радиобатоны меню "Сортировка", отвечающие за выбор направления сортировки

	Settings settings = new Settings();//Настройки приложения
	
	private ItemDialog itemDialog = new ItemDialog(this);			//Диалог создания/изменения элемента списка

	
	public Form() {

		setTitle("Каталог автомобилей");
		setSize(500, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowWatcher());
		
		//загрузка настроек
		loadSettings();

		//Создание меню и разметка
		createMenu();
		add(searchBox, BorderLayout.NORTH);
		add(createMainPanel());
		add(statusBar, BorderLayout.SOUTH);
		statusBar.add(elementsStatusLabel);
		statusBar.add(sortingStatusLabel);
		createListBoxPopupMenu();

		//установка делегатов (событий)
		searchBox.textChangedEventHandler = new SearchBoxTextChangedHandler();
		listBox.itemDoubleClickEventHandler = new ListBoxItemDoubleClick();
		listBox.onDeleteKeyPressed = new ListBoxOnDeleteKeyPressed();
		itemDialog.onSaveNewItem = new ItemDialogOnSaveNewItem();
		itemDialog.onChangeItem = new ItemDialogOnChangeItem();
		listBox.onRightButtonClick = new ListBoxOnRightButtonClick();

		//загрузка данных из списка при старте
		if (settings.loadOnStartup)
			loadList();

		setElementsStatusLabel();
		setSortingStatusLabel();
	}

	
	// Создание панели списка
	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(new JScrollPane(listBox), BorderLayout.CENTER);
		return panel;
	}
///////////////////////////////////////////////////////////////////////////////	
	

	
	//Определение делегатов
///////////////////////////////////////////////////////////////////////////////		
	
	//Событие при изменении текста поиска
	class SearchBoxTextChangedHandler implements Consumer<String> {
		@Override
		public void accept(String text) {
			updateListBox();
		}
	}

	//Двойной клик по элементу списка
	class ListBoxItemDoubleClick implements Consumer<Integer> {
		@Override
		public void accept(Integer index) {
			itemDialog.show(listBox.getModel().getElementAt(index));
		}
	}

	//Удаление выделенных элементов при нажатии клавиши delete
	class ListBoxOnDeleteKeyPressed implements Consumer<List<Car>> {
		@Override
		public void accept(List<Car> selectedItems) {
			removeItems(selectedItems);
		}
	}

	//Событие нажатия кнопки "Подтвердить" ItemDialog при создании нового элемента
	class ItemDialogOnSaveNewItem implements Consumer<Car> {
		@Override
		public void accept(Car car) {
			cars.add(car);
			sortAndUpdateListBox();
		}
	}

	//Событие нажатия кнопки "Подтвердить" ItemDialog при изменении существующего элемента
	class ItemDialogOnChangeItem implements Consumer<Car> {
		@Override
		public void accept(Car car) {
			sortAndUpdateListBox();
		}
	}


	//Последний выделенный элемент. Именно он будут открыт при выборе пункта "Изменить" в контекстном меню
	private int lastSelected = 0;
	
	//Вызов контекстного меню списка при нажатии правой кнопки мыши
	class ListBoxOnRightButtonClick implements Consumer<MouseEvent> {
		@Override
		public void accept(MouseEvent e) {
			listBoxPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			lastSelected = listBox.locationToIndex(e.getPoint());
		}
	}
///////////////////////////////////////////////////////////////////////////////	
	
	
	
	//Создание главного меню
///////////////////////////////////////////////////////////////////////////////	
	
	// меню
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu list = new JMenu("Список");
		JMenu sort = new JMenu("Сортировка");
		JMenu settings = new JMenu("Настройки");

		menuBar.add(list);
		menuBar.add(sort);
		menuBar.add(settings);

		createMenuList(list);
		createMenuSort(sort);
		createMenuSettings(settings);
	}

	// меню "Список"
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

	// меню "Сортировка"
	private void createMenuSort(JMenu sortMenu) {
		//группа радио-кнопок, отвечающих за выбор поля сортировки
		{
			JRadioButtonMenuItem[] radioItems = { 
					new JRadioButtonMenuItem("марка"), 
					new JRadioButtonMenuItem("модель"),
					new JRadioButtonMenuItem("мощность"),
					new JRadioButtonMenuItem("объем двигателя"),
					new JRadioButtonMenuItem("максимальная скорость"), 
					new JRadioButtonMenuItem("расход топлива"), 
			};
			//установка активного радиобатона, по индексу, указанному в настройках
			radioItems[settings.sortingFieldIndex].setSelected(true);

			ActionListener action = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//вычисляем порядковый номер выбранного элемента в радио-группе
					int index = Arrays.asList(radioItems).indexOf(e.getSource());

					//установка имени поля, по которому компаратор будет сравнивать объекты
					//поле выбирается по вычисленному индексу
					comparator.setField(Car.class.getDeclaredFields()[index].getName());
					sortAndUpdateListBox();//обновление графического списка
					setSortingStatusLabel();

					//обновление настроек
					settings.sortingFieldIndex = index;
					saveSettings();
				}
			};

			int i = 0;
			for (JRadioButtonMenuItem item : radioItems) {
				item.addActionListener(action);
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + ++i, 0));	//установка хоткея
				radioGroupSorting.add(item);	//добавление в радио-группу
				sortMenu.add(item);					//добавление в меню
			}
		}
		sortMenu.addSeparator();
		//группа радио-кнопок, отвечающих за порядок сортировки
		{
			JRadioButtonMenuItem[] radioItems = { 
					new JRadioButtonMenuItem("по возрастанию"),
					new JRadioButtonMenuItem("по убыванию"), 
			};
			//установка активного радиобатона, по значению, указанному в настройках
			radioItems[settings.sortingOrder.ordinal()].setSelected(true);
			
			//установка хоткеев
			radioItems[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
			radioItems[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0));

			for (JRadioButtonMenuItem item : radioItems) {
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//вычисляем порядковый номер выбранного элемента в радио-группе
						int index = Arrays.asList(radioItems).indexOf(e.getSource());
						//берем элемент enum Order по индексу
						CollectionComparator.Order order = CollectionComparator.Order.values()[index];

						//установка порядка сортировки для компаратора
						comparator.setOrder(order);
						sortAndUpdateListBox();
						setSortingStatusLabel();

						//обновление настроек
						settings.sortingOrder = order;
						saveSettings();
					}
				});
				radioGroupSortingOrder.add(item);	//добавление в радио-группу
				sortMenu.add(item);					//добавление в меню
			}
		}
	}

	// меню "Настройки"
	private void createMenuSettings(JMenu settingsMenu) {
		JCheckBoxMenuItem loadOnStartup = new JCheckBoxMenuItem("Автозагрузка при старте", this.settings.loadOnStartup);
		JCheckBoxMenuItem saveOnExit = new JCheckBoxMenuItem("Автосохранение при выходе", this.settings.saveOnExit);
		JCheckBoxMenuItem resetAfterSave = new JCheckBoxMenuItem("Сброс после сохранения элемента", this.settings.resetAfterSave);

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

	
	//События меню "Список"
///////////////////////////////////////////////////////////////////////////////	
	
	//вспомогательная функция
	private Window getWindow() {
		return this;
	}

	// Команда для кнопки "Выход"
	class ExitAction extends AbstractAction {

		ExitAction() {
			putValue(NAME, "Выход");
		}

		public void actionPerformed(ActionEvent e) {
			dispatchEvent(new WindowEvent(getWindow(), WindowEvent.WINDOW_CLOSING));
		}
	}

	// Команда для кнопки "Добавить"
	class CreateNewElementAction extends AbstractAction {

		public CreateNewElementAction() {
			putValue(NAME, "Добавить");
		}

		public void actionPerformed(ActionEvent e) {
			itemDialog.show(new Car());
		}
	}

	// Команда для кнопки "Загрузить"
	class LoadItemsAction extends AbstractAction {

		public LoadItemsAction() {
			putValue(NAME, "Загрузить");
		}

		public void actionPerformed(ActionEvent e) {
			loadList();
		}
	}

	// Команда для кнопки "Сохранить"
	class SaveItemsAction extends AbstractAction {

		public SaveItemsAction() {
			putValue(NAME, "Сохранить");
		}

		public void actionPerformed(ActionEvent e) {
			SerializeObject.toFile("cars", cars);
		}
	}
///////////////////////////////////////////////////////////////////////////////	

	
	
	
	//Создание контекстного меню списка
///////////////////////////////////////////////////////////////////////////////	
	private void createListBoxPopupMenu() {
		{
			JMenuItem menuItem = new JMenuItem("Клонировать");
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
			JMenuItem menuItem = new JMenuItem("Изменить");
			listBoxPopupMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					itemDialog.show(listBox.getModel().getElementAt(lastSelected));
				}
			});
		}
//        listBoxPopupMenu.addSeparator();
		{
			JMenuItem menuItem = new JMenuItem("Удалить");
			listBoxPopupMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeItems(listBox.getSelectedValuesList());
				}
			});
		}

	}
///////////////////////////////////////////////////////////////////////////////	
	
	
	
	//Обновление статусбара
///////////////////////////////////////////////////////////////////////////////	
	// функция обновления метки "Элементы" статусбара
	private void setElementsStatusLabel() {
		Object[] args = { cars.size(), listBox.getModel().getSize() };
		MessageFormat fmt = new MessageFormat("Элементы: {1} / {0}");
		elementsStatusLabel.setText(fmt.format(args));
	}

	// функция обновления метки "Сортировка" статусбара
	private void setSortingStatusLabel() {
		Object[] args = { getSelectedButtonText(radioGroupSorting), getSelectedButtonText(radioGroupSortingOrder) };
		MessageFormat fmt = new MessageFormat("Сортировка: {0} | {1}");
		sortingStatusLabel.setText(fmt.format(args));
	}

	//Получение текста выбранной радио-кнопки
	private String getSelectedButtonText(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
			if (button.isSelected())
				return button.getText();
		}
		return null;
	}
///////////////////////////////////////////////////////////////////////////////	
	
	
	
	//Настройки: загрузка, сохранение
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


	//Обновление списка
///////////////////////////////////////////////////////////////////////////////	
	
	//Выборка из массива на основании строки поиска
	public void makeSearchSample(String text) {

		//разбиваем строку поиска на слова
		String[] words = text.split("\\s");
		//применяем регулярные выражения для поиска, не зависящего от регистра
		Pattern[] patterns = new Pattern[words.length];
		for (int i = 0; i != words.length; i++)
			patterns[i] = Pattern.compile("(?iu)" + words[i]);

		//выборка из массива
		Vector<Car> cars_new = new Vector<Car>();

		for (Car car : cars) {
			Boolean flag = true;
			
			//ищем каждое слово в brand и model
			//если хотя бы одно слово не найдено, значит элемент массива не подходит
			for (Pattern pattern : patterns) {
				if (!pattern.matcher(car.getBrand()).find() && !pattern.matcher(car.getModel()).find()) {
					flag = false;
					break;
				}
			}
			//если элемент соответствует условию поиска, добавляем его в выборку
			if (flag)
				cars_new.add(car);
		}

		//устанавливаем выборку в качестве источника данных графического списка
		listBox.setListData(cars_new);
	}
	
	private void updateListBox() {
		//если осуществляется поиск, то при обновлении делаем повторную выборку
		//либо просто устанавляваем cars в качестве источника данных JList
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


	//Изменение списка
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
	
	//загрузка списка из файла
	private void loadList() {
		cars = SerializeObject.fromFile("cars", cars);
		sortAndUpdateListBox();
	}

	//сохранение списка в файл
	private void saveList() {
		SerializeObject.toFile("cars", cars);
	}
///////////////////////////////////////////////////////////////////////////////	


///////////////////////////////////////////////////////////////////////////////	
	//сравнение векторов
	private Boolean compareVectors(Vector<Car> vec1, Vector<Car> vec2) {
		if (vec1.size() != vec2.size())
			return false;

		for (int i = 0; i != vec1.size(); i++)
			if (!vec1.get(i).isEqual(vec2.get(i)))
				return false;

		return true;
	}

	//копирование массива с содержимым
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

	//сравнение массива cars с массивом из файла
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
			//если задано автосохранение при выходе, просто сохраняем список
			if (settings.saveOnExit)	
				saveList();

			//если действующий список не совпадает со списком, записанным в файле, его нужно перезаписать
			if (listIsChanged() && settings.saveOnExit == false) {
				int Result = JOptionPane.showConfirmDialog(e.getWindow(), 
						"Сохранить изменения?", "Выход",
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