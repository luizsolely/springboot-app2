package com.picpay.services;

import java.math.BigDecimal;

import com.picpay.domain.user.User;
import com.picpay.domain.user.UserType;
import com.picpay.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class UserService {

	@Autowired
	private UserRepository repository;
	
	public void validateTransaction(User sender, BigDecimal amount) throws Exception {
		if(sender.getUserType().equals(UserType.MERCHANT)) {
			throw new Exception("O usuário é do tipo lojista, portanto não pode transacionar.");
		}
		
		if(sender.getBalance().compareTo(amount) < 0) {
			throw new Exception("O usuário não possui saldo suficiente para fazer a transação.");
		}
	}
	
	public User FindUserById(Long id) throws Exception {
		return this.repository.findUserById(id).orElseThrow(() -> new Exception("Usuário não encontrado."));
	}
	
	public void saveUser(User user) {
		this.repository.save(user);
	}
	
}
