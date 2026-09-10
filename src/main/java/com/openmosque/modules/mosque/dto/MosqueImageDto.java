package com.openmosque.modules.mosque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueImageDto {
    private UUID id;
    private String imageUrl;
    private String caption;
    private boolean cover;
    private int displayOrder;
}
