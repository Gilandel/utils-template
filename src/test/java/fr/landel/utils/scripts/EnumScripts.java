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
public enum EnumScripts implements ScriptsList<EnumScripts> {

    /**
     * The SQL test file (for test on loader)
     */
    TEST("test.sql"),

    /**
     * The SQL test file (for test on loader with one line)
     */
    TEST_ONE_LINE("test_one_line.sql"),

    /**
     * Select patient search by sector or unit (count and paginated select)
     */
    PATIENTS_SEARCH("patientsSearch.sql"),

    /**
     * Select bikes by engine and tire type
     */
    BIKES("bikes.sql"),

    /**
     * Select patient search by sector or unit (count and paginated select)
     */
    INDEX_AGGS("index.elastic", StandardCharsets.UTF_8);

    private final String name;
    private final Charset charset;

    /**
     * 
     * Constructor
     *
     * @param name
     *            The file name
     * @param charset
     *            The file charset
     */
    EnumScripts(final String name, final Charset charset) {
        this.name = name;
        this.charset = charset;
    }

    /**
     * 
     * Constructor
     *
     * @param name
     *            The file name
     */
    EnumScripts(final String name) {
        this(name, StandardCharsets.UTF_8);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public EnumScripts[] getValues() {
        return EnumScripts.values();
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }
}
