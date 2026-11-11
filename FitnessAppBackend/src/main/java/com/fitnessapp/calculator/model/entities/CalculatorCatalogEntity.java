package com.fitnessapp.calculator.model.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "app_calculator_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatorCatalogEntity {

    @Id
    @Column(name = "calc_key", length = 100)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "calculator_inputs", joinColumns = @JoinColumn(name = "calc_key"))
    @Column(name = "input_name")
    private List<String> requiredInputs;

    private String unit;

    @Builder.Default
    private boolean requiresSavedData = false;
}
