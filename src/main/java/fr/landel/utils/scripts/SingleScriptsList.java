/*-
 * #%L
 * utils-scripts
 * %%
 * Copyright (C) 2016 - 2017 Gilandel
 * %%
 * Authors: Gilles Landel
 * URL: https://github.com/Gilandel
 * 
 * This file is under Apache License, version 2.0 (2004).
 * #L%
 */
package fr.landel.utils.scripts;

import java.nio.charset.Charset;

/**
 * This class allows to load a single script without creating an enumeration of
 * scripts.
 *
 * @since Feb 27, 2017
 * @author Gilles
 *
 */
public class SingleScriptsList implements ScriptsList<SingleScriptsList> {

    private final String name;
    private final Charset charset;

    /**
     * Constructor
     *
     * @param name
     *            the script name
     * @param charset
     *            the file charset
     */
    public SingleScriptsList(final String name, final Charset charset) {
        this.name = name;
        this.charset = charset;
    }

    @Override
    public SingleScriptsList[] getValues() {
        return new SingleScriptsList[] {this};
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }
}
