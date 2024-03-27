package com.grs.core.service;

import com.grs.api.model.request.UserDTO;
import com.grs.core.dao.UserDAO;
import com.grs.core.domain.projapoti.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 9/19/2017.
 */
@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;

}
