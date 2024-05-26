package codezilla.handynestproject.service.messenger;

import codezilla.handynestproject.feign.MessengerFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    @Autowired
    private MessengerFeignClient messengerFeignClient;

    public List<Message> getUnreadMessages(Long userId){
        return messengerFeignClient.getUnreadMessages(userId);
    }
}
