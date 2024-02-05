package com.kapeta.spring.rabbitmq.types;

import com.kapeta.schemas.entity.ResourceMetadata;
import lombok.Data;

@Data
public class ResourceWithSpec<Spec> {
    private ResourceMetadata metadata;
    private Spec spec;
}
