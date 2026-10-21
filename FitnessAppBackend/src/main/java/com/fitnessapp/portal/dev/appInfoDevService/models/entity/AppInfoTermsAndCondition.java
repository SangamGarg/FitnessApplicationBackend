package com.fitnessapp.portal.dev.appInfoDevService.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class AppInfoTermsAndCondition {

    //The @Lob annotation in Spring Boot (which uses the Java Persistence API (JPA) and Hibernate) is used to specify that a field should be persisted as a Large Object (LOB) in the database. This is intended for data that is too large to fit in standard VARCHAR or other typical column types, such as images, audio files, or extensive text documents.

    @Lob
    private String content;

    @Column(name = "lastUpdatedTAndC")
    private String lastUpdatedBy;

}
