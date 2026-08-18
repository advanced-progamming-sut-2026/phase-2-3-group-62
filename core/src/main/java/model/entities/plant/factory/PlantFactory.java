package model.entities.plant.factory;

import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import java.util.List;

public class PlantFactory {
    private static List<Plant> templates = null;

    public static Plant createPlant(String type) {
        if (type == null) return null;
        String cleanType = type.replaceAll("^\"|\"$", "").trim();
        if (templates == null || templates.isEmpty()) {
            templates = PlantLoader.loadPlants();
        }

        for (Plant template : templates) {
            if (template.getName().replace(" ", "").replace("-", "")
                .equalsIgnoreCase(cleanType.replace(" ", "").replace("-", ""))) {
                Plant p = new Plant(
                    template.getId(),
                    template.getName(),
                    template.getCategory(),
                    template.getTags(),
                    template.getCost(),
                    template.getBaseHp(),
                    template.getDamage(),
                    template.getActionInterval(),
                    template.getRecharge(),
                    template.getAbilityType(),
                    template.getAbilityValue(),
                    template.getPlantFoodType(),
                    template.getPlantFoodValue()
                );
                p.setPamPath(template.getPamPath());
                p.initHealth();
                return p;
            }
        }
        return null;
    }
}
