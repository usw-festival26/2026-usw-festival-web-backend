package com.usw.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FoodTruckEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void foodTruckCanBePersisted() {
        FoodTruck foodTruck = new FoodTruck("불초밥 트럭", "불초밥과 닭꼬치를 판매한다.", "https://example.com/food-truck.jpg");

        entityManager.persist(foodTruck);
        entityManager.flush();
        entityManager.clear();

        FoodTruck savedFoodTruck = entityManager.find(FoodTruck.class, foodTruck.getId());

        assertThat(savedFoodTruck).isNotNull();
        assertThat(savedFoodTruck.getName()).isEqualTo("불초밥 트럭");
        assertThat(savedFoodTruck.getDescription()).isEqualTo("불초밥과 닭꼬치를 판매한다.");
    }

    @Test
    void updateKeepsImageUrlWhenNullIsPassed() {
        FoodTruck foodTruck = new FoodTruck("불초밥 트럭", "불초밥과 닭꼬치를 판매한다.", "https://example.com/food-truck.jpg");

        foodTruck.update("타코 트럭", null, null);

        assertThat(foodTruck.getName()).isEqualTo("타코 트럭");
        assertThat(foodTruck.getDescription()).isEqualTo("불초밥과 닭꼬치를 판매한다.");
        assertThat(foodTruck.getImageUrl()).isEqualTo("https://example.com/food-truck.jpg");
    }

    @Test
    void removeImageUrlClearsImage() {
        FoodTruck foodTruck = new FoodTruck("불초밥 트럭", "불초밥과 닭꼬치를 판매한다.", "https://example.com/food-truck.jpg");

        foodTruck.removeImageUrl();

        assertThat(foodTruck.getImageUrl()).isNull();
    }
}
