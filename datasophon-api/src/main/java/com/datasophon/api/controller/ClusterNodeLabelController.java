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

package com.datasophon.api.controller;

import com.datasophon.api.service.ClusterNodeLabelService;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterNodeLabelEntity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("cluster/node/label")
public class ClusterNodeLabelController {
    
    @Autowired
    private ClusterNodeLabelService nodeLabelService;
    
    /**
     * save node label
     */
    @RequestMapping("/list")
    public Result list(Integer clusterId) {
        List<ClusterNodeLabelEntity> list = nodeLabelService.queryClusterNodeLabel(clusterId);
        return Result.success(list);
    }
    
    /**
     * save node label
     */
    @RequestMapping("/save")
    public Result save(Integer clusterId, String nodeLabel) {
        return nodeLabelService.saveNodeLabel(clusterId, nodeLabel);
    }
    
    /**
     * delete node label
     */
    @RequestMapping("/delete")
    public Result delete(Integer nodeLabelId) {
        return nodeLabelService.deleteNodeLabel(nodeLabelId);
    }
    
    /**
     * assign node label
     */
    @RequestMapping("/assign")
    public Result assign(Integer nodeLabelId, String hostIds) {
        return nodeLabelService.assignNodeLabel(nodeLabelId, hostIds);
    }
}
