import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class SearchBox extends JPanel {
	
	private HintTextField textField = new HintTextField("поиск");
//	private JButton searchButton = new JButton("Ќайти");


	SearchBox() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(textField, BorderLayout.CENTER);
		textField.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.white));
		textField.setFont(new Font("Verdana", Font.PLAIN, 16));	
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(150, 150, 150)));
		
		setDocumentListener();
		setKeyListener();
	}
	
	//—обытие изменени€ текста строки поиска
	private void setDocumentListener()
	{
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFieldState();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFieldState();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFieldState();
			}
			protected void updateFieldState() {
				textChangedEventHandler.accept(textField.getText());
			}
		});
	}
	
	//—обытие нажати€ клавиши Escape
	private void setKeyListener()
	{
		textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
    			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
    				textField.setText(""); 
            }
        });
	}
	
	
	String getText() { return textField.getText(); }

	
	//делегат, вызываемый при изменении текста строки поиска
	Consumer<String> textChangedEventHandler = (text) -> {};
}


//TextField с подсказкой
class HintTextField extends JTextField {

	private String hint;//подсказка
	
	public HintTextField(String hint) {
		this.hint = hint;
	}
	
	public void setHint(String hint) {
		this.hint = hint;
	}
	
	public String getHint() {
		return hint;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (getText().isEmpty()) {
			int height = getHeight();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,	RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			Insets insets = getInsets();
			FontMetrics fm = g.getFontMetrics();
			int bgcolor = getBackground().getRGB();
			int fgcolor = getForeground().getRGB();
			int mask = 0xfefefefe;
			int color = ((bgcolor & mask) >>> 1) + ((fgcolor & mask) >>> 1);
			g.setColor(new Color(color, true));
			g.drawString(hint, insets.left, height / 2 + fm.getAscent() / 2 - 2);
		}
	}

}
