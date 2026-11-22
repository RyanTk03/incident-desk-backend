package com.example.incidents.repository;

import com.example.incidents.entity.Incident;
import com.example.incidents.entity.IncidentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {

    List<IncidentHistory> findByIncidentOrderByCreatedAtAsc(Incident incident);
}
