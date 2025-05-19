


package com.example.leaverequestservice.service;

import com.example.leaverequestservice.dto.CreateLeaveRequestCmd;
import com.example.leaverequestservice.dto.ManagerDecisionCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LeaveRequestApiService {
    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestApiService.class);
    private final LeaveRequestSagaOrchestrator sagaOrchestrator;

    public LeaveRequestApiService(LeaveRequestSagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    public String submitLeaveRequest(CreateLeaveRequestCmd command) {
        // Không còn authenticatedEmployeeId từ context, dùng từ command.requestingEmployeeId
        logger.info("Submitting leave request for employee: {}", command.getRequestingEmployeeId());
        // Logic kiểm tra quyền cơ bản có thể vẫn cần nếu service này có thông tin vai trò,
        // nhưng ở đây chúng ta bỏ qua xác thực chi tiết.
        return sagaOrchestrator.startLeaveRequestSaga(command);
    }

    public void handleManagerDecision(String leaveRequestId, ManagerDecisionCmd command) {
        // Không còn authenticatedManagerId từ context, dùng từ command.decidingManagerId
        logger.info("Handling manager decision by {} for leave request: {}", command.getDecidingManagerId(), leaveRequestId);
        // Logic kiểm tra quyền của người quản lý đối với yêu cầu này (isManagerAuthorized)
        // sẽ cần được điều chỉnh hoặc tạm thời bỏ qua/đơn giản hóa.
        // Ví dụ: Kiểm tra xem command.getDecidingManagerId() có phải là người được gán duyệt cho leaveRequestId không
        // bằng cách gọi ApprovalServiceClient hoặc kiểm tra thông tin đã lưu trong SagaInstance.
        if (!isManagerAuthorizedForRequest(leaveRequestId, command.getDecidingManagerId())) {
            throw new SecurityException("Manager " + command.getDecidingManagerId() +
                    " is not authorized to decide on leave request " + leaveRequestId);
        }
        sagaOrchestrator.processManagerDecision(leaveRequestId, command);
    }

    private boolean isManagerAuthorizedForRequest(String leaveRequestId, String managerId) {
        // TẠM THỜI: Logic kiểm tra ủy quyền đơn giản hóa.
        // Trong thực tế, bạn sẽ gọi ApprovalService (hoặc kiểm tra Saga payload)
        // để xem leaveRequestId này có được gán cho managerId này không.
        logger.debug("Performing authorization check for manager {} on request {}", managerId, leaveRequestId);
        // SagaInstance saga = sagaStateService.getSaga(findSagaByCorrelationId(leaveRequestId));
        // if (saga != null && saga.getSagaPayloadData() != null) {
        //     // Parse payload để lấy assignedApproverId
        //     // return managerId.equals(parsedAssignedApproverId);
        // }
        // return false;
        return true; // Tạm thời cho phép để test luồng
    }
}