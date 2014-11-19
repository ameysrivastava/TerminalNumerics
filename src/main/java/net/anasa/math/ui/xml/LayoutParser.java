package net.anasa.math.ui.xml;

import java.io.InputStream;

import net.anasa.math.ui.xml.builder.AbstractBuilder;
import net.anasa.math.ui.xml.builder.ILayoutBuilder;
import net.anasa.util.Checks;
import net.anasa.util.Listing;
import net.anasa.util.StringHelper;
import net.anasa.util.data.DataConform.FormatException;
import net.anasa.util.data.properties.Properties;
import net.anasa.util.data.xml.XmlAttribute;
import net.anasa.util.data.xml.XmlElement;
import net.anasa.util.data.xml.XmlFile;
import net.anasa.util.data.xml.XmlFile.XmlException;

public class LayoutParser
{
	private final Listing<ILayoutBuilder> builders = new Listing<>();
	
	public LayoutParser()
	{
		add(new AbstractBuilder("component", (element) -> new ComponentNode(element.getIDAttribute(), getProps(element))));
		add(new AbstractBuilder("layout", (element) -> {
			LayoutType type = LayoutType.valueOf(StringHelper.upperCase(element.getAttribute("type")));
			Checks.checkNotNull(type, new FormatException("Invalid layout type: " + element.getAttribute("type")));
			LayoutNode node = new LayoutNode(type.getLayout(getProps(element)));
			for(XmlElement child : element.getElements())
			{
				node.add(getFrom(child), child.getAttribute("pos"));
			}
			return node;
		}));
	}
	
	private void add(ILayoutBuilder builder)
	{
		getBuilders().add(builder);
	}
	
	public Listing<ILayoutBuilder> getBuilders()
	{
		return builders;
	}
	
	protected ILayoutNode getFrom(XmlElement element) throws FormatException
	{
		ILayoutBuilder builder = getBuilders().getFirst((parse) -> parse.isValid(element));
		Checks.checkNotNull(builder, new FormatException("Could not find builder for element: " + element.getName()));
		
		return builder.getFrom(element);
	}
	
	public ILayoutNode getFrom(InputStream stream) throws FormatException
	{
		try
		{
			XmlFile xml = XmlFile.read(stream);
			
			return getFrom(xml.getBaseElement());
		}
		catch(XmlException e)
		{
			throw new FormatException(e);
		}
	}
	
	private Properties getProps(XmlElement element)
	{
		return getProps(element, new Properties());
	}
	
	private Properties getProps(XmlElement element, Properties parent)
	{
		Properties props = new Properties();
		
		for(XmlAttribute attribute : element.getAttributes())
		{
			props.set(attribute.getKey(), attribute.getValue());
		}
		
		for(XmlElement child : element.getElements())
		{
			if(child.hasIDAttribute())
			{
				getProps(child, parent.getInner(child.getIDAttribute()));
			}
		}
		
		return props;
	}
}
