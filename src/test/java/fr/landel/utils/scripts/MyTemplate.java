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
        this.setExpressionClose("£££");
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
