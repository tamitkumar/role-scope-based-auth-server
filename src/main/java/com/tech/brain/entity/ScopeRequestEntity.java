package com.tech.brain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "scope_requests", schema = "auth")
public class ScopeRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String scope; // e.g., example-api-v1::read
    private boolean approved;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @JsonIgnore
    private ServiceEntity service;
}
