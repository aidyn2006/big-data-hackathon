package org.example.bigdatahackathon.repository;

import org.example.bigdatahackathon.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
    
    List<Complaint> findByRoute(String route);
    
    List<Complaint> findByPriority(String priority);
    
    List<Complaint> findByActor(String actor);
    
    List<Complaint> findByPlace(String place);

    List<Complaint> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    
    @Query("SELECT c FROM Complaint c WHERE c.route = :route AND c.priority = :priority")
    List<Complaint> findByRouteAndPriority(@Param("route") String route, @Param("priority") String priority);
}

