import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

//статусбар - панель с Flow лейаутом
public class StatusBar extends JPanel
{
	StatusBar()
	{
		super();
		setLayout (new FlowLayout(FlowLayout.LEFT));
		setBackground(new Color(231, 227, 227));	
		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(150, 150, 150)));
	}
}