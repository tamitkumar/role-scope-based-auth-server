package com.tech.brain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "registered_services", schema = "auth")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", unique = true, nullable = false)
    private String serviceName;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<ScopeRequestEntity> requestedScopes = new ArrayList<>();
}
