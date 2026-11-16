package com.example.incidents.repository;

import com.example.incidents.entity.Incident;
import com.example.incidents.entity.Status;
import com.example.incidents.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByCreatedByOrderByCreatedAtDesc(User user);

    List<Incident> findByAssignedToOrderByCreatedAtDesc(User technician);

    List<Incident> findAllByOrderByCreatedAtDesc();

    long countByStatus(Status status);
}
