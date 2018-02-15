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

/**
 * The list of scripts loaded
 *
 * @since Dec 1, 2015
 * @author Gilles
 * 
 * @param <E>
 *            The extended type
 */
public interface ScriptsList<E extends ScriptsList<E>> {

    /**
     * @return The complete list of elements
     */
    E[] getValues();

    /**
     * @return The element name
     */
    String getName();

    /**
     * @return The charset
     */
    Charset getCharset();
}
