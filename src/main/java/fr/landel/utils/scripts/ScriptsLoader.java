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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import fr.landel.utils.assertor.Assertor;
import fr.landel.utils.commons.EnumChar;
import fr.landel.utils.commons.StringUtils;
import fr.landel.utils.io.FileUtils;
import fr.landel.utils.io.StreamUtils;
import fr.landel.utils.io.SystemProperties;

/**
 * Scripts loader (load scripts from classpath, and remove comments and blank
 * lines)
 *
 * @since Dec 1, 2015
 * @author Gilles
 *
 */
public class ScriptsLoader {

    /**
     * Scripts path
     */
    private static final String DEFAULT_PATH = "scripts/";

    private static final String NEW_LINE = EnumChar.LF.getUnicode();
    private static final String LINE_RETURN = EnumChar.CR.getUnicode();

    private final Map<ScriptsList<?>, StringBuilder> scripts;
    private final ScriptsReplacer replacer;

    private String path;

    /**
     * Constructor (default path: "scripts/", default template: SQL)
     */
    public ScriptsLoader() {
        this(DEFAULT_PATH);
    }

    /**
     * Constructor (default template: SQL)
     * 
     * @param path
     *            The base path for all scripts
     */
    public ScriptsLoader(final String path) {
        this(path, ScriptsTemplate.TEMPLATE_SQL);
    }

    /**
     * Constructor
     * 
     * @param path
     *            The base path for all scripts
     * @param template
     *            The template
     */
    public ScriptsLoader(final String path, final ScriptsTemplate template) {
        super();

        this.scripts = new HashMap<>();
        this.replacer = new ScriptsReplacer();
        this.replacer.setTemplate(template);
        this.setPath(path);
    }

    /**
     * @return the replacer
     */
    public ScriptsReplacer getReplacer() {
        return this.replacer;
    }

    /**
     * @param path
     *            The base path (default path: scripts/)
     */
    public void setPath(final String path) {
        Assertor.that(path).isNotEmpty().orElseThrow("Scripts path cannot be null or empty");

        String suffix = SystemProperties.FILE_SEPARATOR.getValue();
        if (path.endsWith(suffix)) {
            suffix = "";
        }
        this.path = path + suffix;
    }

    /**
     * Load all scripts from classpath if {@code loader} is not {@code null},
     * otherwise from system folder. All scripts are loaded from the defined
     * directory (by default 'scripts', can be override by:
     * {@link #setPath(String)}).
     * 
     * @param loader
     *            The current class loader (may be {@code null})
     * @param scriptsList
     *            The scripts list
     * @throws IOException
     *             On loading file failures
     */
    public void init(final ClassLoader loader, final ScriptsList<?>... scriptsList) throws IOException {
        for (ScriptsList<?> value : scriptsList) {
            if (StringUtils.isNotBlank(value.getName())) {
                final StringBuilder sb = new StringBuilder();
                this.scripts.put(value, sb);

                final String path = new StringBuilder(this.path).append(value.getName()).toString();
                try (final InputStream is = loader != null ? loader.getResourceAsStream(path)
                        : StreamUtils.createBufferedInputStream(path)) {
                    sb.append(FileUtils.getFileContent(is, value.getCharset()));
                }
            }
        }
    }

    /**
     * Load all scripts from classpath. All scripts are loaded from the defined
     * directory (by default 'scripts', can be override by:
     * {@link #setPath(String)}).
     * 
     * @param scriptsList
     *            The scripts list
     * @throws IOException
     *             On loading file failures
     */
    public void init(final ScriptsList<?>... scriptsList) throws IOException {
        this.init(ScriptsLoader.class.getClassLoader(), scriptsList);
    }

    /**
     * Load a single script from classpath. The script is loaded from the
     * defined directory (by default 'scripts', can be override by:
     * {@link #setPath(String)}).
     * 
     * @param name
     *            The script name
     * @param charset
     *            The script charset
     * @return The script identifier
     * @throws IOException
     *             On loading file failures
     */
    public ScriptsList<?> init(final String name, final Charset charset) throws IOException {
        return this.init(ScriptsLoader.class.getClassLoader(), name, charset);
    }

    /**
     * Load a single script from classpath if {@code loader} is not
     * {@code null}, otherwise from system folder. The script is loaded from the
     * defined directory (by default 'scripts', can be override by:
     * {@link #setPath(String)}).
     * 
     * @param loader
     *            The current class loader (may be {@code null})
     * @param name
     *            The script name
     * @param charset
     *            The script charset
     * @return The script identifier
     * @throws IOException
     *             On loading file failures
     */
    public ScriptsList<?> init(final ClassLoader loader, final String name, final Charset charset) throws IOException {
        final ScriptsList<?> script = new SingleScriptsList(name, charset);
        this.init(loader, script);
        return script;
    }

    /**
     * Get the scripts file
     * 
     * @param path
     *            The scripts path
     * @param <E>
     *            The type of script list
     * @return The StringBuilder
     */
    public <E extends ScriptsList<E>> StringBuilder get(final ScriptsList<E> path) {
        return this.get(path, new HashMap<String, String>());
    }

    /**
     * Get the scripts file
     * 
     * @param path
     *            The scripts path
     * @param key
     *            The key to replace
     * @param value
     *            The replacement value
     * @param <E>
     *            The type of script list
     * @param <V>
     *            The type of replacement value
     * @return The StringBuilder
     */
    public <E extends ScriptsList<E>, V> StringBuilder get(final ScriptsList<E> path, final String key, final V value) {
        if (key != null) {
            final Map<String, V> replacements = new HashMap<>();
            replacements.put(key, value);

            return this.get(path, replacements);
        }
        return null;
    }

    /**
     * Get the scripts file
     * 
     * @param path
     *            The scripts path
     * @param replacements
     *            The entries (keys to replace, replacement values)
     * @param <E>
     *            The type of script list
     * @param <V>
     *            The type of replacement values
     * @return The StringBuilder
     */
    public <E extends ScriptsList<E>, V> StringBuilder get(final ScriptsList<E> path, final Map<String, V> replacements) {
        if (this.scripts.containsKey(path)) {
            final StringBuilder builder = new StringBuilder(this.scripts.get(path));

            if (this.replacer.getTemplate().isRemoveComments()) {
                this.removeComments(builder);
            }

            this.replacer.replace(builder, replacements);

            if (this.replacer.getTemplate().isRemoveBlankLines()) {
                this.removeBlankLines(builder);
            }

            return builder;
        }
        return null;
    }

    private void removeComments(final StringBuilder builder) {
        int startComments = 0;
        int endComments = 0;

        // removes multi-lines comments
        while ((startComments = builder.indexOf(this.replacer.getTemplate().getMultiLineCommentOperatorOpen())) > -1) {
            endComments = builder.indexOf(this.replacer.getTemplate().getMultiLineCommentOperatorClose(), startComments);
            if (endComments > startComments) {
                builder.delete(startComments, endComments + 2);
            } else {
                builder.delete(startComments, builder.length());
            }
        }

        // removes line comments
        while ((startComments = builder.indexOf(this.replacer.getTemplate().getOneLineCommentOperator())) > -1) {
            // For Windows / Mac / Unix
            int cr = builder.indexOf(NEW_LINE, startComments);
            int lf = builder.indexOf(LINE_RETURN, startComments);
            if (cr > -1 && lf > -1) {
                endComments = Math.min(cr, lf);
            } else if (cr > -1) {
                endComments = cr;
            } else if (lf > -1) {
                endComments = lf;
            } else {
                endComments = builder.length();
            }
            if (endComments > startComments) {
                builder.delete(startComments, endComments);
            }
        }
    }

    private void removeBlankLines(final StringBuilder builder) {
        int lineSeparator = 0;
        int startLine = 0;
        int endLine = 0;

        final String newLine;

        boolean hasNewLine = builder.indexOf(NEW_LINE) > -1;
        boolean hasLineReturn = builder.indexOf(LINE_RETURN) > -1;

        if (hasNewLine && hasLineReturn) {
            newLine = NEW_LINE;

            // removes return character to simplify the remove of blank lines
            while ((lineSeparator = builder.indexOf(LINE_RETURN)) > -1) {
                builder.delete(lineSeparator, lineSeparator + 1);
            }
        } else if (hasLineReturn) {
            newLine = LINE_RETURN;
        } else {
            newLine = NEW_LINE;
        }

        // removes blank lines
        while ((endLine = builder.indexOf(newLine, startLine)) > -1) {
            if (endLine >= startLine && StringUtils.isBlank(builder.substring(startLine, endLine))) {
                builder.delete(startLine, endLine + 1);
            } else {
                startLine = endLine + 1;
            }
        }

        // remove the last new line character
        int length = builder.length();
        if (length > 0 && builder.lastIndexOf(newLine) == length - 1) {
            builder.delete(length - 1, length);
        }
    }
}
