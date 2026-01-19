package com.neb.dto;





import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDTO<T> {
	
	private String message;
	private T data;
	
	
	
	public ResponseDTO() {
		// TODO Auto-generated constructor stub
	}
	public ResponseDTO(T data) {
		this.data = data;
	}

}
