package com.neb.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neb.dto.client.AddClientRequest;
import com.neb.dto.client.ClientProfileDto;
import com.neb.entity.Client;
import com.neb.entity.Users;
import com.neb.repo.ClientRepository;
import com.neb.repo.UsersRepository;
import com.neb.service.ClientService;
import com.neb.util.AuthUtils;

@Service
public class ClientServiceImpl implements ClientService{

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private ClientRepository clientRepository;
	
	@Autowired
	private ModelMapper mapper;
	
	
	@Override
	public ClientProfileDto getMyProfile() {

        String email = AuthUtils.getCurrentUserEmail();
        if (email == null) throw new RuntimeException("User not authenticated");

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        ClientProfileDto clientProfileDto = mapper.map(client, ClientProfileDto.class);
        return clientProfileDto;
    }


	@Override
	public Long createClient(AddClientRequest addClientReq, Users user) {
		
		Client client = mapper.map(addClientReq, Client.class);
		client.setUser(user);
		Client savedClient = clientRepository.save(client);
		
		return savedClient.getId();
	}

}
