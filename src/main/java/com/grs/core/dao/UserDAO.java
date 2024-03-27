package com.grs.core.dao;

import com.grs.api.model.request.UserDTO;
import com.grs.core.domain.projapoti.User;
import com.grs.core.repo.projapoti.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Created by Tanvir on 4/13/2017.
 */
@Slf4j
@Service
public class UserDAO {
    @Autowired
    private UserRepo userRepo;
    private int MIN = 100000;
    private int MAX = 999999;


    public User save(User user) {
        return this.userRepo.save(user);
    }

    public User findOne(Long id) {
        return this.userRepo.findOne(id);
    }

    public List<User> findAll() {
        return this.userRepo.findAll();
    }

    public User findByUsername(String username) {
        return this.userRepo.findByUsername(username);
    }

    public User findByEmployeeRecordId(Long employeeRecordId) {
        return userRepo.findByEmployeeRecordId(employeeRecordId);
    }

    private String getSecurityCode() {
        Random rand = new Random();
        Integer randomNum = rand.nextInt((this.MAX - this.MIN) + 1) + this.MIN;
        return randomNum.toString();
    }

    public User register(UserDTO userDTO) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        User user = User.builder()
                .username(userDTO.getUsername())
                .password(bCryptPasswordEncoder.encode(userDTO.getPassword()))
                .confirmationCode(this.getSecurityCode())
                .authenticated(true)
                .build();

        return this.save(user);
    }
}
