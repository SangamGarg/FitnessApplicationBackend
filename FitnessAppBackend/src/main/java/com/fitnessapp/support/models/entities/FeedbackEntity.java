package com.fitnessapp.support.models.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_feedbacks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String feedback;
    private String name;
    private String userId;
    private String email;


}