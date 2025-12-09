package com.neb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neb.dto.ResponseMessage;
import com.neb.dto.client.ClientProfileDto;
import com.neb.service.ClientService;

@RestController
@RequestMapping("/api/client")
public class ClientController {

	@Autowired
	private ClientService clientService;
	
	@GetMapping("/me")
    public ResponseEntity<ResponseMessage<ClientProfileDto>> getMyProfile() {

        ClientProfileDto dto = clientService.getMyProfile();

        return ResponseEntity.ok(
                new ResponseMessage<>(200, "SUCCESS", "Client profile fetched successfully", dto)
        );
    }
}
