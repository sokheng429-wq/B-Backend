package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.CustomerGroupDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.CustomerGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customer-groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerGroupController {

    private final CustomerGroupService customerGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerGroupDto>>> getAllCustomerGroups(
            @RequestParam(value = "search", required = false) String search) {
        List<CustomerGroupDto> groups = customerGroupService.getAllCustomerGroups(search);
        return ResponseEntity.ok(ApiResponse.success("Customer groups retrieved successfully", groups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerGroupDto>> getCustomerGroupById(@PathVariable Long id) {
        CustomerGroupDto group = customerGroupService.getCustomerGroupById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer group retrieved successfully", group));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerGroupDto>> createCustomerGroup(@RequestBody CustomerGroupDto dto) {
        CustomerGroupDto created = customerGroupService.createCustomerGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer group created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerGroupDto>> updateCustomerGroup(
            @PathVariable Long id,
            @RequestBody CustomerGroupDto dto) {
        CustomerGroupDto updated = customerGroupService.updateCustomerGroup(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Customer group updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerGroup(@PathVariable Long id) {
        customerGroupService.deleteCustomerGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Customer group deleted successfully"));
    }
}