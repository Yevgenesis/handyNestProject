package codezilla.handynestproject.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "HandyNestMessenger")

public interface MessengerFeignClient {

    @RequestMapping(method = RequestMethod.GET,value = "/messages")

    @PostMapping("/send")
    Message sendMessage(@RequestParam Long senderId,
                        @RequestParam Long receiverId,
                        @RequestParam String content);

    @GetMapping("/unread/{receiverId}")
    List<Message> getUnreadMessages(@PathVariable Long receiverId);

    @PostMapping("/read/{messageId}")
    void markAsRead(@PathVariable Long messageId);


}
