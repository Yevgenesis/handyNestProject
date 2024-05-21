package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.service.PerformerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/performer")
@RequiredArgsConstructor
@Validated
public class PerformerController {

    private final PerformerService performerService;

    //POST
    @PostMapping
    public PerformerResponseDto create(@RequestBody @Valid PerformerRequestDto performerDto) {
        return performerService.createPerformer(performerDto);
    }

    //PUT
    @PutMapping
    public PerformerResponseDto update(@RequestBody PerformerRequestDto updateDto) {
        return performerService.updatePerformer(updateDto);
    }

    @PutMapping("/{id}")
    public PerformerResponseDto updateAvailability(@PathVariable Long id, @RequestParam Boolean isPublish) {
        return performerService.updateAvailability(id, isPublish);
    }



    //GET
    @GetMapping
    public List<PerformerResponseDto> findAll() {
        return performerService.getPerformers();
    }

    @GetMapping("/{id}")
    public PerformerResponseDto findById(@PathVariable Long id) {
        return performerService.getPerformerById(id);
    }

}
