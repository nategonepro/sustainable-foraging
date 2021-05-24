package learn.foraging.domain;

import learn.foraging.data.DataException;
import learn.foraging.data.ForagerRepository;
import learn.foraging.models.Forager;

import java.util.List;
import java.util.stream.Collectors;

public class ForagerService {

    private final ForagerRepository repository;

    public ForagerService(ForagerRepository repository) {
        this.repository = repository;
    }

    public List<Forager> findByState(String stateAbbr) {
        return repository.findByState(stateAbbr);
    }

    public List<Forager> findByLastName(String prefix) {
        return repository.findAll().stream()
                .filter(i -> i.getLastName().startsWith(prefix))
                .collect(Collectors.toList());
    }

    public Result<Forager> add(Forager forager) throws DataException{
        Result<Forager> result = validate(forager);
        if(!result.isSuccess()){
            return result;
        }

        result.setPayload(repository.add(forager));
        return result;
    }

    private Result<Forager> validate(Forager forager){
        Result<Forager> result = new Result<>();

        if(forager == null){
            result.addErrorMessage("Nothing to save.");
            return result;
        }

        if(forager.getId() != null){
            result.addErrorMessage("ID should not be set prior to Service.");
        }

        if(forager.getFirstName() == null || forager.getFirstName().isBlank()){
            result.addErrorMessage("First name is required.");
        }

        if(forager.getLastName() == null || forager.getLastName().isBlank()){
            result.addErrorMessage("Last name is required.");
        }

        if(forager.getState() == null || forager.getState().isBlank()){
            result.addErrorMessage("State is required.");
        }else if(forager.getState().length() != 2){
            result.addErrorMessage("State must be in abbreviation form [XX].");
        }

        if(isDuplicate(forager)){
            result.addErrorMessage(String.format("%s %s from %s is already in the database.",
                    forager.getFirstName(),
                    forager.getLastName(),
                    forager.getState()));
        }

        return result;
    }

    private boolean isDuplicate(Forager forager){
        List<Forager> byState = repository.findByState(forager.getState());
        for(Forager f : byState){
            if(f.getFirstName().equals(forager.getFirstName()) &&
                f.getLastName().equals(forager.getLastName()) &&
                f.getState().equals(forager.getState())){
                return true;
            }
        }
        return false;
    }
}
