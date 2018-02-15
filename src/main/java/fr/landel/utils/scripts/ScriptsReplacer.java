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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fr.landel.utils.assertor.Assertor;
import fr.landel.utils.assertor.AssertorStepCharSequence;

/**
 * Scripts replacer (parse, execute and replace all scripts). Default
 * expressions and operator are configured for scripts like SQL.
 *
 * @since Dec 1, 2015
 * @author Gilles
 *
 */
public class ScriptsReplacer {

    private ScriptsTemplate template;

    /**
     * Constructor
     */
    public ScriptsReplacer() {
        super();

        this.template = ScriptsTemplate.TEMPLATE_SQL;
    }

    /**
     * Constructor
     * 
     * @param template
     *            the template to set
     */
    public ScriptsReplacer(final ScriptsTemplate template) {
        super();

        this.template = template;
    }

    /**
     * @return the template
     */
    public ScriptsTemplate getTemplate() {
        return this.template;
    }

    /**
     * @param template
     *            the template to set
     */
    public void setTemplate(final ScriptsTemplate template) {
        this.template = template;
    }

    /**
     * Replace all keys by their values in the input string.<br>
     * <br>
     * Supported operators: or=||, and=&amp;&amp;, not=!<br>
     * Parenthesis are supported in condition<br>
     * Sub-conditions can be added in value and default<br>
     * <br>
     * Supported format (with replacements: key1=var.name, value1=data,
     * key2=var2, value2=vx):
     * 
     * <pre>
     * - {var.name} will be replaced by "data" without quotes
     * - {var.name??value} will be replaced by "value" without quotes
     * - {var.name::default} will be replaced by "data" without quotes
     * - {var.name&amp;&amp;var2::default} will be replaced by "datavx" without quotes
     * - {var2 &amp;&amp; var.name::default} will be replaced by "vxdata" without quotes
     * - {var2 || !var.name::default} will be replaced by "vx" without quotes
     * - {unknown.var} will be replaced by an empty string
     * - {unknown.var::default} will be replaced by "default" without quotes
     * - {var.name??var='{var.name}'} will be replaced by "var='data'" without quotes
     * - {unknown.var??var='{var.name}'} will be replaced en empty string
     * - {unknown.var??var='{var.name}'::default} will be replaced by "default" without quotes
     * </pre>
     * 
     * @param content
     *            The content to replace
     * @param replacements
     *            the replacements (entry: key=value)
     * @param <V>
     *            The type of values
     * @return the content replaced
     * @throws IllegalArgumentException
     *             If number of brackets open and close doesn't match. If
     *             brackets are found in key replacement or in value
     *             replacement. If replacement value hasn't pairs of single
     *             quote (avoid some SQL injections but not all, parameters have
     *             to be checked)
     */
    public <V> String replace(final String content, final Map<String, V> replacements) throws IllegalArgumentException {
        final StringBuilder builder = new StringBuilder(content);
        this.replace(builder, replacements);
        return builder.toString();
    }

    /**
     * Replace all keys by their values in the input string builder.<br>
     * <br>
     * Supported operators: or=||, and=&amp;&amp;, not=!<br>
     * Parenthesis are supported in condition<br>
     * Sub-conditions can be added in value and default<br>
     * <br>
     * Supported format (with replacements: key1=var.name, value1=data,
     * key2=var2, value2=vx):
     * 
     * <pre>
     * - {var.name} will be replaced by "data" without quotes
     * - {var.name??value} will be replaced by "value" without quotes
     * - {var.name::default} will be replaced by "data" without quotes
     * - {var.name&amp;&amp;var2::default} will be replaced by "datavx" without quotes
     * - {var2 &amp;&amp; var.name::default} will be replaced by "vxdata" without quotes
     * - {var2 || !var.name::default} will be replaced by "vx" without quotes
     * - {unknown.var} will be replaced by an empty string
     * - {unknown.var::default} will be replaced by "default" without quotes
     * - {var.name??var='{var.name}'} will be replaced by "var='data'" without quotes
     * - {unknown.var??var='{var.name}'} will be replaced en empty string
     * - {unknown.var??var='{var.name}'::default} will be replaced by "default" without quotes
     * </pre>
     * 
     * @param sb
     *            The string builder
     * @param replacements
     *            the replacements (entry: key=value)
     * @param <V>
     *            The type of values
     * @throws IllegalArgumentException
     *             If number of brackets open and close doesn't match. If
     *             brackets are found in key replacement or in value
     *             replacement. If replacement value hasn't pairs of single
     *             quote (avoid some SQL injections but not all, parameters have
     *             to be checked)
     */
    public <V> void replace(final StringBuilder sb, final Map<String, V> replacements) throws IllegalArgumentException {
        // first check if the input is valid
        this.checkInput(sb);

        final Map<String, String> replacementsSTR = new HashMap<>();
        for (Entry<String, V> entry : replacements.entrySet()) {
            replacementsSTR.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        // check if replacements ar valid
        this.checkReplacements(replacementsSTR);

        // replace all keys with their variables
        for (Entry<String, String> entry : replacementsSTR.entrySet()) {
            this.replaceSimples(sb, entry.getKey(), entry.getValue());
        }

        // clear all unknown variables
        this.clearUnknownSimples(sb);

        // replace conditions
        this.replaceConditions(sb, replacementsSTR);
    }

    private void checkInput(final StringBuilder sb) throws IllegalArgumentException {
        Assertor.that(sb).isNotEmpty().orElseThrow("Input cannot be empty or null");

        int countBracketOpen = 0;
        int countBracketClose = 0;

        // get first
        int index = sb.indexOf(this.template.getExpressionOpen());
        int indexStop;

        for (; index > -1;) {

            indexStop = sb.indexOf(this.template.getExpressionClose(), index);
            if (indexStop > -1) {
                countBracketClose++;
            }

            // get next
            index = sb.indexOf(this.template.getExpressionOpen(), index + 1);
            if (index > -1) {
                indexStop = sb.indexOf(this.template.getExpressionClose(), index);
            }

            countBracketOpen++;
        }

        Assertor.that(countBracketOpen).isEqual(countBracketClose).orElseThrow("The count of %s doesn't match the count of %s, input: %s",
                this.template.getExpressionOpen(), this.template.getExpressionClose(), sb);
    }

    private void checkReplacements(final Map<String, String> replacements) throws IllegalArgumentException {
        for (Entry<String, String> entry : replacements.entrySet()) {
            final String key = Assertor.that(entry.getKey()).isNotNull().orElseThrow("Replacement key cannot be null");
            final String value = Assertor.that(entry.getValue()).isNotNull().orElseThrow("Replacement value cannot be null");

            final String errorKey = "Replacement key cannot contains: %s";
            AssertorStepCharSequence<String> assertorNot = Assertor.that(key).not();
            assertorNot.contains(this.template.getExpressionOpen()).orElseThrow(errorKey, this.template.getExpressionOpen());
            assertorNot.contains(this.template.getExpressionClose()).orElseThrow(errorKey, this.template.getExpressionClose());
            assertorNot.contains(this.template.getVariableOpen()).orElseThrow(errorKey, this.template.getVariableOpen());
            assertorNot.contains(this.template.getVariableClose()).orElseThrow(errorKey, this.template.getVariableClose());
            assertorNot.contains(this.template.getOperatorThen()).orElseThrow(errorKey, this.template.getOperatorThen());
            assertorNot.contains(this.template.getOperatorElse()).orElseThrow(errorKey, this.template.getOperatorElse());

            final String errorValue = "Replacement value cannot contains: ";
            assertorNot = Assertor.that(value).not();
            assertorNot.contains(this.template.getExpressionOpen()).orElseThrow(errorValue, this.template.getExpressionOpen());
            assertorNot.contains(this.template.getExpressionClose()).orElseThrow(errorValue, this.template.getExpressionClose());
            assertorNot.contains(this.template.getVariableOpen()).orElseThrow(errorValue, this.template.getVariableOpen());
            assertorNot.contains(this.template.getVariableClose()).orElseThrow(errorValue, this.template.getVariableClose());
            assertorNot.contains(this.template.getOperatorThen()).orElseThrow(errorValue, this.template.getOperatorThen());
            assertorNot.contains(this.template.getOperatorElse()).orElseThrow(errorValue, this.template.getOperatorElse());

            if (this.template.getChecker() != null) {
                this.template.getChecker().acceptThrows(value);
            }
        }
    }

    private void replaceSimples(final StringBuilder sb, final String key, final String value) {
        String simple;

        final int valueLen = value.length();
        final int startLen = this.template.getVariableOpen().length();
        final int stopLen = this.template.getVariableClose().length();

        // get first
        int index = sb.indexOf(this.template.getVariableOpen());
        int indexStop = sb.indexOf(this.template.getVariableClose(), index + startLen);

        for (; index > -1 && indexStop > -1;) {
            simple = sb.substring(index + startLen, indexStop);

            if (key.equals(simple.trim())) {
                sb.replace(index, indexStop + stopLen, value);
                index += valueLen;
            }

            // get next
            index = sb.indexOf(this.template.getVariableOpen(), index + startLen);
            if (index > -1) {
                indexStop = sb.indexOf(this.template.getVariableClose(), index + startLen);
            }
        }
    }

    private void clearUnknownSimples(final StringBuilder sb) {
        String simple;
        int indexValid;
        int indexDefault;

        final int startLen = this.template.getVariableOpen().length();
        final int stopLen = this.template.getVariableClose().length();

        // get first
        int index = sb.indexOf(this.template.getVariableOpen());
        int indexStop = sb.indexOf(this.template.getVariableClose(), index + startLen);

        for (; index > -1 && indexStop > -1;) {
            simple = sb.substring(index + startLen, indexStop);

            indexValid = simple.indexOf(this.template.getOperatorThen());
            indexDefault = simple.indexOf(this.template.getOperatorElse());

            if (indexValid == -1 && indexDefault == -1) {
                sb.replace(index, indexStop + stopLen, "");
            }

            // get next
            index = sb.indexOf(this.template.getVariableOpen(), index + 1);
            if (index > -1) {
                indexStop = sb.indexOf(this.template.getVariableClose(), index + startLen);
            }
        }
    }

    private void replaceConditions(final StringBuilder sb, final Map<String, String> replacements) {
        Pair<Integer, Integer> bounds;
        String condition;
        int indexValid;
        int indexDefault;
        String expression;
        String value;
        String defaultValue;

        final int stopLen = this.template.getExpressionClose().length();
        final int operatorLen = this.template.getOperatorThen().length();
        final int elseLen = this.template.getOperatorElse().length();

        for (bounds = this.findDeepestCondition(sb, this.template.getExpressionOpen(),
                this.template.getExpressionClose()); bounds != null; bounds = this.findDeepestCondition(sb,
                        this.template.getExpressionOpen(), this.template.getExpressionClose())) {

            condition = sb.substring(bounds.getLeft(), bounds.getRight());

            indexValid = condition.indexOf(this.template.getOperatorThen());
            indexDefault = condition.indexOf(this.template.getOperatorElse(), indexValid);

            expression = null;
            value = null;
            defaultValue = null;
            if (indexValid > -1) {
                expression = condition.substring(0, indexValid);
                if (indexDefault > -1) {
                    value = condition.substring(indexValid + operatorLen, indexDefault);
                    defaultValue = condition.substring(indexDefault + elseLen);
                } else {
                    value = condition.substring(indexValid + operatorLen);
                }
            } else if (indexDefault > -1) {
                expression = condition.substring(0, indexDefault);
                defaultValue = condition.substring(indexDefault + elseLen);
            }

            if (expression != null) {
                sb.replace(bounds.getLeft() - 1, bounds.getRight() + stopLen,
                        this.replaceCondition(replacements, expression, value, defaultValue));
            } else {
                sb.replace(bounds.getLeft() - 1, bounds.getRight() + stopLen, "");
            }
        }
    }

    private Pair<Integer, Integer> findDeepestCondition(final StringBuilder sb, final String inStr, final String outStr) {
        Pair<Integer, Integer> bounds = null;

        final int inStrLen = inStr.length();

        // get first
        int index = sb.indexOf(inStr);
        int indexStop = sb.indexOf(outStr, index + inStrLen);

        if (index > -1 && indexStop > -1) {
            for (; index > -1 && index < indexStop;) {
                bounds = new MutablePair<Integer, Integer>(index + inStrLen, indexStop);

                // get next
                index = sb.indexOf(inStr, index + inStrLen);
            }
        }

        return bounds;
    }

    private String replaceCondition(final Map<String, String> replacements, final String expression, final String value,
            final String defaultValue) {

        final List<String> varChecker = new ArrayList<>();
        final Set<String> keys = replacements.keySet();

        StringBuilder result = new StringBuilder();

        if (this.checkExpression(expression, keys, varChecker)) {
            if (value != null) {
                result.append(value);
            } else {
                for (String variable : varChecker) {
                    if (variable.indexOf(this.template.getOperatorNot()) == -1) {
                        result.append(replacements.get(variable));
                    }
                }
            }
        } else if (defaultValue != null) {
            result.append(defaultValue);
        }

        return result.toString();
    }

    private boolean checkExpression(final String expression, final Set<String> keys, final List<String> variables) {
        Pair<Integer, Integer> bounds;
        StringBuilder sb = new StringBuilder(expression);
        String condition;
        boolean isOk = false;

        for (bounds = this.findDeepestCondition(sb, this.template.getBlockOpen(),
                this.template.getBlockClose()); bounds != null; bounds = this.findDeepestCondition(sb, this.template.getBlockOpen(),
                        this.template.getBlockClose())) {

            condition = sb.substring(bounds.getLeft(), bounds.getRight());

            isOk = this.checkVariables(keys, variables, condition, isOk);

            sb.replace(bounds.getLeft() - 1, bounds.getRight() + 1, Boolean.toString(isOk));
        }

        return this.checkVariables(keys, variables, sb.toString(), isOk);
    }

    private boolean checkVariables(final Set<String> keys, final List<String> variables, final String condition, final boolean isOk) {
        boolean isOkVar = isOk;
        boolean previousOr = true;
        boolean currentOr = false;
        int previousIndex = 0;
        String variable = null;
        int indexAnd;
        int indexOr;

        final int opAndLen = this.template.getOperatorAnd().length();
        final int opOrLen = this.template.getOperatorOr().length();

        indexAnd = condition.indexOf(this.template.getOperatorAnd());
        indexOr = condition.indexOf(this.template.getOperatorOr());
        if (indexAnd > -1 || indexOr > -1) {
            for (; indexAnd > -1 || indexOr > -1;) {

                if (this.checkOperators(indexAnd, indexOr)) {
                    variable = condition.substring(previousIndex, indexAnd).trim();
                    previousIndex = indexAnd + opAndLen;
                    currentOr = false;
                } else if (this.checkOperators(indexOr, indexAnd)) {
                    variable = condition.substring(previousIndex, indexOr).trim();
                    previousIndex = indexOr + opOrLen;
                    currentOr = true;
                }

                isOkVar = this.add(keys, variable, isOkVar, previousOr, variables);

                indexAnd = condition.indexOf(this.template.getOperatorAnd(), previousIndex);
                indexOr = condition.indexOf(this.template.getOperatorOr(), previousIndex);
                previousOr = currentOr;
            }
            isOkVar = this.add(keys, condition.substring(previousIndex).trim(), isOkVar, previousOr, variables);
        } else {
            isOkVar = this.add(keys, condition.trim(), isOkVar, previousOr, variables);
        }

        return isOkVar;
    }

    private boolean checkOperators(final int indexOperator1, final int indexOperator2) {
        return indexOperator1 > -1 && (indexOperator2 == -1 || indexOperator1 < indexOperator2);
    }

    private boolean add(final Set<String> keys, final String variable, final boolean isOk, final boolean previousOr,
            final List<String> variables) {
        boolean is = false;

        if (variable != null) {
            if (Boolean.TRUE.toString().equals(variable)) {
                is = true;
            } else if (!Boolean.FALSE.toString().equals(variable)) {
                boolean isNot = variable.indexOf(this.template.getOperatorNot()) == 0;
                if (!isNot && keys.contains(variable)) {
                    variables.add(variable);
                    is = true;
                } else if (isNot) {
                    is = !keys.contains(variable.substring(this.template.getOperatorNot().length()).trim());
                }
            }
        }

        if (previousOr) {
            return isOk || is;
        }
        return isOk && is;
    }
}
