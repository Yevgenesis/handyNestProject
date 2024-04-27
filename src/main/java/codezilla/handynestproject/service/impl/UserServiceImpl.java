package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.model.entity.User;
import codezilla.handynestproject.repository.UserRepository;
import codezilla.handynestproject.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
