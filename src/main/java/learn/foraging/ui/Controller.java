package learn.foraging.ui;

import learn.foraging.data.DataException;
import learn.foraging.domain.ForageService;
import learn.foraging.domain.ForagerService;
import learn.foraging.domain.ItemService;
import learn.foraging.domain.Result;
import learn.foraging.models.Category;
import learn.foraging.models.Forage;
import learn.foraging.models.Forager;
import learn.foraging.models.Item;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Controller {

    private final ForagerService foragerService;
    private final ForageService forageService;
    private final ItemService itemService;
    private final View view;

    public Controller(ForagerService foragerService, ForageService forageService, ItemService itemService, View view) {
        this.foragerService = foragerService;
        this.forageService = forageService;
        this.itemService = itemService;
        this.view = view;
    }

    public void run() {
        view.displayHeader("Welcome to Sustainable Foraging");
        try {
            runAppLoop();
        } catch (DataException ex) {
            view.displayException(ex);
        }
        view.displayHeader("Goodbye.");
    }

    private void runAppLoop() throws DataException {
        MainMenuOption option;
        do {
            option = view.selectMainMenuOption();
            switch (option) {
                case VIEW_FORAGES_BY_DATE:
                    viewByDate();
                    break;
                case VIEW_ITEMS:
                    viewItems();
                    break;
                case VIEW_FORAGER:
                    viewForager();
                    break;
                case ADD_FORAGE:
                    addForage();
                    break;
                case ADD_FORAGER:
                    addForager();
                    view.enterToContinue();
                    break;
                case ADD_ITEM:
                    addItem();
                    break;
                case REPORT_KG_PER_ITEM:
                    reportKgPerItem();
                    view.enterToContinue();
                    break;
                case REPORT_CATEGORY_VALUE:
                    reportValueByCategory();
                    view.enterToContinue();
                    break;
                case GENERATE:
                    generate();
                    break;
            }
        } while (option != MainMenuOption.EXIT);
    }

    // top level menu
    private void viewByDate() {
        view.displayHeader(MainMenuOption.VIEW_FORAGES_BY_DATE.getMessage());
        LocalDate date = view.getForageDate();
        List<Forage> forages = forageService.findByDate(date);
        view.displayForages(forages);
        view.enterToContinue();
    }

    private void viewItems() {
        view.displayHeader(MainMenuOption.VIEW_ITEMS.getMessage());
        Category category = view.getItemCategory();
        List<Item> items = itemService.findByCategory(category);
        view.displayHeader("Items");
        view.displayItems(items);
        view.enterToContinue();
    }

    private void viewForager(){
        view.displayHeader(MainMenuOption.VIEW_FORAGER.getMessage());
        String state = view.getForagerState();
        List<Forager> foragers = foragerService.findByState(state);
        view.displayHeader("Foragers from " + state);
        view.displayForagers(foragers, state);
        view.enterToContinue();
    }

    private void addForager() throws DataException{
        view.displayHeader(MainMenuOption.ADD_FORAGER.getMessage());
        Forager forager = view.makeForager();
        if(forager==null){
            return;
        }
        Result<Forager> result = foragerService.add(forager);
        if(!result.isSuccess()){
            view.displayStatus(false, result.getErrorMessages());
        }else{
            String successMessage = String.format("Forager %s created.", result.getPayload().getId());
            view.displayStatus(true, successMessage);
        }
    }

    private void addForage() throws DataException {
        view.displayHeader(MainMenuOption.ADD_FORAGE.getMessage());
        Forager forager = getForager();
        if (forager == null) {
            return;
        }
        Item item = getItem();
        if (item == null) {
            return;
        }
        Forage forage = view.makeForage(forager, item);
        Result<Forage> result = forageService.add(forage);
        if (!result.isSuccess()) {
            view.displayStatus(false, result.getErrorMessages());
        } else {
            String successMessage = String.format("Forage %s created.", result.getPayload().getId());
            view.displayStatus(true, successMessage);
        }
    }

    private void addItem() throws DataException {
        Item item = view.makeItem();
        Result<Item> result = itemService.add(item);
        if (!result.isSuccess()) {
            view.displayStatus(false, result.getErrorMessages());
        } else {
            String successMessage = String.format("Item %s created.", result.getPayload().getId());
            view.displayStatus(true, successMessage);
        }
    }

    private void reportKgPerItem() throws DataException{
        view.displayHeader(MainMenuOption.REPORT_KG_PER_ITEM.getMessage());
        LocalDate forageDate = view.getForageDate(); //get date from view
        List<Forage> foragesByDate = forageService.findByDate(forageDate); //get list of forages on date
        if(foragesByDate == null ||foragesByDate.size() == 0){ //if none found, return
            view.displayStatus(false, "No forages on " + forageDate.toString());
            return;
        }

        //create list of distinct items on date
        List<Item> itemsOnDate = foragesByDate.stream()
                .map(Forage::getItem)
                .distinct()
                .collect(Collectors.toList());

        //create map of distinct items to kg value
        HashMap<Item, Double> totals = new HashMap<>();
        for(Item item : itemsOnDate){
            totals.put(item, 0.0);
        }

        //loop through list of forages and add kg to appropriate item total
        for(Forage forage : foragesByDate){
            double currentValue = totals.get(forage.getItem());
            totals.put(forage.getItem(), currentValue+forage.getKilograms());
        }

        //display totals in view
        view.displayForageTotals(totals, forageDate);
    }

    private void reportValueByCategory(){
        view.displayHeader(MainMenuOption.REPORT_CATEGORY_VALUE.getMessage());
        LocalDate forageDate = view.getForageDate(); //get date from view
        List<Forage> foragesByDate = forageService.findByDate(forageDate); //get forages on date
        if(foragesByDate == null ||foragesByDate.size() == 0){ //if none found, return
            view.displayStatus(false, "No forages on " + forageDate.toString());
            return;
        }

        //create map of categories to totals
        HashMap<Category, BigDecimal> totals = new HashMap<>();
        for(Category c : Category.values()){
            totals.put(c, BigDecimal.ZERO);
        }

        for(Forage forage : foragesByDate){
            BigDecimal currentValue = totals.get(forage.getItem().getCategory());
            BigDecimal newAddition = forage.getItem().getDollarPerKilogram().multiply(BigDecimal.valueOf(forage.getKilograms()));

            totals.put(forage.getItem().getCategory(), currentValue.add(newAddition));
        }

        for(Map.Entry<Category, BigDecimal> entry : totals.entrySet()){
            BigDecimal roundedToCents = totals.get(entry.getKey()).setScale(2, RoundingMode.HALF_UP);
            totals.put(entry.getKey(), roundedToCents);
        }

        view.displayCategoryTotals(totals, forageDate);
    }

    private void generate() throws DataException {
        GenerateRequest request = view.getGenerateRequest();
        if (request != null) {
            int count = forageService.generate(request.getStart(), request.getEnd(), request.getCount());
            view.displayStatus(true, String.format("%s forages generated.", count));
        }
    }

    // support methods
    private Forager getForager() {
        String lastNamePrefix = view.getForagerNamePrefix();
        List<Forager> foragers = foragerService.findByLastName(lastNamePrefix);
        return view.chooseForager(foragers);
    }

    private Item getItem() {
        Category category = view.getItemCategory();
        List<Item> items = itemService.findByCategory(category);
        return view.chooseItem(items);
    }
}
