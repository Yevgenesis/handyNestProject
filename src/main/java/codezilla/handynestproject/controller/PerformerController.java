package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.performer.PerformerRequestDto;
import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.dto.performer.PerformerUpdateRequestDto;
import codezilla.handynestproject.service.PerformerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/performers")
@RequiredArgsConstructor
public class PerformerController {

    private final PerformerService performerService;

    //POST
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public PerformerResponseDto create(@RequestBody @Valid PerformerRequestDto performerDto) {
        return performerService.create(performerDto);
    }

    //GET
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public List<PerformerResponseDto> findAll() {
        return performerService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @GetMapping("/{id}")
    public PerformerResponseDto findById(@PathVariable Long id) {
        return performerService.findById(id);
    }


    //PUT
    @PreAuthorize("hasAnyAuthority('USER','PERFORMER','ADMIN')")
    @PutMapping
    public PerformerResponseDto update(@RequestBody @Valid PerformerUpdateRequestDto updateDto) {
        return performerService.update(updateDto);
    }
    @PreAuthorize("hasAnyAuthority('PERFORMER','ADMIN')")
    @PutMapping("/{id}/{isAvailable}")
    public PerformerResponseDto updateAvailability(@PathVariable Long id, @PathVariable Boolean isAvailable) {
        return performerService.updateAvailability(id, isAvailable);
    }

}
