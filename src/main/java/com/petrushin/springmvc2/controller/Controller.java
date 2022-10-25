package com.petrushin.springmvc2.controller;

import com.petrushin.springmvc2.dao.PersonDAO;
import com.petrushin.springmvc2.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@org.springframework.stereotype.Controller
public class Controller {

    @Autowired
    private PersonDAO personDAO;

    @GetMapping("/")
    public String main(Model model){
        model.addAttribute("people", personDAO.getPeoples());
        return "main";
    }

    @GetMapping("/{id}")
    public String person(@PathVariable("id") int id, Model model){
        model.addAttribute("person", personDAO.getPersonById(id));
        return "person";
    }

    @GetMapping("/new")
    public String newPerson(Model model){
        model.addAttribute("person",new Person());
        return "new";
    }

    @PostMapping("/")
    public String saveNewPerson(@ModelAttribute("person") Person person){
        personDAO.savePerson(person);
        return "redirect:/";
    }

    @GetMapping("/{id}/edit")
    public String editPerson(@PathVariable("id") int id, Model model){
       model.addAttribute("person", personDAO.getPersonById(id));
        return "edit";
    }

    @PatchMapping("/{id}")
    public String saveEditPerson(@ModelAttribute("person") Person person){
        personDAO.editPerson(person);
        return "redirect:/";
    }

    @DeleteMapping("/{id}")
    public String deletePerson(@PathVariable("id") int id){
        personDAO.deletePerson(id);
        return "redirect:/";
    }

}
