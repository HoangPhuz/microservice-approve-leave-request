

package com.example.approvalservice.repository;

import com.example.approvalservice.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> { // String là kiểu của requestId

    // Tìm các yêu cầu đang chờ duyệt của một quản lý cụ thể
    List<LeaveRequest> findByAssignedApproverIdAndCurrentRequestStatus(String assignedApproverId, String status);

    // Tìm các yêu cầu của một nhân viên cụ thể
    List<LeaveRequest> findByRequestingEmployeeIdOrderByRequestSubmissionTimestampDesc(String requestingEmployeeId);

    // Tìm theo Saga ID (nếu LRS cần)
    List<LeaveRequest> findByOrchestratingSagaId(String sagaId);
}