package codezilla.handynestproject.service;

import codezilla.handynestproject.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getUsers();

    Optional<User> getUserByUuid(Long id);
}
