/*
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
import java.nio.charset.StandardCharsets;

/**
 * Enumeration of scripts
 *
 * @since Dec 1, 2015
 * @author Gilles
 *
 */
public enum EnumScripts2 implements ScriptsList<EnumScripts2> {

    /**
     * The TST test file (for test on loader with Unix line character)
     */
    TEST_UNIX("test_unix.tst"),

    /**
     * The TST test file (for test on loader with Mac line character)
     */
    TEST_MAC("test_mac.tst");

    private String name;
    private Charset charset;

    /**
     * Constructor
     *
     * @param name
     *            The file name
     * @param charset
     *            The file charset
     */
    EnumScripts2(final String name, final Charset charset) {
        this.name = name;
        this.charset = charset;
    }

    /**
     * Constructor
     *
     * @param name
     *            The file name
     */
    EnumScripts2(final String name) {
        this(name, StandardCharsets.UTF_8);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public EnumScripts2[] getValues() {
        return EnumScripts2.values();
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }
}
