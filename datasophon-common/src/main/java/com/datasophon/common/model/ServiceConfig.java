/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.datasophon.common.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ServiceConfig implements Serializable {
    
    private String name;
    
    private Object value;
    
    private String label;
    
    private String description;
    
    private boolean required;
    
    private String type;
    
    private boolean configurableInWizard;
    
    private Object defaultValue;
    
    private Integer minValue;
    
    private Integer maxValue;
    
    private String unit;
    
    private boolean hidden;
    
    private List<String> selectValue;
    
    private String configType;
    
    private boolean configWithKerberos;
    
    private boolean configWithRack;
    
    private boolean configWithHA;
    
    private String separator;
    
}
