package com.agrosphere.config;

import com.agrosphere.entity.CropType;
import com.agrosphere.entity.Disease;
import com.agrosphere.entity.User;
import com.agrosphere.enums.DiseaseSeverity;
import com.agrosphere.enums.Role;
import com.agrosphere.repository.CropTypeRepository;
import com.agrosphere.repository.DiseaseRepository;
import com.agrosphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component @RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CropTypeRepository cropTypeRepository;
    private final DiseaseRepository diseaseRepository;

    @Override public void run(String... args) {
        // Seed Admin
        if (!userRepository.existsByEmail("admin@agrosphere.in")) {
            userRepository.save(User.builder()
                .name("Super Admin").email("admin@agrosphere.in")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.SUPER_ADMIN).build());
            System.out.println("✅ Admin seeded: admin@agrosphere.in / Admin@123");
        }

        // Seed Crop Types
        if (cropTypeRepository.count() == 0) {
            List<CropType> crops = List.of(
                CropType.builder().name("Paddy (Rice)").category("Grain").seasonType("Kharif").avgDurationDays(120).iconEmoji("🌾").description("Staple grain crop grown in flooded fields").build(),
                CropType.builder().name("Wheat").category("Grain").seasonType("Rabi").avgDurationDays(150).iconEmoji("🌿").description("Winter cereal crop, widely cultivated").build(),
                CropType.builder().name("Tomato").category("Vegetable").seasonType("Annual").avgDurationDays(90).iconEmoji("🍅").description("High-value vegetable crop, quick harvest").build(),
                CropType.builder().name("Onion").category("Vegetable").seasonType("Rabi").avgDurationDays(120).iconEmoji("🧅").description("Cash crop with high market demand").build(),
                CropType.builder().name("Chilli").category("Spice").seasonType("Kharif").avgDurationDays(150).iconEmoji("🌶️").description("Famous Guntur chilli, high export value").build(),
                CropType.builder().name("Turmeric").category("Spice").seasonType("Kharif").avgDurationDays(270).iconEmoji("🟡").description("Golden spice with medicinal properties").build(),
                CropType.builder().name("Groundnut").category("Oilseed").seasonType("Kharif").avgDurationDays(120).iconEmoji("🥜").description("Important oilseed and protein source").build(),
                CropType.builder().name("Sugarcane").category("Cash Crop").seasonType("Annual").avgDurationDays(365).iconEmoji("🎋").description("Long-duration cash crop for sugar mills").build(),
                CropType.builder().name("Cotton").category("Fiber").seasonType("Kharif").avgDurationDays(180).iconEmoji("☁️").description("White gold - major fiber crop").build(),
                CropType.builder().name("Banana").category("Fruit").seasonType("Annual").avgDurationDays(300).iconEmoji("🍌").description("Perennial fruit with year-round production").build(),
                CropType.builder().name("Coconut").category("Fruit").seasonType("Perennial").avgDurationDays(365).iconEmoji("🥥").description("Versatile tree crop, all parts useful").build(),
                CropType.builder().name("Maize").category("Grain").seasonType("Kharif").avgDurationDays(90).iconEmoji("🌽").description("Versatile cereal, used for food and fodder").build()
            );
            cropTypeRepository.saveAll(crops);
            System.out.println("✅ Seeded " + crops.size() + " crop types");
        }

        // Seed Diseases
        if (diseaseRepository.count() == 0) {
            List<Disease> diseases = List.of(
                Disease.builder().name("Blast Disease").symptoms("Diamond-shaped lesions on leaves, neck rot").treatment("Apply tricyclazole or carbendazim fungicide").prevention("Use resistant varieties, avoid excessive nitrogen").severity(DiseaseSeverity.HIGH).affectedCrops("Paddy,Wheat").build(),
                Disease.builder().name("Brown Plant Hopper").symptoms("Yellowing, wilting, circular dead patches (hopper burn)").treatment("Apply imidacloprid or thiamethoxam").prevention("Avoid excess nitrogen, maintain water level").severity(DiseaseSeverity.HIGH).affectedCrops("Paddy").build(),
                Disease.builder().name("Leaf Rust").symptoms("Orange-brown pustules on leaf surface").treatment("Spray propiconazole or tebuconazole").prevention("Use resistant varieties, crop rotation").severity(DiseaseSeverity.MEDIUM).affectedCrops("Wheat").build(),
                Disease.builder().name("Damping Off").symptoms("Seedling collapse at soil level, water-soaked lesions").treatment("Drench with carbendazim, improve drainage").prevention("Seed treatment, avoid overwatering").severity(DiseaseSeverity.MEDIUM).affectedCrops("Tomato,Chilli").build(),
                Disease.builder().name("Early Blight").symptoms("Dark brown spots with concentric rings on lower leaves").treatment("Apply mancozeb or chlorothalonil").prevention("Crop rotation, remove infected debris").severity(DiseaseSeverity.MEDIUM).affectedCrops("Tomato").build(),
                Disease.builder().name("Thrips").symptoms("Silver streaks on leaves, curling, scarring on fruits").treatment("Spray spinosad or imidacloprid").prevention("Blue sticky traps, destroy crop residue").severity(DiseaseSeverity.MEDIUM).affectedCrops("Chilli,Onion").build(),
                Disease.builder().name("Collar Rot").symptoms("Brown rotting at stem base, plant collapse").treatment("Drench with copper oxychloride").prevention("Avoid waterlogging, use well-drained soil").severity(DiseaseSeverity.HIGH).affectedCrops("Groundnut,Turmeric").build(),
                Disease.builder().name("Aphids").symptoms("Yellowing leaves, sticky honeydew, curled shoots").treatment("Apply dimethoate or neem-based spray").prevention("Remove weed hosts, encourage predators").severity(DiseaseSeverity.LOW).affectedCrops("All crops").build()
            );
            diseaseRepository.saveAll(diseases);
            System.out.println("✅ Seeded " + diseases.size() + " diseases");
        }
    }
}
