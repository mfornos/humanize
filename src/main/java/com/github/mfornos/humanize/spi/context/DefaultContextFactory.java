package com.github.mfornos.humanize.spi.context;

/**
 * Default implementation of {@link ContextFactory}. Creates
 * {@link DefaultContext} instances.
 * 
 * @author michaux
 * 
 */
public class DefaultContextFactory implements ContextFactory {

	@Override
	public Context createContext() {

		return new DefaultContext();
	}

}
