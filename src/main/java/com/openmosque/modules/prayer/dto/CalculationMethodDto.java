package com.openmosque.modules.prayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationMethodDto implements Serializable {
    private String code;
    private String name;
    private int aladhanMethodId;
}
