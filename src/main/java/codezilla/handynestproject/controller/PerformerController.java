package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.performer.PerformerUpdateRequestDto;
import codezilla.handynestproject.service.PerformerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/performers")
@RequiredArgsConstructor
@Validated
public class PerformerController {

    private final PerformerService performerService;

    //POST
    @PostMapping
    public PerformerResponseDto create(@RequestBody @Valid PerformerRequestDto performerDto) {
        return performerService.create(performerDto);
    }

    //PUT
    @PutMapping
    public PerformerResponseDto update(@RequestBody PerformerUpdateRequestDto updateDto) {
        return performerService.update(updateDto);
    }

    @PutMapping("/{id}/{isAvailable}")
    public PerformerResponseDto updateAvailability(@PathVariable Long id, @PathVariable Boolean isAvailable) {
        return performerService.updateAvailability(id, isAvailable);
    }



    //GET
    @GetMapping
    public List<PerformerResponseDto> findAll() {
        return performerService.findAll();
    }

    @GetMapping("/{id}")
    public PerformerResponseDto findById(@PathVariable Long id) {
        return performerService.findById(id);
    }

}
