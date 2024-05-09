package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/performer")
@RequiredArgsConstructor
public class PerformerController {

    private final PerformerService performerService;

    //POST
    @PostMapping
    public PerformerResponseDto createPerformer(@RequestBody PerformerRequestDto performerDto) {
        return performerService.createPerformer(performerDto);
    }

    //PUT
    @PutMapping
    public PerformerResponseDto updatePerformer(@RequestBody PerformerRequestDto updateDto) {
        return performerService.updatePerformer(updateDto);
    }


    //GET
    @GetMapping
    public List<PerformerResponseDto> getAllPerformers() {
        return performerService.getPerformers();
    }

    @GetMapping("/{id}")
    public PerformerResponseDto getPerformerById(@PathVariable Long id) {
        return performerService.getPerformerById(id);
    }

}
