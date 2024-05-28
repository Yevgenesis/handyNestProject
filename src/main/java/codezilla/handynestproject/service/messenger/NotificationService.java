package codezilla.handynestproject.service.messenger;

import codezilla.handynestproject.feign.MessengerFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class NotificationService {


    private MessengerFeignClient messengerFeignClient;

    public List<codezilla.handynestmessenger.model.entity.Message> getUnreadMessages(Long userId){
        return messengerFeignClient.getUnreadMessages(userId);
    }
}
