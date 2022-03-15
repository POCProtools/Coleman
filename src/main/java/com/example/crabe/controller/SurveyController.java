package com.example.crabe.controller;

import com.example.crabe.beans.Person;
import com.example.crabe.beans.Survey;
import com.example.crabe.exceptions.DuplicateException;
import com.example.crabe.exceptions.NotFoundException;
import com.example.crabe.repository.PersonRepository;
import com.example.crabe.repository.SurveyRepository;
import com.example.crabe.services.PersonService;
import com.example.crabe.services.SurveyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class SurveyController {
    // Relatif a une enquete

    @Autowired
    SurveyService surveyService;
    @Autowired
    PersonService personService;

    private final SurveyRepository repository;
    private final PersonRepository personRepository;
    // private final SurveyAssembler assembler;
    static final Logger LOGGER = LoggerFactory.getLogger(SurveyController.class);

    public SurveyController(SurveyRepository repository, PersonRepository personRepository) {
        this.repository = repository;
        // this.assembler = assembler;
        this.personRepository = personRepository;
    }

    @GetMapping("/surveys")
    ResponseEntity<List<Survey>> all(){
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping("/surveys")
    public ResponseEntity<Survey> addSurvey(@RequestBody Survey newSurvey){
        LOGGER.info("Post request, add a survey");
        return new ResponseEntity<Survey>(surveyService.save(newSurvey), HttpStatus.CREATED);

    }

    @GetMapping(value = "/surveys/{idsurvey}", produces = "application/json")
    public ResponseEntity<Survey> displayAsurvey(@PathVariable Long idsurvey) throws NotFoundException {
        LOGGER.info("Request GET with survey id : {}", idsurvey);
        return ResponseEntity.ok(surveyService.findById(idsurvey));
    }

    @GetMapping("/surveys/{idsurvey}/units")
    public ResponseEntity<List<Person>> allUnits(@PathVariable String idsurvey){
        // est ce que c'est vraiment utile ?
        return ResponseEntity.ok(personRepository.findByIdSurvey(Long.parseLong(idsurvey)));
    }

    @PostMapping("/surveys/{idsurvey}/units")
    public ResponseEntity addToSurvey(@RequestBody List<Person> units, @PathVariable String idsurvey){
        LOGGER.info("POST request to add unit to survey {}", idsurvey);
        Long idLongSurvey = Long.parseLong(idsurvey);
        try {
            surveyService.findById(idLongSurvey);
            LOGGER.info("Enquête trouvée");
            List<Person> list = personService.addToSurvey(idLongSurvey, units);
            personRepository.saveAll(list);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (NotFoundException e) {
            LOGGER.error("Error in request: campaign not found {}", idsurvey);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (DuplicateException e){
            LOGGER.error("Error in request: duplicate unit");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
