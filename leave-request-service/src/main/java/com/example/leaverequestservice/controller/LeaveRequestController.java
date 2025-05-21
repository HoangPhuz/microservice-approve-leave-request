

package com.example.leaverequestservice.controller;

import com.example.leaverequestservice.dto.CreateLeaveRequestCmd;
import com.example.leaverequestservice.dto.ManagerDecisionCmd;
// import com.example.leaverequestservice.dto.LeaveRequestDetailsDto; // Nếu có API GET
import com.example.leaverequestservice.service.LeaveRequestApiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {
    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestController.class);
    private final LeaveRequestApiService leaveRequestApiService;

    public LeaveRequestController(LeaveRequestApiService leaveRequestApiService) {
        this.leaveRequestApiService = leaveRequestApiService;
    }

    @PostMapping
    public ResponseEntity<?> createLeaveRequest(@Valid @RequestBody CreateLeaveRequestCmd command) {
        logger.info("Received request to create leave: {}", command);
        try {
            // requestingEmployeeId giờ đã nằm trong command object
            String sagaId = leaveRequestApiService.submitLeaveRequest(command);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Leave request submitted. Processing...",
                            "sagaId", sagaId
//                             "leaveRequestId", requestIdFromSagaPayload // (Tùy chọn)
                    ));
        } catch (IllegalArgumentException e) { // Ví dụ, nếu ID nhân viên không hợp lệ từ service
            logger.warn("Bad request for createLeaveRequest: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error submitting leave request for employee {}", command.getRequestingEmployeeId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit leave request: " + e.getMessage()));
        }
    }

    @PostMapping("/{leaveRequestId}/decision")
    public ResponseEntity<?> managerMakesDecision(
            @PathVariable String leaveRequestId,
            @Valid @RequestBody ManagerDecisionCmd command) {
        logger.info("Received manager decision for leaveRequestId {}: {}", leaveRequestId, command);
        try {
            // decidingManagerId giờ đã nằm trong command object
            leaveRequestApiService.handleManagerDecision(leaveRequestId, command);
            return ResponseEntity.ok(Map.of("message", "Manager decision processed for request: " + leaveRequestId));
        } catch (IllegalArgumentException | SecurityException e) { // SecurityException nếu logic ủy quyền vẫn còn (ví dụ check managerId với request)
            logger.warn("Bad request or authorization issue for managerMakesDecision on {}: {}", leaveRequestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) { // Bắt các lỗi nghiệp vụ hoặc Saga
            logger.error("Runtime error processing manager decision for {}: {}", leaveRequestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error processing manager decision for {}: {}", leaveRequestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process manager decision: " + e.getMessage()));
        }
    }

    // Các API GET (nếu có) sẽ không cần header X-User-ID cho mục đích xác thực người gọi
    // mà có thể nhận employeeId/managerId qua path variable hoặc request param nếu cần lọc dữ liệu.
    // Ví dụ:
    // @GetMapping("/employee/{employeeId}")
    // public ResponseEntity<List<LeaveRequestDetailsDto>> getLeaveRequestsForEmployee(@PathVariable String employeeId) {
    //     // ...
    // }
}