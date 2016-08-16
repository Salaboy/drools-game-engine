/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.game.core;

import java.io.PrintStream;
import java.util.List;
import org.drools.game.core.api.PlayerConfiguration;

/**
 * Basic implementation of PlayerConfiguration that handles the getter-setter busywork of standard usecases for you.
 * @author salaboy
 */
public class BasePlayerConfigurationImpl implements PlayerConfiguration {

    private List initialData;

    public BasePlayerConfigurationImpl() {
    }

    public BasePlayerConfigurationImpl( List initialData ) {
        this.initialData = initialData;
    }

    @Override
    public List getInitialData() {
        return initialData;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public PrintStream getLogStream() {
        return System.out;
    }

}
