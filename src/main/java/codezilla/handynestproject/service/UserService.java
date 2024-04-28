package codezilla.handynestproject.service;

import codezilla.handynestproject.model.entity.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> getUsers();
}
