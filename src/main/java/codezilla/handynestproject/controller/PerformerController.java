package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/performer")
@RequiredArgsConstructor
public class PerformerController {

    private final PerformerService performerService;


    @GetMapping
    public List<PerformerResponseDto> getAllPerformers() {
        return performerService.getPerformers();
    }

    @GetMapping("/{id}")
    public PerformerResponseDto getPerformerById(@PathVariable Long id) {
        return performerService.getPerformerById(id);
    }

}
