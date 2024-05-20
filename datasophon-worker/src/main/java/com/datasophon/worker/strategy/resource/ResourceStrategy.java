package com.datasophon.worker.strategy.resource;

import lombok.Data;

@Data
public abstract class ResourceStrategy {
    
    public static final String TYPE_KEY = "type";
    
    String frameCode;
    
    String service;
    
    String serviceRole;
    
    String basePath;
    
    public abstract void exec();
    
}
