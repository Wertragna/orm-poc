package org.essential.core;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EntityKey {
    private final Object entityId;
    private final Class<?> entityClass;
}
