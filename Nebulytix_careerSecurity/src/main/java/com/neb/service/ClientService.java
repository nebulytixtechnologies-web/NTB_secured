package com.neb.service;

import com.neb.dto.client.AddClientRequest;
import com.neb.dto.client.ClientProfileDto;
import com.neb.entity.Users;

public interface ClientService {

	public ClientProfileDto getMyProfile();
	public Long createClient(AddClientRequest addClientReq, Users user);
}
