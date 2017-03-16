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

import fr.landel.utils.assertor.Assertor;

/**
 * Test template
 *
 * @since Aug 9, 2016
 * @author Gilles
 *
 */
public class MyTemplate extends AbstractScriptsTemplate {

    /*
     * @see AbstractScriptsTemplate#init()
     */
    @Override
    protected void init() {
        this.setExpressionOpen("$");
        this.setExpressionClose("£");
        this.setBlockOpen("\\");
        this.setBlockClose("/");
        this.setOperatorThen("THEN");
        this.setOperatorElse("ELSE");
        this.setOperatorAnd("AND");
        this.setOperatorOr("OR");
        this.setOperatorNot("NOT");

        this.setRemoveComments(Boolean.TRUE);
        this.setRemoveBlankLines(Boolean.TRUE);

        this.setOneLineCommentOperator("#");
        this.setMultiLineCommentOperators("#_", "_#");

        this.setChecker((input) -> {
            Assertor.that(input).not().contains('=').orElseThrow("the script cannot contains the '=' character");
        });
    }
}
