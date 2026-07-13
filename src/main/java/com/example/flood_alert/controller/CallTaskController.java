package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.UpdateCallResultRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.CallResultResponse;
import com.example.flood_alert.dbo.response.CallTaskResponse;
import com.example.flood_alert.dbo.response.UpdateCallResultResponse;
import com.example.flood_alert.service.CallWorkflowService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/call-tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CallTaskController {

        CallWorkflowService callWorkflowService;

        /**
         * Hotline cập nhật kết quả cuộc gọi.
         */
        @PutMapping("/{callTaskId}/result")
        public ApiResponse<UpdateCallResultResponse> updateCallResult(
                        @PathVariable UUID callTaskId,
                        @RequestBody @Valid UpdateCallResultRequest request) {

                return ApiResponse.<UpdateCallResultResponse>builder()
                                .result(callWorkflowService.updateCallResult(callTaskId, request))
                                .build();
        }

        // Lấy calltask hiện tại theo sosId
        @GetMapping("/sos/{sosId}")
        public ApiResponse<CallTaskResponse> getBySosId(
                        @PathVariable UUID sosId) {

                return ApiResponse.<CallTaskResponse>builder()
                                .result(callWorkflowService.getBySosId(sosId))
                                .build();
        }

        // FE đang ở màn hình gọi điện và muốn refresh trạng thái của CallTask hiện tại.
        @GetMapping("/{callTaskId}")
        public ApiResponse<CallTaskResponse> getById(
                        @PathVariable UUID callTaskId) {

                return ApiResponse.<CallTaskResponse>builder()
                                .result(callWorkflowService.getById(callTaskId))
                                .build();
        }

        @GetMapping("/call-results")
        public ApiResponse<List<CallResultResponse>> getCallResults() {

                return ApiResponse.<List<CallResultResponse>>builder()
                                .result(callWorkflowService.getCallResults())
                                .build();
        }
}
