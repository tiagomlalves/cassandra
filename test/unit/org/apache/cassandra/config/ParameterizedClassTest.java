/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cassandra.config;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.apache.cassandra.auth.AllowAllAuthorizer;
import org.apache.cassandra.auth.IAuthorizer;
import org.apache.cassandra.exceptions.ConfigurationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParameterizedClassTest
{
    @Test
    public void newInstance_NonExistentClass_FailsWithConfigurationException()
    {
        assertThatThrownBy(() -> ParameterizedClass.newInstance(new ParameterizedClass("NonExistentClass"), List.of("org.apache.cassandra.config")))
        .hasMessage("Unable to find class NonExistentClass in packages [\"org.apache.cassandra.config\"]")
        .isInstanceOf(ConfigurationException.class);
    }

    @Test
    public void newInstance_WithSingleEmptyConstructor_UsesEmptyConstructor()
    {
        ParameterizedClass parameterizedClass = new ParameterizedClass(AllowAllAuthorizer.class.getName());
        IAuthorizer instance = ParameterizedClass.newInstance(parameterizedClass, null);
        assertNotNull(instance);
    }

    @Test
    public void newInstance_SingleEmptyConstructorWithParameters_FailsWithConfigurationException()
    {
        assertThatThrownBy(() -> ParameterizedClass.newInstance(new ParameterizedClass(AllowAllAuthorizer.class.getName(), Map.of("key", "value")), null))
        .hasMessageStartingWith("No valid constructor found for class")
        .isInstanceOf(ConfigurationException.class);
    }

    @Test
    public void newInstance_WithValidConstructors_FavorsMapConstructor()
    {
        ParameterizedClass parameterizedClass = new ParameterizedClass(ParameterizedClassExample.class.getName());
        ParameterizedClassExample instance = ParameterizedClass.newInstance(parameterizedClass, null);

        assertTrue(instance.calledMapConstructor);
    }

    @Test
    public void newInstance_WithConstructorException_PreservesOriginalFailure()
    {
        assertThatThrownBy(() -> ParameterizedClass.newInstance(new ParameterizedClass(ParameterizedClassExample.class.getName(),
                                                                                       Map.of("fail", "true")), null))
        .hasMessageContaining("Simulated failure")
        .hasMessageStartingWith("Failed to instantiate class")
        .isInstanceOf(ConfigurationException.class);
    }
}
