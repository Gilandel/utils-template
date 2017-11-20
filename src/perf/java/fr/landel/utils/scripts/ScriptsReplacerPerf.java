/*
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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import fr.landel.utils.microbenchmark.AbstractMicrobenchmark;

/**
 * Check {@link ScriptsReplacer} performance
 *
 * @since Mar 18, 2017
 * @author Gilles
 *
 */
@State(Scope.Benchmark)
public class ScriptsReplacerPerf extends AbstractMicrobenchmark {

    private ScriptsLoader scriptsLoader;
    private Map<String, Object> replacements;

    public ScriptsReplacerPerf() {
        try {
            this.scriptsLoader = new ScriptsLoader();

            this.scriptsLoader.setPath("my_scripts");
            this.scriptsLoader.init(EnumScripts.values());

            this.replacements = new HashMap<>();

            this.replacements.put("statusNormal", "ok");
            this.replacements.put("vacation", "yes");
            this.replacements.put("birthday", "now");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected double getExpectedMinNbOpsPerSeconds() {
        return 500d;
    }

    /**
     * Test class for {@link ScriptsLoader} and {@link ScriptsReplacer}.
     */
    @Benchmark
    public void perfReplacer() {
        this.scriptsLoader.get(EnumScripts.PATIENTS_SEARCH, replacements);
    }

    @Test
    public void testPerf() throws IOException, RunnerException {
        assertNotNull(super.run());
    }
}
