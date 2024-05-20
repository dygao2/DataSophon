/*
 *
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
 *
 */

package com.datasophon.api.migration;

import static com.datasophon.api.migration.DatabaseMigration.SPLIT;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.core.io.Resource;

@Data
@NoArgsConstructor
public class Migration implements Comparable<Migration> {
    
    private String version;
    
    private String executeUser;
    
    private Date executeDate;
    
    private boolean success;
    
    private Resource upgradeDDLFile;
    
    private Resource upgradeDMLFile;
    
    private Resource rollbackFile;
    
    public Migration(String version, Resource upgradeDDLFile, Resource upgradeDMLFile, Resource rollbackFile) {
        this.version = version;
        this.upgradeDDLFile = upgradeDDLFile;
        this.upgradeDMLFile = upgradeDMLFile;
        this.rollbackFile = rollbackFile;
    }
    
    @Override
    public int compareTo(Migration other) {
        int[] otherId = Arrays.stream(other.getVersion().split("\\.")).mapToInt(Integer::valueOf).toArray();
        int[] thisId = Arrays.stream(version.split("\\.")).mapToInt(Integer::valueOf).toArray();
        if (otherId.length != thisId.length) {
            return thisId.length - otherId.length;
        }
        for (int i = 0; i < thisId.length; i++) {
            if (thisId[i] != otherId[i]) {
                return thisId[i] - otherId[i];
            }
        }
        return 0;
    }
    
    public static boolean isMigrationFile(Resource resource) {
        if (resource == null) {
            return false;
        }
        String name = resource.getFilename();
        if (!StringUtils.endsWithIgnoreCase(name, ".sql")) {
            return false;
        }
        if (name == null || name.split(SPLIT).length != 2) {
            return false;
        }
        return name.startsWith(ScriptType.ROLLBACK.getPrefix()) || name.startsWith(ScriptType.UPGRADE.getPrefix());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Migration)) {
            return false;
        }
        Migration migration = (Migration) o;
        return version.equals(migration.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
