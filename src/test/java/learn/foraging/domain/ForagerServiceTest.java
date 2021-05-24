package learn.foraging.domain;

import learn.foraging.data.DataException;
import learn.foraging.data.ForagerRepositoryDouble;
import learn.foraging.models.Forager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForagerServiceTest {
    ForagerService foragerService = new ForagerService(
            new ForagerRepositoryDouble()
    );

    @Test
    void shouldFindForagerByLastName(){
        List<Forager> actual = foragerService.findByLastName("Sisse");
        assertNotEquals(0, actual.size());
        assertEquals("Sisse", actual.get(0).getLastName());
    }

    @Test
    void shouldFindByState(){
        List<Forager> actual = foragerService.findByState("GA");
        assertNotEquals(0, actual.size());
        assertEquals("GA", actual.get(0).getState());
    }

    @Test
    void shouldNotFindMissingLastName(){
        List<Forager> actual = foragerService.findByLastName("Johnson");
        assertEquals(0, actual.size());
    }

    @Test
    void shouldNotFindMissingState(){
        List<Forager> actual = foragerService.findByState("WI");
        assertEquals(0, actual.size());
    }

    @Test
    void shouldAdd() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Bilbo");
        forager.setLastName("Schnabbins");
        forager.setState("IA");

        Result<Forager> result = foragerService.add(forager);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldNotAddWithPresetId() throws DataException {
        Forager forager = new Forager();
        forager.setId("8000");
        forager.setFirstName("Bilbo");
        forager.setLastName("Schnabbins");
        forager.setState("IA");

        Result<Forager> result = foragerService.add(forager);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddWithoutFields() throws DataException {
        Forager forager = new Forager();

        Result<Forager> result = foragerService.add(forager);

        assertFalse(result.isSuccess());
        assertEquals(3, result.getErrorMessages().size());
    }
}