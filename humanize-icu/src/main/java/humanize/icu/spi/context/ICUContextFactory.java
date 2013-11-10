package humanize.icu.spi.context;

import humanize.spi.context.Context;
import humanize.spi.context.ContextFactory;

/**
 * Default implementation of {@link ContextFactory}. Creates
 * {@link DefaultICUContext} instances.
 * 
 * @author michaux
 * 
 */
public class ICUContextFactory implements ContextFactory
{

    @Override
    public Context createContext()
    {

        return new DefaultICUContext();
    }

}
