package com.picpay.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.picpay.domain.transaction.Transaction;
import com.picpay.domain.user.User;
import com.picpay.dtos.TransactionDTO;
import com.picpay.repositories.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TransactionRepository repository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public void createTransaction(TransactionDTO transaction) throws Exception {
    	
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());
        
        userService.validateTransaction(sender, transaction.value());
        
        boolean isAuthorized = this.authorizeTransaction(sender, transaction.value());
        
        if(!isAuthorized) {
        	throw new Exception("Transação não autorizada.");
        }
        
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setTimestamp(LocalDateTime.now());
        
        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));
        
        this.repository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);
        
    }
    
    public boolean authorizeTransaction(User sender, BigDecimal value) {
    	
        ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://util.devi.tools/api/v2/authorize", Map.class);

        if (authorizationResponse.getStatusCode() == HttpStatus.OK &&
            "success".equals(authorizationResponse.getBody().get("status"))) {
            
            Map<String, Object> data = (Map<String, Object>) authorizationResponse.getBody().get("data");
            
            if (Boolean.TRUE.equals(data.get("authorization"))) {
            	
                return true;
                
            }
        }      
        
        return false;
        
    }
}
