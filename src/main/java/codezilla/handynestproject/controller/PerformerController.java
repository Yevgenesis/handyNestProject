package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.performer.PerformerResponseDto;
import codezilla.handynestproject.mapper.PerformerMapper;
import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.service.PerformerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/performer")
@RequiredArgsConstructor
public class PerformerController {

    private final PerformerService performerService;
    private final PerformerMapper performerMapper;


    @GetMapping
    public List<PerformerResponseDto> getAllPerformers() {
        return performerMapper.performersToListDto(performerService.getPerformers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Performer> getUserById(@PathVariable Long id) {
        Optional<Performer> performerOptional = performerService.getPerformerById(id);
        return performerOptional.map(performer -> new ResponseEntity<>(performer, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
