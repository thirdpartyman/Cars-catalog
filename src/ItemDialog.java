import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

public class ItemDialog extends JDialog {
	
	private JTextField brandTextField = new JTextField();
	private JTextField modelTextField = new JTextField();
	private JSpinner enginePowerSpinner = new JSpinner(new SpinnerNumberModel((short)0, (short)0, null, (short)1));
	private JSpinner maxSpeedSpinner = new JSpinner(new SpinnerNumberModel((short)0, (short)0, null, (short)1));
	private JSpinner engineVolumeSpinner = new JSpinner(new SpinnerNumberModel(0f, 0f, null, 0.1f));	
	private JSpinner fuelConsumptionSpinner = new JSpinner(new SpinnerNumberModel(0f, 0f, null, 0.1f));
	
	private JButton confirmButton = new JButton("���������");
	private JButton resetButton = new JButton("�����");

	private boolean isNewItem;	//����, ������������ ��������� ����� ������� ��� ���������� ������������
	private Car refCar;			//����������/����������� ������
	
	private Boolean resetAfterSave;	//���������� � �������� ������ �������� ��� ������ ������ "���������" ������� ItemDialog
    

	public ItemDialog(Frame parent) {
		super(parent);
		setModalityType(ModalityType.MODELESS);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocationRelativeTo(getParent());
//		addWindowListener(new WindowWatcher());
		setEscapeCloseOperation(this);	//�������� ���� ������� �� ������� ������� Escape	
		init();//��������
	}
	
	//��������� �����, ��������� ��������� ���� ��� �������������
	private void setNewItemFlag(Boolean state)
	{
		isNewItem = state;
		this.setTitle((isNewItem) ? "��������" : "��������� \"" + refCar.getBrand() + ' ' + refCar.getModel() + "\"");
	}

	public void setResetAfterSave(Boolean state)
	{
		resetAfterSave = state;
	}

	//����������� �������, ������� ����� �� ������ ����������� �������
	public void show(Car reference) {
		refCar = reference;
		brandTextField.setText(refCar.getBrand());
		modelTextField.setText(refCar.getModel());
		enginePowerSpinner.setValue(refCar.getEnginePower());
		engineVolumeSpinner.setValue(refCar.getEngineVolume());
		maxSpeedSpinner.setValue(refCar.getMaxSpeed());
		fuelConsumptionSpinner.setValue(refCar.getFuelConsumption());

		setNewItemFlag(refCar.isEqual(new Car()));
		super.show();
	}


	private void init() {
		// ������ ����� ����� � ���������
		List<Map.Entry<String, Component>> pairList = new ArrayList<>();
		pairList.add(new SimpleEntry<>("�����", brandTextField));
		pairList.add(new SimpleEntry<>("������", modelTextField));
		pairList.add(new SimpleEntry<>("��������, �.�.", enginePowerSpinner));
		pairList.add(new SimpleEntry<>("����� ���������, �", engineVolumeSpinner));
		pairList.add(new SimpleEntry<>("������������ ��������, ��/�", maxSpeedSpinner));
		pairList.add(new SimpleEntry<>("������ �������, �/100 ��", fuelConsumptionSpinner));

		setLeftAligmentForSpinners(pairList);	//������������ ��������� �� ������ ����
		enableSelectionOnClick(pairList);		//��������� ������ ���� ��� ��������� ������
		createLayout(pairList);					//���������� �����������

		pack();
		Dimension size = getSize();
		size.width = (int)(size.height * 1.618);
		setMinimumSize(size);//����������� ������ �� ����� ���� ������ ������������� ��� �������� �������

		confirmButton.addActionListener(new ConfirmButtonClickHandler());
		resetButton.addActionListener(new ResetButtonClickHandler());
	}

	//���������� ���������
	private void createLayout(List<Map.Entry<String, Component>> pairList) {
		int numPairs = pairList.size();

		JPanel panel = new JPanel(new SpringLayout());
		
		setLayout(new SpringLayout());
		for(Map.Entry<String, Component> entry : pairList) {
		    String key = entry.getKey();
		    Component field = entry.getValue();

		    JLabel label = new JLabel(key, JLabel.LEADING);
		    label.setLabelFor(field);
		    panel.add(label);
		    panel.add(field);
		}
	    panel.add(resetButton);
	    panel.add(confirmButton);
		
	    
		SpringUtilities.makeCompactGrid(panel, numPairs + 1, 2, 6, 6, 6, 6);

		panel.setOpaque(true);
        setContentPane(panel);
	}
	

	//������������ ������ ��������� �� ������ ����
	private void setLeftAligmentForSpinners(List<Map.Entry<String, Component>> pairList) {
		int i = 2;
		while (i < pairList.size()) {
			JSpinner spinner = (JSpinner) pairList.get(i++).getValue();
			JComponent editor = spinner.getEditor();
			JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
			spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
		}
	}
	
	
	//��������� ������ ���� ��� ��������� ������
	private void enableSelectionOnClick(List<Map.Entry<String, Component>> pairList) {
		int i = 0;
		while (i < 2) {
			JTextField textField = (JTextField) pairList.get(i++).getValue();
			textField.addFocusListener(new TextFieldSelectOnFocusGainedHandler());
		}
		while (i < pairList.size()) {
			JSpinner spinner = (JSpinner) pairList.get(i++).getValue();
			JComponent editor = spinner.getEditor();
			JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
			spinnerEditor.getTextField().addFocusListener(new SpinnerSelectOnFocusGainedHandler());
		}
	}
	
	//��������� ������ JTextField ��� ��������� ������
	private static class TextFieldSelectOnFocusGainedHandler extends FocusAdapter {
		@Override
		public void focusGained(FocusEvent e) {
			((JTextField) e.getSource()).selectAll();				
		}
	}
	//��������� ������ JSpinner JTextComponent ��� ��������� ������
	private static class SpinnerSelectOnFocusGainedHandler extends FocusAdapter {
		@Override
		public void focusGained(FocusEvent e) {
			JTextComponent textComponent = (JTextComponent) e.getComponent();
			EventQueue.invokeLater( () -> textComponent.selectAll() );						
		}
	}
	
	
	//����������� �������� ����� ������� � ����������/����������� ������
	private boolean saveDetails()
	{
		boolean result = true;
		
		result &= this.<String>Assign((value)->refCar.setBrand(value), brandTextField);
		result &= this.<String>Assign((value)->refCar.setModel(value), modelTextField);
		result &= this.<Short>Assign((value)->refCar.setEnginePower(value), enginePowerSpinner);
		result &= this.<Float>Assign((value)->refCar.setEngineVolume(value), engineVolumeSpinner);
		result &= this.<Short>Assign((value)->refCar.setMaxSpeed(value), maxSpeedSpinner);
		result &= this.<Float>Assign((value)->refCar.setFuelConsumption(value), fuelConsumptionSpinner);
		
		return result;
	}
	
	
	private interface Functor<T> {
	   void execute(T t) throws Exception;
	}
	
	//������ �������� ���� ����� � ��������������� ���� �������
	//���� ������ �� �������, ���� �������������� �������
	private <T> boolean Assign(Functor<String> func, JTextField textField)
	{
		try {
			func.execute(textField.getText().trim());
			return true;
		} catch (Exception e) {
			EventQueue.invokeLater( () -> errorizeField(textField) );
		    return false;
		}
	}
	private <T> boolean Assign(Functor<T> func, JSpinner spinner)
	{
		try {
			func.execute((T) spinner.getValue());
			return true;
		} catch (Exception e) {
			EventQueue.invokeLater( () -> errorizeField(spinner.getEditor().getComponent(0)) );
		    return false;
		}
	}

	
	//������� ������� ������ "�����������"
	private class ConfirmButtonClickHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			if (!saveDetails()) return;
				
			//���� ������ ����� �������, �������� ��������������� ������� � �������� ����
			if (isNewItem) {
				onSaveNewItem.accept(refCar);
				if (resetAfterSave)
					reset();
				else
					setNewItemFlag(false);
			} else {
				onChangeItem.accept(refCar);
			}
		}
	}
	
	
	private void reset()
	{
		show(new Car());
	}
	
	//������� ������� ������ "�����"
	private class ResetButtonClickHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			reset();
		}
	}
	
	
	//������� ������� ���������� ������ ��������
	Consumer<Car> onSaveNewItem = (car) -> {};
	//������� ������� ��������� ������������� ��������
	Consumer<Car> onChangeItem = (car) -> {};
	
	
	//�������� ���� ������� �� ������� ������� Escape
	private static void setEscapeCloseOperation(final JDialog dialog) { 
		final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); 
		final String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING"; 
		
	    Action dispatchClosing = new AbstractAction() { 
	        public void actionPerformed(ActionEvent event) { 
	            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING)); 
	        } 
	    }; 
	    JRootPane root = dialog.getRootPane(); 
	    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, dispatchWindowClosingActionMapKey); 
	    root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing); 
	}
	
		
	//��������� "����������" �������
	private static void errorizeField(Component component) {
		component.setBackground(Color.RED);
		int duration = 3;
		
		class MyActionListener implements ActionListener {
			private Timer timer = new Timer(duration, this);
			
			MyActionListener() { timer.start();}

			@Override
			public void actionPerformed(ActionEvent e) {
				int green = component.getBackground().getGreen();
				int blue = component.getBackground().getBlue();
				try {
					if (!component.getBackground().equals(Color.WHITE)) {
						component.setBackground(new Color(255, green + 1, blue + 1));
					} else {
						timer.stop();
					}
				} catch (IllegalArgumentException exception) {
					timer.stop();
				}
			}
		}
		
		new MyActionListener();
	}
	
	
//	//�������� ��� ������ ������
//	public class WindowWatcher implements WindowListener
//	{
//		@Override
//		public void windowDeactivated(WindowEvent arg0) {
////			dispatchEvent(new WindowEvent(arg0.getWindow(), WindowEvent.WINDOW_CLOSING));
//		}		
//	}
	
}
