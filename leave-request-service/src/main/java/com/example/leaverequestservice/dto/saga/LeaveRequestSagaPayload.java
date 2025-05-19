package com.example.leaverequestservice.dto.saga;



import com.example.leaverequestservice.dto.CreateLeaveRequestCmd;
import com.example.leaverequestservice.dto.ext.EmployeeDetailsDto;
import com.example.leaverequestservice.dto.ext.LeaveValidationResponseDto;
import lombok.Data;

@Data
public class LeaveRequestSagaPayload {
    private CreateLeaveRequestCmd createLeaveRequestCmd;
    private String leaveRequestId; // ID từ ApprovalService
    private EmployeeDetailsDto employeeDetails; // Thông tin của nhân viên tạo request
    // không nhất thiết lưu managerDetails ở đây vì có thể lấy lại khi cần
    private LeaveValidationResponseDto leaveValidationResponse;
    // Thêm các thông tin khác cần thiết cho các bước của Saga
}