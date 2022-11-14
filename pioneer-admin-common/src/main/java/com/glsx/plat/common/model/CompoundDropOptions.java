package com.glsx.plat.common.model;

import lombok.Data;

import java.util.List;

@Data
public class CompoundDropOptions {

    private String id;
    private String pId;
    private String name;
    private List<CompoundDropOptions> children;

}
