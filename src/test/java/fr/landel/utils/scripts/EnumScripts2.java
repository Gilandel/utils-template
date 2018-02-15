/*
 * #%L
 * utils-scripts
 * %%
 * Copyright (C) 2016 - 2018 Gilles Landel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
