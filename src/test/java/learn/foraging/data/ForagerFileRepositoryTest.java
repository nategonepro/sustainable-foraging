package learn.foraging.data;

import learn.foraging.models.Forager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForagerFileRepositoryTest {

    static final String SEED_PATH = "./data/foragers-seed.csv";
    static final String TEST_PATH = "./data/foragers-test.csv";

    ForagerFileRepository repo = new ForagerFileRepository(TEST_PATH);

    @BeforeEach
    void setup() throws IOException {
        Path seedPath = Paths.get(SEED_PATH);
        Path testPath = Paths.get(TEST_PATH);
        Files.copy(seedPath, testPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void shouldFindAll() {
        List<Forager> all = repo.findAll();
        assertEquals(1001, all.size());
    }

    @Test
    void shouldFindId(){
        Forager forager = repo.findById("564b668c-378c-48a2-bb4c-ddfb9bff3a1f");

        assertEquals("Buck", forager.getFirstName());
        assertEquals("WI", forager.getState());
    }

    @Test
    void shouldNotFindMissingId(){
        Forager forager = repo.findById("0");

        assertNull(forager);
    }

    @Test
    void shouldFindByState(){
        List<Forager> foragers = repo.findByState("WI");
        for(Forager f : foragers){
            assertEquals("WI", f.getState());
        }
    }

    @Test
    void shouldNotFindFakeState(){
        List<Forager> foragers = repo.findByState("UO");
        assertEquals(0, foragers.size());
    }

    @Test
    void shouldAdd() throws DataException {
        Forager forager = new Forager();
        forager.setState("WI");
        forager.setFirstName("Harry");
        forager.setLastName("McClary");

        forager = repo.add(forager);

        assertEquals(36, forager.getId().length());
    }
}