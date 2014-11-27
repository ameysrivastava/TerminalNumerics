package net.anasa.util.ui;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public interface ISwingComponent extends IComponent
{
	@Override
	public JComponent getHandle();
	
	public default void setBorder(int x, int y)
	{
		setBorder(BorderFactory.createEmptyBorder(y, x, y, x));
	}
	
	public default void setBorder(String text)
	{
		setBorder(text, new Font(Font.DIALOG, Font.PLAIN, 11));
	}
	
	public default void setBorder(String text, Font font)
	{
		setBorder(BorderFactory.createTitledBorder(null, text, TitledBorder.CENTER, TitledBorder.TOP, font));
	}
	
	public default void setBorder(Border border)
	{
		getHandle().setBorder(border);
	}
	
	public default void setToolTipText(String text)
	{
		getHandle().setToolTipText(text);
	}
	
	public default void setVisible(boolean visible)
	{
		getHandle().setVisible(visible);
	}
}
