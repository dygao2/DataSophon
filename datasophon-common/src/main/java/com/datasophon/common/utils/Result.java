
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

package com.datasophon.common.utils;

import com.datasophon.common.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Result extends HashMap<String, Object> {
    
    private static final long serialVersionUID = 1L;
    
    private Integer code;
    private String msg;
    
    private Object data;
    
    public Result() {
    }
    
    public static Result error() {
        return error(500, "未知异常，请联系管理员");
    }
    
    public static Result error(String msg) {
        return error(500, msg);
    }
    
    public static Result error(int code, String msg) {
        Result result = new Result();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }
    
    public static Result success(Map<String, Object> map) {
        Result result = new Result();
        result.putAll(map);
        return result;
    }
    
    public Integer getCode() {
        return (Integer) this.get(Constants.CODE);
    }
    
    public Object getData() {
        return this.get(Constants.DATA);
    }
    
    public boolean isSuccess() {
        return this.getCode() == 200;
    }
    
    public static Result success(Object data) {
        Result result = new Result();
        result.put(Constants.CODE, 200);
        result.put(Constants.MSG, "success");
        result.put("data", data);
        return result;
    }
    
    public static Result success() {
        Result result = new Result();
        result.put(Constants.CODE, 200);
        result.put(Constants.MSG, "success");
        return result;
    }
    
    public static Result successEmptyCount() {
        Result result = success(new ArrayList<>(0));
        result.put(Constants.TOTAL, 0);
        return result;
    }
    
    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
