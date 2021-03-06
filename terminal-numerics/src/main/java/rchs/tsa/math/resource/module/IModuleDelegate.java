package rchs.tsa.math.resource.module;

import java.io.IOException;
import java.net.URL;

import net.anasa.util.Checks;
import rchs.tsa.math.TerminalNumerics;
import rchs.tsa.math.io.xml.XmlLayoutLoader;
import rchs.tsa.math.resource.app.IApp;
import rchs.tsa.math.resource.module.context.ActionRegister.IComponentAction;
import rchs.tsa.math.resource.module.context.IComponentEntry;
import rchs.tsa.math.resource.module.context.ModuleContext;

public interface IModuleDelegate
{
	default void init() throws Exception
	{
		
	}
	
	default ModuleContext getContext()
	{
		return TerminalNumerics.getContext();
	}
	
	default void addComponent(String id, IComponentEntry entry)
	{
		getContext().addComponent(id, entry);
	}
	
	default void addComponent(String id, URL layoutURL) throws IOException
	{
		Checks.checkNotNull(layoutURL, new IOException("layout file URL must not be null"));
		
		getContext().addComponent(id, (props) -> new XmlLayoutLoader().load(layoutURL.openStream()).compile(getContext()));
	}
	
	default void addAction(String id, IComponentAction action)
	{
		getContext().addAction(id, action);
	}
	
	default void addModule(IModule module)
	{
		getContext().addModule(module);
	}
	
	default void addApp(IApp app)
	{
		getContext().addApp(app);
	}
}
