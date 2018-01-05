/*-
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

import fr.landel.utils.assertor.Assertor;
import fr.landel.utils.commons.StringUtils;
import fr.landel.utils.commons.function.ConsumerThrowable;

/**
 * Script templates
 *
 * @since Dec 21, 2016
 * @author Gilles
 *
 */
public interface ScriptsTemplate {

    /**
     * The SQL comment tag (one line)
     */
    String COMMENT_SQL = "--";

    /**
     * The standard comment tag (one line)
     */
    String COMMENT_STANDARD = "//";

    /**
     * The open comment tag (multi-lines)
     */
    String COMMENT_OPEN = "/*";

    /**
     * The close comment tag (multi-lines)
     */
    String COMMENT_CLOSE = "*/";

    /**
     * The open expression tag (use for expressions and variables)
     */
    String EXPRESSION_OPEN = "{";

    /**
     * The close expression tag (use for expressions and variables)
     */
    String EXPRESSION_CLOSE = "}";

    /**
     * The open block in expression
     * 
     * <pre>
     * ex: {(a &amp;&amp; b) || (b &amp;&amp; c) ?? ...}
     * </pre>
     */
    String BLOCK_OPEN = "(";

    /**
     * The close block in expression
     * 
     * <pre>
     * ex: {(a &amp;&amp; b) || (b &amp;&amp; c) ?? ...}
     * </pre>
     */
    String BLOCK_CLOSE = ")";

    /**
     * The THEN operator
     */
    String OPERATOR_THEN = "??";

    /**
     * The ELSE operator
     */
    String OPERATOR_ELSE = "::";

    /**
     * The AND operator
     */
    String OPERATOR_AND = "&&";

    /**
     * The OR operator
     */
    String OPERATOR_OR = "||";

    /**
     * The NOT operator
     */
    String OPERATOR_NOT = "!";

    /**
     * Double quotes character
     */
    String DOUBLE_QUOTE = "\"";

    /**
     * Single quote character
     */
    String SINGLE_QUOTE = "'";

    /**
     * Throwable consumer to check variable value in SQL scripts. If the value
     * contains single quote alone (not by pair) an
     * {@link IllegalArgumentException} is thrown.
     */
    ConsumerThrowable<String, IllegalArgumentException> CHECKER_SQL = (v) -> {
        // Avoid some SQL injections but not all!, parameters has to be
        // checked before
        Assertor.that(StringUtils.countMatches(v, SINGLE_QUOTE) % 2).isEqual(0)
                .orElseThrow("Replacement value has to contain only pairs of: " + SINGLE_QUOTE);
        Assertor.that(StringUtils.countMatches(StringUtils.replace(v, SINGLE_QUOTE + SINGLE_QUOTE, ""), SINGLE_QUOTE)).isEqual(0)
                .orElseThrow("Replacement value has to contain only group of pairs of: " + SINGLE_QUOTE);
    };

    /**
     * Throwable consumer to check variable value in JSON scripts (like
     * ElasticSearch queries). If the value contains braces an
     * {@link IllegalArgumentException} is thrown.
     */
    ConsumerThrowable<String, IllegalArgumentException> CHECKER_JSON = (v) -> {
        // Avoid some JSON injections but not all!, parameters has to be
        // checked before
        Assertor.that(v).not().contains(EXPRESSION_OPEN).and().not().contains(EXPRESSION_CLOSE)
                .orElseThrow("Replacement value hasn't to contain braces");
    };

    /**
     * Template for SQL scripts
     * 
     * <pre>
     * -- comments
     * /* multi-line comments *&#47;
     * {(a &amp;&amp; b) || (b &amp;&amp; c) ?? ... {variable} }
     * </pre>
     */
    ScriptsTemplate TEMPLATE_SQL = new AbstractScriptsTemplate() {
        @Override
        protected void init() {
            this.setExpressionOpen(EXPRESSION_OPEN);
            this.setExpressionClose(EXPRESSION_CLOSE);
            this.setBlockOpen(BLOCK_OPEN);
            this.setBlockClose(BLOCK_CLOSE);
            this.setOperatorThen(OPERATOR_THEN);
            this.setOperatorElse(OPERATOR_ELSE);
            this.setOperatorAnd(OPERATOR_AND);
            this.setOperatorOr(OPERATOR_OR);
            this.setOperatorNot(OPERATOR_NOT);

            this.setRemoveComments(Boolean.TRUE);
            this.setRemoveBlankLines(Boolean.TRUE);

            this.setOneLineCommentOperator(COMMENT_SQL);
            this.setMultiLineCommentOperators(COMMENT_OPEN, COMMENT_CLOSE);

            this.setChecker(CHECKER_SQL);
        }
    };

    /**
     * Template for JSON scripts (example for ElasticSearch requests)
     * 
     * <pre>
     * // comments
     * /* multi-line comments *&#47;
     * &lt;(a &amp;&amp; b) || (b &amp;&amp; c) ?? ... &lt;variable&gt; &gt;
     * </pre>
     */
    ScriptsTemplate TEMPLATE_JSON = new AbstractScriptsTemplate() {
        @Override
        protected void init() {
            this.setExpressionOpen("<");
            this.setExpressionClose(">");
            this.setBlockOpen(BLOCK_OPEN);
            this.setBlockClose(BLOCK_CLOSE);
            this.setOperatorThen(OPERATOR_THEN);
            this.setOperatorElse(OPERATOR_ELSE);
            this.setOperatorAnd(OPERATOR_AND);
            this.setOperatorOr(OPERATOR_OR);
            this.setOperatorNot(OPERATOR_NOT);

            this.setRemoveComments(Boolean.TRUE);
            this.setRemoveBlankLines(Boolean.TRUE);

            this.setOneLineCommentOperator(COMMENT_STANDARD);
            this.setMultiLineCommentOperators(COMMENT_OPEN, COMMENT_CLOSE);

            this.setChecker(CHECKER_JSON);
        }
    };

    /**
     * @return the removeComments
     */
    boolean isRemoveComments();

    /**
     * @return the removeBlankLines
     */
    boolean isRemoveBlankLines();

    /**
     * @return the expressionOpen
     */
    String getExpressionOpen();

    /**
     * @return the expressionClose
     */
    String getExpressionClose();

    /**
     * @return the blockOpen
     */
    String getBlockOpen();

    /**
     * @return the operatorThen
     */
    String getOperatorThen();

    /**
     * @return the operatorElse
     */
    String getOperatorElse();

    /**
     * @return the operatorAnd
     */
    String getOperatorAnd();

    /**
     * @return the operatorOr
     */
    String getOperatorOr();

    /**
     * @return the operatorNot
     */
    String getOperatorNot();

    /**
     * @return the multiLineCommentOperatorOpen
     */
    String getMultiLineCommentOperatorOpen();

    /**
     * @return the multiLineCommentOperatorClose
     */
    String getMultiLineCommentOperatorClose();

    /**
     * @return the oneLineCommentOperator
     */
    String getOneLineCommentOperator();

    /**
     * @return the checker
     */
    ConsumerThrowable<String, IllegalArgumentException> getChecker();

    /**
     * @return the blockClose
     */
    String getBlockClose();
}
