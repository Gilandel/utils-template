/*-
 * #%L
 * utils-scripts
 * %%
 * Copyright (C) 2016 - 2017 Gilles Landel
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

import fr.landel.utils.commons.function.ConsumerThrowable;

/**
 * Scripts template base
 *
 * @since Jun 29, 2016
 * @author Gilles
 *
 */
public abstract class AbstractScriptsTemplate implements ScriptsTemplate {

    private String expressionOpen;
    private String expressionClose;
    private String blockOpen;
    private String blockClose;
    private String operatorThen;
    private String operatorElse;
    private String operatorAnd;
    private String operatorOr;
    private String operatorNot;
    private boolean removeComments;
    private boolean removeBlankLines;
    private String oneLineCommentOperator;
    private String multiLineCommentOperatorOpen;
    private String multiLineCommentOperatorClose;
    private ConsumerThrowable<String, IllegalArgumentException> checker;

    /**
     * Constructor
     */
    protected AbstractScriptsTemplate() {
        this.init();
    }

    /**
     * Template initializer
     */
    protected abstract void init();

    @Override
    public boolean isRemoveComments() {
        return this.removeComments;
    }

    @Override
    public boolean isRemoveBlankLines() {
        return this.removeBlankLines;
    }

    @Override
    public String getExpressionOpen() {
        return this.expressionOpen;
    }

    @Override
    public String getExpressionClose() {
        return this.expressionClose;
    }

    @Override
    public String getBlockOpen() {
        return this.blockOpen;
    }

    @Override
    public String getOperatorThen() {
        return this.operatorThen;
    }

    @Override
    public String getOperatorElse() {
        return this.operatorElse;
    }

    @Override
    public String getOperatorAnd() {
        return this.operatorAnd;
    }

    @Override
    public String getOperatorOr() {
        return this.operatorOr;
    }

    @Override
    public String getOperatorNot() {
        return this.operatorNot;
    }

    @Override
    public String getMultiLineCommentOperatorOpen() {
        return this.multiLineCommentOperatorOpen;
    }

    @Override
    public String getMultiLineCommentOperatorClose() {
        return this.multiLineCommentOperatorClose;
    }

    @Override
    public String getOneLineCommentOperator() {
        return this.oneLineCommentOperator;
    }

    @Override
    public ConsumerThrowable<String, IllegalArgumentException> getChecker() {
        return this.checker;
    }

    @Override
    public String getBlockClose() {
        return this.blockClose;
    }

    /**
     * @param oneLineCommentOperator
     *            the oneLineCommentOperator to set (default: "--")
     */
    protected void setOneLineCommentOperator(final String oneLineCommentOperator) {
        this.oneLineCommentOperator = oneLineCommentOperator;
    }

    /**
     * The multi-line comment operator
     * 
     * @param multiLineCommentOperatorOpen
     *            the multi-line open comment operator (default: "/*")
     * @param multiLineCommentOperatorClose
     *            the multi-line close comment operator (default: "&#42;/")
     */
    protected void setMultiLineCommentOperators(final String multiLineCommentOperatorOpen, final String multiLineCommentOperatorClose) {
        this.multiLineCommentOperatorOpen = multiLineCommentOperatorOpen;
        this.multiLineCommentOperatorClose = multiLineCommentOperatorClose;
    }

    /**
     * @param expressionOpen
     *            the expressionOpen to set
     */
    protected void setExpressionOpen(final String expressionOpen) {
        this.expressionOpen = expressionOpen;
    }

    /**
     * @param expressionClose
     *            the expressionClose to set
     */
    protected void setExpressionClose(final String expressionClose) {
        this.expressionClose = expressionClose;
    }

    /**
     * @param blockOpen
     *            the blockOpen to set
     */
    protected void setBlockOpen(final String blockOpen) {
        this.blockOpen = blockOpen;
    }

    /**
     * @param blockClose
     *            the blockClose to set
     */
    protected void setBlockClose(final String blockClose) {
        this.blockClose = blockClose;
    }

    /**
     * @param operatorThen
     *            the operatorThen to set
     */
    protected void setOperatorThen(final String operatorThen) {
        this.operatorThen = operatorThen;
    }

    /**
     * @param operatorElse
     *            the operatorElse to set
     */
    protected void setOperatorElse(final String operatorElse) {
        this.operatorElse = operatorElse;
    }

    /**
     * @param operatorAnd
     *            the operatorAnd to set
     */
    protected void setOperatorAnd(final String operatorAnd) {
        this.operatorAnd = operatorAnd;
    }

    /**
     * @param operatorOr
     *            the operatorOr to set
     */
    protected void setOperatorOr(final String operatorOr) {
        this.operatorOr = operatorOr;
    }

    /**
     * @param operatorNot
     *            the operatorNot to set
     */
    protected void setOperatorNot(final String operatorNot) {
        this.operatorNot = operatorNot;
    }

    /**
     * @param removeComments
     *            the removeComments to set
     */
    protected void setRemoveComments(final boolean removeComments) {
        this.removeComments = removeComments;
    }

    /**
     * @param removeBlankLines
     *            the removeBlankLines to set
     */
    protected void setRemoveBlankLines(final boolean removeBlankLines) {
        this.removeBlankLines = removeBlankLines;
    }

    /**
     * @param checker
     *            the checker to set
     */
    protected void setChecker(final ConsumerThrowable<String, IllegalArgumentException> checker) {
        this.checker = checker;
    }
}
