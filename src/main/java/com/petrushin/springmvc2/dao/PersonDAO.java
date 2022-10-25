package com.petrushin.springmvc2.dao;

import com.petrushin.springmvc2.model.Person;
import com.petrushin.springmvc2.sheets.MyGoogleSheets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersonDAO {

    @Autowired
    MyGoogleSheets sheet;

    private List<Person> listOfPeoples;
    private int lastId = 0;

    public List<Person> getPeoples(){
        listOfPeoples = new ArrayList<>();
        for(List row : sheet.getValues("Persons")){
            if(row.get(0).equals("id")) continue;
            listOfPeoples.add(new Person(Integer.parseInt((String) row.get(0)), (String) row.get(1), (String)row.get(2), (String)row.get(3)));
            if(row.get(0) == null) break;
        }
        setLastId();
        return listOfPeoples;
    }

    private void setLastId(){
        for(Person person : listOfPeoples){
            if(person.getId() > lastId) lastId = person.getId();
        }
    }

    public Person getPersonById(int id){
        listOfPeoples = getPeoples();
        for(Person person: listOfPeoples){
            if(person.getId() == id) return person;
        }
        return null;
    }

    public void savePerson(Person person){
        listOfPeoples = getPeoples();
        person.setId(++lastId);
        sheet.writeValues("Persons", person.toString());
    }

    public void editPerson(Person newPerson){
        Person oldPerson = getPersonById(newPerson.getId());
        sheet.updateValues("Persons", oldPerson.toString(), newPerson.toString());
    }

    public void deletePerson(int id){
        sheet.deleteRows("Persons", getPersonById(id).toString(), 0);
    }
}
