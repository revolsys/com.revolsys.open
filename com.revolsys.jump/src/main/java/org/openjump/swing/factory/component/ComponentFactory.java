package org.openjump.swing.factory.component;

import java.awt.Component;
import javax.swing.Icon;

public interface ComponentFactory<T extends Component>
{

    public abstract T createComponent();

    public abstract String getName();

    public abstract Icon getIcon();

    public abstract String getToolTip();

    public abstract void close(Component component);
}
