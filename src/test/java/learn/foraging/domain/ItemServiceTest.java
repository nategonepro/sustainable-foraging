package learn.foraging.domain;

import learn.foraging.data.DataException;
import learn.foraging.data.ItemRepositoryDouble;
import learn.foraging.models.Category;
import learn.foraging.models.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    ItemService service = new ItemService(new ItemRepositoryDouble());

    @Test
    void shouldNotSaveNullName() throws DataException {
        Item item = new Item(0, null, Category.EDIBLE, new BigDecimal("5.00"));
        Result<Item> result = service.add(item);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotSaveBlankName() throws DataException {
        Item item = new Item(0, "   \t\n", Category.EDIBLE, new BigDecimal("5.00"));
        Result<Item> result = service.add(item);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotSaveNullDollars() throws DataException {
        Item item = new Item(0, "Test Item", Category.EDIBLE, null);
        Result<Item> result = service.add(item);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotSaveNegativeDollars() throws DataException {
        Item item = new Item(0, "Test Item", Category.EDIBLE, new BigDecimal("-5.00"));
        Result<Item> result = service.add(item);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotSaveTooLargeDollars() throws DataException {
        Item item = new Item(0, "Test Item", Category.EDIBLE, new BigDecimal("9999.00"));
        Result<Item> result = service.add(item);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldSave() throws DataException {
        Item item = new Item(0, "Test Item", Category.EDIBLE, new BigDecimal("5.00"));

        Result<Item> result = service.add(item);

        assertNotNull(result.getPayload());
        assertEquals(2, result.getPayload().getId());
    }

    @Test
    void shouldFindByCategory(){
        List<Item> items = service.findByCategory(Category.EDIBLE);
        assertEquals(1, items.size());
    }

    @Test
    void shouldNotFindEmptyCategory(){
        List<Item> items = service.findByCategory(Category.MEDICINAL);
        assertEquals(0, items.size());
    }
}